package com.feiwin.imserver.security;

import com.feiwin.imserver.constant.Constants;
import com.feiwin.imserver.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class JwtFilter extends GenericFilterBean {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String token = request.getHeader(Constants.AUTHORIZATION);
        Claims claims   = tokenService.parseToken( token );
        String memberId = ( String ) claims.get( Constants.MEMBER_ID );

        String resToken = stringRedisTemplate.opsForValue().get( Constants.USER_JJWT_KEY + memberId );

        if(!StringUtils.equals(token, resToken)) {
            throw new ServletException("The token is invalid!");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
