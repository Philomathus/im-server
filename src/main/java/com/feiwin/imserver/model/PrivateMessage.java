package com.feiwin.imserver.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Document("PrivateMessage")
public class PrivateMessage {
    @Id
    private String id;

    private final String privateChatId;

    private final User sender;

    private final User receiver;

    private final String content;

    private final LocalDateTime sentAt;
}
