package com.feiwin.imserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomMessageDto {
    private String content;
    private String roomId;
}
