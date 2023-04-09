package com.feiwin.imserver.controller;

import com.feiwin.imserver.constant.Constants;
import com.feiwin.imserver.dto.TokenAuth;
import io.jsonwebtoken.lang.Maps;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.feiwin.imserver.service.TokenService;
import java.time.Duration;

@RestController
@RequestMapping( "/token" )
@Log4j2
public class TokenController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private TokenService tokenService;

    @PostMapping("/auth")
    public String auth(@RequestBody TokenAuth tokenAuth) {
        String token = tokenService.createToken( Maps.of( Constants.MEMBER_ID, tokenAuth.getMemberId() )
                                                     .and( Constants.USERNAME, tokenAuth.getUsername() ).build() );
        stringRedisTemplate.opsForValue()
                .set( Constants.USER_JJWT_KEY + tokenAuth.getMemberId(), token, Duration.ofDays( 3 ) );
        return token;
    }
}
