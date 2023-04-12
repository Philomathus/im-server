package com.feiwin.imserver.model;

import lombok.*;

@Data
@RequiredArgsConstructor
public class User {
    @NonNull
    private String username;
    private Boolean hasDeleted = false;
}
