package com.feiwin.imserver.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomUser {
    private String roomId;
    private String username;
}