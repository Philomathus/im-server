package com.feiwin.imserver.dto;

import lombok.Data;

@Data
public class TokenAuth {
    private String memberId;
    private String username;
}