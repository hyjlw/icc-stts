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
public class TtsResp {
    @JsonProperty("audio_path")
    private String audioPath;
}
