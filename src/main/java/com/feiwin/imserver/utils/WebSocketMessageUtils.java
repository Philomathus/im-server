package com.feiwin.imserver.utils;

import com.feiwin.imserver.constant.Constants;
import com.feiwin.imserver.constant.MessageType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.jivesoftware.smack.packet.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.feiwin.imserver.vo.WebSocketMessage;
import static org.jivesoftware.smackx.address.MultipleRecipientManager.send;

@Component
@Log4j2
public class WebSocketMessageUtils {
    private static WebSocketMessageUtils me;

    @PostConstruct
    void init() {
        me = this;
    }

    @Resource
    private ThreadPoolTaskExecutor executor;

    public static void sendPersonalMessage(WebSocketSession webSocketSession, Message message) {
        String from = message.getFrom().getLocalpartOrNull().toString();
        String to = message.getTo().getLocalpartOrNull().toString();
        String content = message.getBody();
        sendMessage(
            webSocketSession,
            WebSocketMessage.builder()
                .messageType(MessageType.PRIVATE)
                .content(content)
                .from(from)
                .to(to)
                .build()
                .toString()
        );
        log.info("New message from '{}' to '{}': {}", from, to, content);
    }

    public static void sendRoomMessage(WebSocketSession webSocketSession, Message message) {
        String roomId = message.getFrom().getLocalpartOrNull().toString();
        String[] fromAndBodySplit = splitFromAndBody(message.getBody());
        String from = fromAndBodySplit[0];
        String content = fromAndBodySplit[1];
        sendMessage(webSocketSession,
            WebSocketMessage.builder()
                .messageType(MessageType.ROOM)
                .content(content)
                .from(from)
                .roomId(roomId)
                .build()
                .toString()
        );

        log.info("New message from '{}' to '{}': {}", from, roomId, content);
    }

    public static void sendMessage( WebSocketSession webSocketSession, String messageBody ) {
        // 用普通线程去发送消息,避免发送消息失败导致nio线程被消耗
        me.executor.execute( () -> sendMessage( webSocketSession, messageBody, 3 ) );
    }

    private static void sendMessage( WebSocketSession webSocketSession, String messageBody, int retryNum ) {
        if ( retryNum <= 0 ) {
            return;
        }
        if ( webSocketSession == null ) {
            return;
        }
        String username = webSocketSession.getAttributes().getOrDefault( Constants.USERNAME, "" ).toString();
        if ( webSocketSession.isOpen() ) {
            try {
                webSocketSession.sendMessage( new TextMessage( messageBody ) );
                //log.info( "会员{}发送消息成功 - 消息:{}", username, messageBody );
            } catch ( Exception e ) {
                if ( retryNum == 1 ) {
                    log.error( "会员{}发送消息发生错误:{}", username, e.getMessage(), e );
                    sendClose( webSocketSession, username );
                    return;
                }
                try {
                    Thread.sleep( 800L );
                } catch ( Exception ignored ) {
                }
                retryNum--;
                sendMessage( webSocketSession, messageBody, retryNum );
            }
        } else {
            log.error( "会员{}的webSocketSession已关闭,无法发送消息", username );
            try {
                Thread.sleep( 800L );
            } catch ( Exception ignored ) {
            }
            // 间隔800毫秒再次重发1次
            if ( webSocketSession.isOpen() ) {
                try {
                    webSocketSession.sendMessage( new TextMessage( messageBody ) );
                } catch ( Exception ignored ) {
                    sendClose( webSocketSession, username );
                }
            }
        }
    }

    private static void sendClose( WebSocketSession webSocketSession, String memberId ) {
        try {
            webSocketSession.close( CloseStatus.NORMAL );
        } catch ( Exception e ) {
            log.error( "会员{}发送关闭通知发生错误:{}", memberId, e.getMessage(), e );
        }
    }

    private static String[] splitFromAndBody(String fromAndBody) {
        if(fromAndBody == null) {
            return new String[2];
        }

        int indexOfDelimiter = fromAndBody.indexOf(':');
        return new String[] {
                fromAndBody.substring(0, indexOfDelimiter),
                fromAndBody.substring(indexOfDelimiter + 1)
        };
    }
}
