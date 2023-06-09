package com.feiwin.imserver.service;

import com.feiwin.imserver.config.XmppProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PresenceBuilder;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.form.FillableForm;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@EnableConfigurationProperties(XmppProperties.class)
public class XmppService {

    private XMPPTCPConnection adminXmppConnection;

    @Resource
    private XmppProperties xmppProperties;

    @PostConstruct
    private void loginAdminXmppAccount() {
        adminXmppConnection = connect(xmppProperties.getAdminUsername(), xmppProperties.getAdminPassword());
        login(adminXmppConnection);
    }

    public XMPPTCPConnection connect(String username) {
        return connect(username, xmppProperties.getUserPassword());
    }

    public XMPPTCPConnection connect(String username, String password) {
        XMPPTCPConnection connection;
        try {
            EntityBareJid entityBareJid = JidCreate.entityBareFrom(username + "@" + xmppProperties.getDomain());
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setHost(xmppProperties.getHost())
                    .setPort(xmppProperties.getPort())
                    .setXmppDomain(xmppProperties.getDomain())
                    .setUsernameAndPassword(entityBareJid.getLocalpart(), password)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource(entityBareJid.getResourceOrEmpty())
                    .setSendPresence(true)
                    .build();

            connection = new XMPPTCPConnection(config);
            connection.connect();
        } catch (Exception e) {
            log.info("Could not connect to XMPP server.", e);
            throw new RuntimeException(e);
        }
        return connection;
    }

    public void login(XMPPTCPConnection connection) {
        try {
            connection.login();
        } catch (Exception e) {
            log.error("Login to XMPP server with user {} failed.", connection.getUser(), e);
            throw new RuntimeException(e);
        }
        log.info("User '{}' logged in.", connection.getUser());
    }

    public void createAccount(String username) {
        createAccount(username, xmppProperties.getUserPassword());
    }

    public void createAccount(String username, String password) {
        AccountManager accountManager = AccountManager.getInstance(adminXmppConnection);
        accountManager.sensitiveOperationOverInsecureConnection(true);
        try {
            accountManager.createAccount(Localpart.from(username), password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        log.info("Account for user '{}' created.", username);
    }

    public void joinRoom(XMPPTCPConnection connection, String roomId, MessageListener listener) {
        try {
            EntityBareJid jid = JidCreate.entityBareFrom(roomId + "@" + xmppProperties.getRoomDomain());
            MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
            multiUserChatManager.getRoomInfo(jid);
            MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(jid);
            multiUserChat.join(connection.getUser().getResourcepart());
            multiUserChat.addMessageListener(listener);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createRoom(XMPPTCPConnection connection, String roomId, Map<String, String> settings, MessageListener listener) {

        try {
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(connection)
                    .getMultiUserChat(JidCreate.entityBareFrom(roomId + "@" + xmppProperties.getRoomDomain()));
            multiUserChat.create(connection.getUser().getResourcepart());

            FillableForm form = multiUserChat.getConfigurationForm().getFillableForm();
            settings.forEach(form::setAnswer);
            multiUserChat.sendConfigurationForm(form);
            multiUserChat.addMessageListener( listener );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPrivateMessage(XMPPTCPConnection connection, String content, String to) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        try {
            Chat chat = chatManager.chatWith(JidCreate.entityBareFrom(to + "@" + xmppProperties.getDomain()));
            chat.send(content);
            log.info("Message sent to user '{}' from user '{}'.", to, connection.getUser());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRoomMessage(XMPPTCPConnection connection, String roomId, String content) {
        try {
            EntityBareJid groupJid = JidCreate.entityBareFrom(roomId + "@" + xmppProperties.getRoomDomain());
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(connection)
                    .getMultiUserChat(groupJid);
            multiUserChat.sendMessage(connection.getUser().getLocalpart() + ":" + content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void leaveRoom(XMPPTCPConnection connection, String roomId) {
        try {
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(connection)
                    .getMultiUserChat(JidCreate.entityBareFrom(roomId + "@" + xmppProperties.getRoomDomain()));
            multiUserChat.leave();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroyRoom(XMPPTCPConnection connection, String roomId) {
        try {
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(connection)
                    .getMultiUserChat(JidCreate.entityBareFrom(roomId + '@' + xmppProperties.getRoomDomain()));
            multiUserChat.destroy(null, null);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllRooms() {
        try {
            Map<EntityBareJid, HostedRoom> jidHostedRoomMap = MultiUserChatManager.getInstanceFor(adminXmppConnection)
                .getRoomsHostedBy( JidCreate.domainBareFrom( xmppProperties.getRoomDomain() ) );

            return jidHostedRoomMap.values().stream().map(HostedRoom::getName).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getJoinedRooms(XMPPTCPConnection connection) {
        return MultiUserChatManager.getInstanceFor(connection)
                .getJoinedRooms()
                .stream()
                .map(entityBareJid -> entityBareJid.getLocalpart().toString())
                .toList();
    }

    public List<String> getRoomOccupants(XMPPTCPConnection connection, String roomId) {
        try {
            MultiUserChat multiUserChat = MultiUserChatManager.getInstanceFor(connection)
                    .getMultiUserChat(JidCreate.entityBareFrom(roomId + '@' + xmppProperties.getRoomDomain()));

            return multiUserChat.getOccupants()
                    .stream()
                    .map(entityFullJid -> entityFullJid.getLocalpart().toString())
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(XMPPTCPConnection connection) {
        Presence presence = PresenceBuilder.buildPresence()
                .ofType(Presence.Type.unavailable)
                .build();
        try {
            connection.sendStanza(presence);
            connection.disconnect();
        } catch (Exception e) {
            log.error("XMPP error.", e);
        }
        connection.disconnect();
        log.info("Connection closed for user '{}'.", connection.getUser());
    }

    public void sendStanza(XMPPTCPConnection connection, Presence.Type type) {
        Presence presence = PresenceBuilder.buildPresence()
                .ofType(type)
                .build();
        try {
            connection.sendStanza(presence);
            log.info("Status {} sent for user '{}'.", type, connection.getUser());
        } catch (Exception e) {
            log.error("XMPP error.", e);
            throw new RuntimeException(connection.getUser().toString(), e);
        }
    }

    public void addIncomingMessageListener(XMPPTCPConnection connection, IncomingChatMessageListener listener) {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(listener);
        log.info("Incoming message listener for user '{}' added.", connection.getUser());
    }
}
