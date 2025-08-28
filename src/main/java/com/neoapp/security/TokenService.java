package com.neoapp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.neoapp.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class TokenService {
    @Value("${auth.token}")
    private String secretKey;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create().withIssuer("neoapp").withSubject(user.getEmail()).withExpiresAt(generateExpirationDate()).sign(algorithm);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating token: " + e);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.require(algorithm).withIssuer("neoapp").build().verify(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public Date generateExpirationDate() {
        return Date.from(LocalDateTime.now().plusHours(24).toInstant(ZoneOffset.UTC));
    }
}
