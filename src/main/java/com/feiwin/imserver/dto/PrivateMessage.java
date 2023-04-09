package com.feiwin.imserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateMessage {
    private String content;
    private String username;
    private String to;
}
