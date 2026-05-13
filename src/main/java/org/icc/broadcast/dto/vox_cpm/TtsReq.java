package org.icc.broadcast.dto.vox_cpm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsReq {
    private String text;
    private String language;
    @JsonProperty("ref_audio_name")
    private String refAudioName;
}
