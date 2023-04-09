package com.feiwin.imserver.config;

import com.feiwin.imserver.handler.WebSocketHandler;
import com.feiwin.imserver.interceptor.WebSocketAuthInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Log4j2
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    private WebSocketAuthInterceptor webSocketAuthInterceptor;
    @Resource
    private WebSocketHandler webSocketGroupHandler;

    @Override
    public void registerWebSocketHandlers( WebSocketHandlerRegistry registry ) {
        registry.addHandler( webSocketGroupHandler, "/im" )
                .addInterceptors( webSocketAuthInterceptor )
                .setAllowedOrigins( "*" );
    }
}