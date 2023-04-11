package com.feiwin.imserver.interceptor;

import com.feiwin.imserver.constant.Constants;
import com.feiwin.imserver.service.TokenService;
import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNullApi;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

/**
 * @author meng.jun
 */
@Log4j2
@Component
@NonNullApi
public class WebSocketAuthInterceptor implements HandshakeInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    @Override
    public boolean beforeHandshake( ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                    Map<String, Object> attributes ) {
        try {
            String token = ( ( ServletServerHttpRequest ) request ).getServletRequest().getHeader( Constants.WEBSOCKET_PROTOCOL );
            if ( !StringUtils.hasText( token ) ) {
                log.error( "There is no token!" );
                return false;
            }

            // 开始鉴权
            Claims claims   = tokenService.parseToken( token );
            String username = ( String ) claims.get( Constants.USERNAME );
            if ( !StringUtils.hasText( username ) ) {
                log.error( "There is no username!" );
                return false;
            }

            Object resToken = stringRedisTemplate.opsForValue().get( Constants.USER_JJWT_KEY + username );
            if ( resToken == null || !resToken.equals( token ) ) {
                log.error( "token不匹配,username:{}", username );
                return false;
            }
            log.info( "会员{}连接上线", username );

            attributes.put( Constants.USERNAME, username );

            ( ( ServletServerHttpResponse ) response ).getServletResponse().setHeader( Constants.WEBSOCKET_PROTOCOL, token );
            return true;
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return false;
    }

    @Override
    public void afterHandshake( ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                Exception exception ) {

    }
}
