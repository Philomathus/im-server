package com.feiwin.imserver.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Map;
import java.util.Objects;

@Service
public class TokenService {

    private static Key KEY_SECRET;

    @Value( "${jjwt.secret}" )
    private void setKeySecret( String secret ) {
        TokenService.KEY_SECRET = Keys.hmacShaKeyFor( secret.getBytes( StandardCharsets.UTF_8 ) );
    }

    public String createToken( Map<String, String> claims ) {
        return Jwts.builder().setClaims( claims ).signWith( KEY_SECRET ).compact();
    }

    public Claims parseToken( String token ) {
        return Jwts.parserBuilder().setSigningKey( KEY_SECRET ).build().parseClaimsJws( token ).getBody();
    }

    public String getClaimsValueFromToken(String key, String token) {
        return Objects.toString(parseToken(token).get(key), null);
    }
}

