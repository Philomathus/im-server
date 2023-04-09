package com.feiwin.imserver.service;

import com.feiwin.imserver.annotation.FieldName;
import com.feiwin.imserver.constant.MessageType;
import com.feiwin.imserver.dto.PrivateMessage;
import com.feiwin.imserver.dto.RoomCreation;
import com.feiwin.imserver.dto.RoomMessage;
import com.feiwin.imserver.dto.RoomUser;
import com.feiwin.imserver.utils.WsSendMessageUtils;
import com.feiwin.imserver.vo.WebSocketMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.feiwin.imserver.constant.Constants.CONNECTIONS;
import static com.feiwin.imserver.constant.Constants.USERNAME_SESSION;

@Service
@Slf4j
public class ImService {

    @Resource
    private XmppService xmppService;
    
    public void sendPrivateMessage(PrivateMessage privateMessage) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(privateMessage.getUsername());

        xmppService.sendPrivateMessage(CONNECTIONS.get(webSocketSession), privateMessage.getContent(), privateMessage.getTo());
    }
    
    public void sendRoomMessage(RoomMessage roomMessage) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(roomMessage.getUsername());
        xmppService.sendRoomMessage(CONNECTIONS.get(webSocketSession), roomMessage.getRoomId(), roomMessage.getContent());
    }

    public void createRoom(RoomCreation roomCreation) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(roomCreation.getUsername());

        Map<String, String> settings = new HashMap<>();

        for(Field field : RoomCreation.class.getDeclaredFields()) {
            FieldName fieldNameAnnotation = field.getAnnotation(FieldName.class);

            if(fieldNameAnnotation != null) {
                field.setAccessible(true);

                try {
                    settings.put(fieldNameAnnotation.value(), Objects.toString(field.get(roomCreation), null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        xmppService.createRoom(CONNECTIONS.get(webSocketSession),
            roomCreation.getRoomId(),
            settings,
            msg -> WsSendMessageUtils.sendRoomMessage(webSocketSession, msg)
        );
    }
    
    public void joinRoom(RoomUser roomUser) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(roomUser.getUsername());

        xmppService.joinRoom(CONNECTIONS.get(webSocketSession), roomUser.getRoomId(),
            msg -> WsSendMessageUtils.sendRoomMessage(webSocketSession, msg));
    } 
    
    public void leaveRoom(RoomUser roomUser) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(roomUser.getUsername());
        xmppService.leaveRoom(CONNECTIONS.get(webSocketSession), roomUser.getRoomId());
    }
    
    public void destroyRoom(RoomUser roomUser) {
        WebSocketSession webSocketSession = USERNAME_SESSION.get(roomUser.getUsername());
        xmppService.destroyRoom(CONNECTIONS.get(webSocketSession), roomUser.getRoomId());
    }
}
