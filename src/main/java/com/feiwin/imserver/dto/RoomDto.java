package com.feiwin.imserver.dto;

import com.feiwin.imserver.annotation.FieldName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomDto {
    private String roomId;

    @FieldName("muc#roomconfig_persistentroom")
    private Boolean isPersistent = false;

    @FieldName("muc#roomconfig_maxusers")
    private Integer maxUsers = 0;
}