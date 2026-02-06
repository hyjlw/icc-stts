package org.icc.broadcast.constant;

import lombok.Getter;

@Getter
public enum ProcessType {
    TRANSLATION("TRANSLATION", "TRANSLATION"),
    SYNTHESISE("SYNTHESISE", "SYNTHESISE"),
    ;


    private String code;
    private String desc;

    ProcessType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
