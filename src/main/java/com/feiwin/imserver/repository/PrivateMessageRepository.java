package com.feiwin.imserver.repository;

import com.feiwin.imserver.model.PrivateMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrivateMessageRepository extends MongoRepository<PrivateMessage, String> {

    PrivateMessage getPrivateMessageByPrivateChatId(String privateChatId);

}
