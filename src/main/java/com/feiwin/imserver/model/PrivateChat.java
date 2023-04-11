package com.feiwin.imserver.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@RequiredArgsConstructor
@Document("PrivateChat")
public class PrivateChat {
    @Id
    private String id;

    private final User[] users;
}
