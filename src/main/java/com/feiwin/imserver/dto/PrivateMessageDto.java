package com.feiwin.imserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivateMessageDto {
    private String content;
    private String to;
}
