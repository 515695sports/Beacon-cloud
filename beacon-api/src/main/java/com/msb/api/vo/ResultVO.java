package com.msb.api.vo;

import lombok.Data;

@Data
public class ResultVO {

    private Integer code;

    private String msg;

    private Integer count;

    private Long fee;

    private String uid;

    private String sid;

}
