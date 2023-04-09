package com.feiwin.imserver.handler;

import com.feiwin.imserver.constant.Constants;
import com.feiwin.imserver.constant.MessageType;
import com.feiwin.imserver.service.XmppService;
import com.feiwin.imserver.utils.WsSendMessageUtils;
import com.feiwin.imserver.vo.WebSocketMessage;
import io.micrometer.common.lang.NonNullApi;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.util.Strings;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.EOFException;

/**
 * @author meng.jun
 */
@Log4j2
@Component
@NonNullApi
public class WebSocketHandler extends AbstractWebSocketHandler {
    @Resource
    private XmppService xmppService;

    @Override
    public void afterConnectionEstablished( WebSocketSession session ) throws Exception {
        super.afterConnectionEstablished( session );
        String memberId = session.getAttributes().getOrDefault( Constants.MEMBER_ID, Strings.EMPTY ).toString();
        String username = session.getAttributes().getOrDefault( Constants.USERNAME, Strings.EMPTY ).toString();

        if(Constants.USERNAME_SESSION.containsKey(memberId)) {
            WsSendMessageUtils.sendMessage(session,
                WebSocketMessage.builder()
                    .messageType(MessageType.ERROR)
                    .content("The member already has an active session!")
                    .to(username)
                    .build()
                    .toString()
            );
            session.close(CloseStatus.POLICY_VIOLATION);
            log.error("A member tried to have more than 1 sessions!");
            return;
        }

        try {
            connectToXmppServer(session, username);
        } catch(Exception e) {
            WsSendMessageUtils.sendMessage(session,
                WebSocketMessage.builder()
                    .messageType(MessageType.ERROR)
                    .content(e.getMessage())
                    .to(username)
                    .build()
                    .toString()
            );
            session.close(CloseStatus.SERVER_ERROR);
            return;
        }

        Constants.USERNAME_SESSION.put(username, session);

        session.setTextMessageSizeLimit( Constants.MINIMUM_WEBSOCKET_MESSAGE_SIZE );
    }

    private void connectToXmppServer(WebSocketSession session, String username) {
        log.info("Starting XMPP session '{}'.", username);

        XMPPTCPConnection connection = xmppService.connect(username);

        if (connection == null) {
            log.error("XMPP connection was not established. Closing websocket session...");
            throw new RuntimeException("Something went wrong establishing a connection");
        }

        try {
            try {
                log.info("Login into account.");
                xmppService.login(connection);
            } catch (Exception ex) {
                log.warn("The user does not exist. Creating a new account.");
                xmppService.createAccount(username);
                xmppService.login(connection);
            }
        } catch(Exception e) {
            log.error("There was a problem creating the account and logging in");
            throw new RuntimeException("There was a problem logging in");
        }

        Constants.CONNECTIONS.put(session, connection);

        log.info("Connection was stored");

        xmppService.addIncomingMessageListener(connection, (from, message, chat) ->
            WsSendMessageUtils.sendPersonalMessage(session, message)
        );

        WsSendMessageUtils.sendMessage(session,
            WebSocketMessage.builder()
                .messageType(MessageType.CONNECTED)
                .to(username)
                .build()
                .toString()
        );
    }

    @Override
    public void afterConnectionClosed( WebSocketSession session, CloseStatus status ) {
        String memberId = session.getAttributes().getOrDefault( Constants.MEMBER_ID, "" ).toString();

        Constants.USERNAME_SESSION.remove(memberId);
        Constants.CONNECTIONS.remove(session);
    }

    @Override
    public void handleTransportError( WebSocketSession session, Throwable exception ) throws Exception {
        super.handleTransportError( session, exception );

        String memberId = session.getAttributes().getOrDefault( Constants.MEMBER_ID, "" ).toString();
        if ( exception instanceof EOFException ) {
            //log.warn( "会员{}异常退出 EOFException", memberId );
        } else {
            //log.error( "会员{}发生连接异常:{}", memberId, exception.getMessage(), exception );
        }
    }
}
