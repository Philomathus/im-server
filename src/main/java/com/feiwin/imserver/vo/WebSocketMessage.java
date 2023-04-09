package com.feiwin.imserver.vo;

import com.feiwin.imserver.constant.MessageType;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebSocketMessage {
    private static Gson gson = new Gson();

    private MessageType messageType;
    private String content;
    private String roomId;
    private String from;
    private String to;

    public String toString() {
        return gson.toJson(this);
    }
}
