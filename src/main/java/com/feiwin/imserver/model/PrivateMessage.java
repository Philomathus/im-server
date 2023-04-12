package com.feiwin.imserver.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@Document("privateMessages")
public class PrivateMessage {
    @Id
    private String id;

    @NonNull
    private String privateChatId;
    @NonNull
    private User sender;
    @NonNull
    private User receiver;
    @NonNull
    private String content;
    @NonNull
    private LocalDateTime sentAt;
}
