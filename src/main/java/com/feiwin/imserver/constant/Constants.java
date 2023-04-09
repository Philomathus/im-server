package com.feiwin.imserver.constant;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.socket.WebSocketSession;
public class Constants {

    public static final Map<String, WebSocketSession> USERNAME_SESSION = new HashMap<>();
    public static final Map<WebSocketSession, XMPPTCPConnection> CONNECTIONS = new HashMap<>();

    public final static String MEMBER_ID = "memberId";

    public final static String USERNAME = "username";

    public static final String USER_JJWT_KEY = "im:user:jjwt:";

    public static final String WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    public static final String AUTHORIZATION = "Authorization";

    public static final int MINIMUM_WEBSOCKET_MESSAGE_SIZE = 16 * 1024 + 256;

}