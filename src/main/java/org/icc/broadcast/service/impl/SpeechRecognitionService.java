package org.icc.broadcast.service.impl;

import org.icc.broadcast.reader.BinaryAudioStreamReader;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class SpeechRecognitionService {

    private static final Cache<String, SpeechConfig> CONFIG_CACHE = Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();
    private static final Cache<String, SpeechTranslationConfig> TRANSLATION_CONFIG_CACHE = Caffeine.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();

    @Value("${ms.speech.key}")
    private String speechKey;
    @Value("${ms.speech.region}")
    private String speechRegion;

    @Value("${file.download.dir}")
    private String baseDir;

    @Value("${tts.config.ssml}")
    private String ttsSSML;

    /**
     * build the speech config
     * @param lang en-US, zh-CN
     * @return speechConfig with lang
     */
    public SpeechConfig buildSpeechConfig(String lang) {
        log.info("build speech config with: {}, {}", speechRegion, lang);
        SpeechConfig speechConfig = CONFIG_CACHE.getIfPresent(lang);

        if(speechConfig == null) {
            speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage(lang);

            CONFIG_CACHE.put(lang, speechConfig);
        }

        return speechConfig;
    }

    /**
     * build the speech config
     * @param srcLang en-US, zh-CN
     * @param destLang en-US, zh-CN
     * @return speechConfig with lang
     */
    public SpeechTranslationConfig buildSpeechTranslationConfig(String srcLang, String destLang) {
        String key = srcLang + ":" + destLang;
        SpeechTranslationConfig speechConfig = TRANSLATION_CONFIG_CACHE.getIfPresent(key);

        if(speechConfig == null) {
            log.info("build speech config with: {}, {}, {}", speechRegion, srcLang, destLang);

            speechConfig = SpeechTranslationConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechRecognitionLanguage(srcLang);
            speechConfig.addTargetLanguage(destLang);

            TRANSLATION_CONFIG_CACHE.put(key, speechConfig);
        }

        return speechConfig;
    }

    /**
     * if audio is too long or audio with more than 1s silence gap, the result is not good
     *
     * @param lang
     * @param filePath
     * @param useStream
     * @return
     */
    public String recognizeFromSpeechOnceAsync(String lang, String filePath, boolean useStream) {
        SpeechConfig speechConfig = buildSpeechConfig(lang);
        AudioConfig audioConfig = null;
        SpeechRecognizer speechRecognizer = null;

        try {
            if(useStream) {
                PullAudioInputStream pullAudio = AudioInputStream.createPullStream(new BinaryAudioStreamReader(filePath),
                        AudioStreamFormat.getCompressedFormat(AudioStreamContainerFormat.MP3));
                audioConfig = AudioConfig.fromStreamInput(pullAudio);
            } else {
                audioConfig = AudioConfig.fromWavFileInput(filePath);
            }

            speechRecognizer = new SpeechRecognizer(speechConfig, audioConfig);

            log.info("start to recognize speech: {}", filePath);

            String text = null;
            Future<SpeechRecognitionResult> task = speechRecognizer.recognizeOnceAsync();
            SpeechRecognitionResult speechRecognitionResult = task.get();

            if (speechRecognitionResult.getReason() == ResultReason.RecognizedSpeech) {
                text = speechRecognitionResult.getText();
                log.info("RECOGNIZED: Text=" + text);
            }
            else if (speechRecognitionResult.getReason() == ResultReason.NoMatch) {
                log.info("NOMATCH: Speech could not be recognized.");
            }
            else if (speechRecognitionResult.getReason() == ResultReason.Canceled) {
                CancellationDetails cancellation = CancellationDetails.fromResult(speechRecognitionResult);
                log.info("CANCELED: Reason=" + cancellation.getReason());

                if (cancellation.getReason() == CancellationReason.Error) {
                    log.info("CANCELED: ErrorCode=" + cancellation.getErrorCode());
                    log.info("CANCELED: ErrorDetails=" + cancellation.getErrorDetails());
                    log.info("CANCELED: Did you set the speech resource key and region values?");
                }
            }

            return text;
        } catch (Exception e) {
            log.error("recognizeFromSpeech error: {}", e.getMessage(), e);
        } finally {
            if(audioConfig != null) {
                audioConfig.close();
            }
            if(speechRecognizer != null) {
                speechRecognizer.close();
            }
        }

        return null;
    }

    /**
     * continuous recognizing
     *
     * @param lang
     * @param filePath
     * @param useStream
     * @return
     */
    public String recognizeFromSpeech(String lang, String filePath, boolean useStream) {
        SpeechConfig speechConfig = buildSpeechConfig(lang);
        AudioConfig audioConfig = null;
        SpeechRecognizer speechRecognizer = null;
        Semaphore recognitionEnd = new Semaphore(0);

        try {
            if(useStream) {
                PullAudioInputStream pullAudio = AudioInputStream.createPullStream(new BinaryAudioStreamReader(filePath),
                        AudioStreamFormat.getCompressedFormat(AudioStreamContainerFormat.MP3));
                audioConfig = AudioConfig.fromStreamInput(pullAudio);
            } else {
                audioConfig = AudioConfig.fromWavFileInput(filePath);
            }

            speechRecognizer = new SpeechRecognizer(speechConfig, audioConfig);

            log.info("start to recognize speech: {}", filePath);
            List<String> partResults = new ArrayList<>();

            speechRecognizer.recognizing.addEventListener((s, e) -> {
                // Intermediate result (hypothesis).
                if (e.getResult().getReason() == ResultReason.RecognizingSpeech)
                {
                    log.debug("recognizing tmp result: {}", e.getResult().getText());
                }
                else
                {
                    log.info("recognizing other reason: {}", e.getResult().getReason());
                }
            });

            speechRecognizer.recognized.addEventListener((s, e) ->
            {
                if (e.getResult().getReason() == ResultReason.RecognizedKeyword)
                {
                    // Keyword detected, speech recognition will start.
                    log.info("KEYWORD: Text=" + e.getResult().getText());
                }
                else if (e.getResult().getReason() == ResultReason.RecognizedSpeech)
                {
                    // Final result. May differ from the last intermediate result.
                    log.info("RECOGNIZED: Text=" + e.getResult().getText());
                    partResults.add(e.getResult().getText());
                }
                else if (e.getResult().getReason() == ResultReason.NoMatch)
                {
                    // NoMatch occurs when no speech was recognized.
                    NoMatchReason reason = NoMatchDetails.fromResult(e.getResult()).getReason();
                    log.info("NOMATCH: Reason=" + reason);
                }
            });

            speechRecognizer.canceled.addEventListener((s, e) ->
            {
                log.info("CANCELED: Reason=" + e.getReason());

                if (e.getReason() == CancellationReason.Error)
                {
                    // NOTE: In case of an error, do not use the same recognizer for recognition anymore.
                    log.error("CANCELED: ErrorCode=" + e.getErrorCode());
                    log.error("CANCELED: ErrorDetails=\"" + e.getErrorDetails() + "\"");
                }
            });

            speechRecognizer.sessionStarted.addEventListener((s, e) ->
            {
                log.info("Speech Recognizer Session started.");
            });

            speechRecognizer.sessionStopped.addEventListener((s, e) ->
            {
                log.info("Session stopped.");
                recognitionEnd.release();
            });

            speechRecognizer.startContinuousRecognitionAsync().get();

            recognitionEnd.acquire();
            // Stops recognition.
            speechRecognizer.stopContinuousRecognitionAsync().get();

            if(!CollectionUtils.isEmpty(partResults)) {
                return StringUtils.join(partResults, " ");
            }
        } catch (Exception e) {
            log.error("recognizeFromSpeech error: {}", e.getMessage(), e);
        } finally {
            if(audioConfig != null) {
                audioConfig.close();
            }
            if(speechRecognizer != null) {
                speechRecognizer.close();
            }
        }

        return null;
    }

    public String translateSpeechAsync(String srcLang, String destLang, String filePath)
    {
        SpeechTranslationConfig speechConfig = buildSpeechTranslationConfig(srcLang, destLang);
        AudioConfig audioConfig = null;
        TranslationRecognizer recognizer = null;

        List<String> resultTexts = new ArrayList<>();
        Semaphore recognitionEnd = new Semaphore(0);

        try {
            audioConfig = AudioConfig.fromWavFileInput(filePath);

            recognizer = new TranslationRecognizer(speechConfig, audioConfig);

            // Subscribes to events.
            recognizer.recognizing.addEventListener((s, e) ->
            {
                // Intermediate result (hypothesis).
                // Note that embedded "many-to-1" translation models support only one
                // target language (the model native output language). For example, a
                // "Many-to-English" model generates only output in English.
                // At the moment embedded translation cannot provide transcription or
                // language ID of the source language.
                if (e.getResult().getReason() == ResultReason.TranslatingSpeech) {
                    for (Map.Entry<String, String> translation : e.getResult().getTranslations().entrySet()) {
                        String targetLang = translation.getKey();
                        String outputText = translation.getValue();
                        log.debug("Translating [" + targetLang + "]: " + outputText);
                    }
                }
            });

            recognizer.recognized.addEventListener((s, e) ->
            {
                if (e.getResult().getReason() == ResultReason.TranslatedSpeech) {
                    // Final result. May differ from the last intermediate result.
                    for (Map.Entry<String, String> translation : e.getResult().getTranslations().entrySet()) {
                        String targetLang = translation.getKey();
                        String outputText = translation.getValue();
                        log.info("TRANSLATED [{}]: {}", targetLang, outputText);

                        resultTexts.add(outputText);
                    }
                } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                    // NoMatch occurs when no speech was recognized.
                    NoMatchReason reason = NoMatchDetails.fromResult(e.getResult()).getReason();
                    log.info("NOMATCH: Reason=" + reason);
                }
            });

            recognizer.canceled.addEventListener((s, e) ->
            {
                log.error("CANCELED: Reason={}", e.getReason());

                if (e.getReason() == CancellationReason.Error) {
                    // NOTE: In case of an error, do not use the same recognizer for recognition anymore.
                    log.error("CANCELED: ErrorCode=" + e.getErrorCode());
                    log.error("CANCELED: ErrorDetails=\"" + e.getErrorDetails() + "\"");
                }
            });

            recognizer.sessionStarted.addEventListener((s, e) ->
            {
                log.info("Session started.");
            });

            recognizer.sessionStopped.addEventListener((s, e) ->
            {
                log.info("Session stopped.");
                recognitionEnd.release();
            });

            // The following lines run continuous recognition that listens for speech
            // in input audio and generates results until stopped. To run recognition
            // only once, replace this code block with
            //
            // SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();
            //
            // and optionally check this returned result for the outcome instead of
            // doing it in event handlers.

            // Starts continuous recognition.
            recognizer.startContinuousRecognitionAsync().get();
            recognitionEnd.acquire();

            // Stops recognition.
            recognizer.stopContinuousRecognitionAsync().get();

        } catch (Exception e) {
            log.error("speech translation error:", e);
        } finally {
            if(audioConfig != null) {
                audioConfig.close();
            }
            if(recognizer != null) {
                recognizer.close();
            }
        }

        return StringUtils.join(resultTexts, ", ");
    }


    /**
     *
     * @param lang e.g zh-CN, en-US
     * @param voiceName e.g zh-CN-YunfengNeural, en-US-DavisNeural
     * @param text
     * @return the file path
     */
    public void synthesizeTextToSpeech(String lang, String voiceName, String text, String destFilePath) {
        SpeechConfig speechConfig = buildSynthesizeSpeechConfig(lang, voiceName);
        SpeechSynthesizer speechSynthesizer = null;
        try {
            AudioConfig audioConfig = AudioConfig.fromWavFileOutput(destFilePath);

            speechSynthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
            speechSynthesizer.SpeakText(text);
        } catch (Exception e) {
            log.error("text to speech error: {}", e.getMessage(), e);
        } finally {
            if(speechSynthesizer != null) {
                speechSynthesizer.close();
            }
        }
    }

    /**
     *
     * @param lang e.g zh-CN, en-US
     * @param voiceName e.g zh-CN-YunfengNeural, en-US-DavisNeural
     * @param text
     * @return the file path
     */
    public void synthesizeTextToSpeechSsml(String lang, String voiceName, String text, String destFilePath) {
        SpeechConfig speechConfig = buildSsmlSynthesizeSpeechConfig(lang, voiceName);
        SpeechSynthesizer speechSynthesizer = null;
        try {
            speechSynthesizer = new SpeechSynthesizer(speechConfig, null);

            String ssml = ttsSSML
                    .replace("{lang}", lang)
                    .replace("{voiceName}", voiceName)
                    .replace("{content}", text);
            SpeechSynthesisResult result = speechSynthesizer.SpeakSsml(ssml);
            AudioDataStream stream = AudioDataStream.fromResult(result);
            stream.saveToWavFile(destFilePath);
        } catch (Exception e) {
            log.error("text to speech ssml, error: {}", e.getMessage(), e);
        } finally {
            if(speechSynthesizer != null) {
                speechSynthesizer.close();
            }
        }
    }

    public SpeechConfig buildSynthesizeSpeechConfig(String lang, String voiceName) {
        SpeechConfig speechConfig = CONFIG_CACHE.getIfPresent(lang + "_" + voiceName);

        if(speechConfig == null) {
            speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);

            // Set either the `SpeechSynthesisVoiceName` or `SpeechSynthesisLanguage`.
            speechConfig.setSpeechSynthesisLanguage(lang);
            speechConfig.setSpeechSynthesisVoiceName(voiceName);
            speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Riff24Khz16BitMonoPcm);

            CONFIG_CACHE.put(lang + "_" + voiceName, speechConfig);
        }

        return speechConfig;
    }

    public SpeechConfig buildSsmlSynthesizeSpeechConfig(String lang, String voiceName) {
        String key = "ssml_" + lang + "_" + voiceName;
        SpeechConfig speechConfig = CONFIG_CACHE.getIfPresent(key);

        if(speechConfig == null) {
            speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
            speechConfig.setSpeechSynthesisOutputFormat(SpeechSynthesisOutputFormat.Riff44100Hz16BitMonoPcm);

            CONFIG_CACHE.put(key, speechConfig);
        }

        return speechConfig;
    }
}