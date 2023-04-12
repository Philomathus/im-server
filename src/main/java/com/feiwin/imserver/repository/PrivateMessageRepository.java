package com.feiwin.imserver.repository;

import com.feiwin.imserver.model.PrivateMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PrivateMessageRepository extends MongoRepository<PrivateMessage, String> {

    @Query("""
    {
        $and: [
            { privateChatId: '?1' },
            {
                $or: [
                    { sender:   { username: '?0', hasDeleted: false } },
                    { receiver: { username: '?0', hasDeleted: false } }
                ]
            }
        ]
    }
    """)
    List<PrivateMessage> getPrivateMessagesOnUserSideByPrivateChatId(String user, String privateChatId);

}
