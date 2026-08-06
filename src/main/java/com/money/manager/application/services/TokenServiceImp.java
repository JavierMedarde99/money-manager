package com.money.manager.application.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.money.manager.domain.User;
import com.money.manager.domain.services.TokenService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenServiceImp implements TokenService {

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Override
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        User currentUser = (User) authentication.getPrincipal();

        JwtClaimsSet claims = JwtClaimsSet.builder().subject(currentUser.getUsername()).issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.MINUTES)).build();
        
        var jwtEncoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);

        return jwtEncoder.encode(jwtEncoderParameters).getTokenValue();
    }

    @Override
    public String getUserFromToken(String token) {
        Jwt jwtToken = jwtDecoder.decode(token);
        return jwtToken.getSubject();

    }

    @Override
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            throw new BadJwtException("Error while trying to validate token");
        }
    }

}
