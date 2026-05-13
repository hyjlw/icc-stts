package org.icc.broadcast.constant;

import lombok.Getter;

@Getter
public enum TtsProvider {
    AZURE("AZURE", "AZURE"),
    VOX_CPM("VOX_CPM", "VOX_CPM"),
    ;


    private String code;
    private String desc;

    TtsProvider(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
