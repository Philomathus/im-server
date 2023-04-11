package com.feiwin.imserver.model;

import lombok.*;

@Data
@RequiredArgsConstructor
public class User {
    private final String username;
    private Boolean hasDeleted = false;
}
