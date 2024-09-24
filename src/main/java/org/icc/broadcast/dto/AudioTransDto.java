package org.icc.broadcast.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioTransDto {

    @NotNull(message = "session id is null")
    private String sessionId;
    @NotNull(message = "src lang is null")
    private String srcLang;
    @NotNull(message = "dest lang is null")
    private String destLang;
    @NotNull(message = "dest model is null")
    private String destModel;

}
