package com.feiwin.imserver.repository;

import com.feiwin.imserver.model.PrivateChat;
import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PrivateChatRepository extends MongoRepository<PrivateChat, String> {

    @Query(value = "{ users: { $all: [ { username: '?0', hasDeleted: false }, { username: '?1' } ] } }")
    PrivateChat queryPrivateChatByUsers(String user1, String user2);

    @Query("{ users: { username: '?0', hasDeleted: false } }")
    List<PrivateChat> queryPrivateChatsByUser(String user);
}
