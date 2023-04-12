package com.feiwin.imserver.service;

import com.feiwin.imserver.model.PrivateChat;
import com.feiwin.imserver.model.PrivateMessage;
import com.feiwin.imserver.model.User;
import com.feiwin.imserver.repository.PrivateChatRepository;
import com.feiwin.imserver.repository.PrivateMessageRepository;
import com.feiwin.imserver.utils.WebSocketMessageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.feiwin.imserver.constant.Constants.CONNECTIONS;
import static com.feiwin.imserver.constant.Constants.USERNAME_SESSION;

@Service
@Slf4j
public class ImService {
    @Resource
    private XmppService xmppService;
    @Resource
    private PrivateChatRepository privateChatRepository;
    @Resource
    private PrivateMessageRepository privateMessageRepository;
    
    public void sendPrivateMessage(String username, String content, String to) {
        xmppService.sendPrivateMessage(getConnectionByUsername(username), content, to);

        User[] users = { new User(username), new User(to) };

        PrivateChat privateChat = privateChatRepository.queryPrivateChatByUsers(username, to);

        if(privateChat == null) {
            privateChat = privateChatRepository.insert( new PrivateChat( users, LocalDateTime.now(), LocalDateTime.now() ) );
        }

        privateMessageRepository.insert( new PrivateMessage( privateChat.getId(), users[0], users[1], content, LocalDateTime.now() ) );
    }

    public List<String> getUsersConversingWithUsername(String username) {
        List<PrivateChat> privateChats = privateChatRepository.queryPrivateChatsByUser(username);

        if(CollectionUtils.isEmpty(privateChats)) {
            return Collections.emptyList();
        }

        return privateChats.stream()
            .map(privateChat -> {
                String user1 = privateChat.getUsers()[0].getUsername();
                String user2 = privateChat.getUsers()[1].getUsername();
                return username.equals(user1) ? user2 : user1;
            })
            .toList();
    }

    public List<PrivateMessage> getPrivateMessageHistory(String user1, String user2) {
        PrivateChat privateChat = privateChatRepository.queryPrivateChatByUsers(user1, user2);

        if(privateChat == null) {
            return Collections.emptyList();
        }

        List<PrivateMessage> privateMessages = privateMessageRepository.getPrivateMessagesOnUserSideByPrivateChatId( user1, privateChat.getId() );

        if(CollectionUtils.isEmpty(privateMessages)) {
            return Collections.emptyList();
        }

        return privateMessages;
    }

    public void deletePrivateChatByUsers(String user1, String user2) {
        PrivateChat privateChat = privateChatRepository.queryPrivateChatByUsers(user1, user2);

        if(privateChat == null) {
            return;
        }

        for(User user : privateChat.getUsers()) {
            if(StringUtils.equals( user1, user.getUsername() )) {
                user.setHasDeleted(true);
                privateChatRepository.save(privateChat);
                return;
            }
        }
    }

    public void deletePrivateMessageOnUserSideById(String username, String privateMessageId) {
        PrivateMessage privateMessage = privateMessageRepository.findById( privateMessageId ).orElse(null);

        if(privateMessage == null) {
            return;
        }

        for(User user : new User[] { privateMessage.getSender(), privateMessage.getReceiver() }) {
            if(StringUtils.equals(username, user.getUsername())) {
                user.setHasDeleted(true);
                privateMessageRepository.save(privateMessage);
                return;
            }
        }
    }
    
    public void sendRoomMessage(String username, String roomId, String content) {
        xmppService.sendRoomMessage(getConnectionByUsername(username), roomId, content);
    }

    public void createRoom(String username, String roomId, Map<String, String> settings) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(username);

        xmppService.createRoom(CONNECTIONS.get(webSocketSession),
            roomId,
            settings,
            msg -> WebSocketMessageUtils.sendRoomMessage(webSocketSession, msg)
        );
    }
    
    public void joinRoom(String username, String roomId) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(username);

        xmppService.joinRoom(CONNECTIONS.get(webSocketSession), roomId,
            msg -> WebSocketMessageUtils.sendRoomMessage(webSocketSession, msg));
    }
    
    public void leaveRoom(String username, String roomId) {
        xmppService.leaveRoom(getConnectionByUsername(username), roomId);
    }
    
    public void destroyRoom(String username, String roomId) {
        xmppService.destroyRoom(getConnectionByUsername(username), roomId);
    }

    public List<String> getAllRooms() {
        return xmppService.getAllRooms();
    }

    public List<String> getJoinedRooms(String username) {
        return xmppService.getJoinedRooms(getConnectionByUsername(username));
    }

    public List<String> getRoomOccupants(String username, String roomId) {
        return xmppService.getRoomOccupants(getConnectionByUsername(username), roomId);
    }

    private static XMPPTCPConnection getConnectionByUsername(String username) {
        return CONNECTIONS.get(USERNAME_SESSION.get(username));
    }
}