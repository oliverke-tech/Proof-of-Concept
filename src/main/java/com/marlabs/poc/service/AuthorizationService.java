package com.marlabs.poc.service;

import static com.marlabs.poc.constants.TokenConstants.RSA_KEY_BASE_SIZE;
import static com.marlabs.poc.constants.TokenConstants.TOKEN_FORMAT;
import static com.marlabs.poc.constants.TokenConstants.TOKEN_HEADER;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

@Service
public class AuthorizationService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public AuthorizationService() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(RSA_KEY_BASE_SIZE);
        KeyPair keyPair = keyGenerator.genKeyPair();
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        algorithm = Algorithm.RSA256(rsaPublicKey, rsaPrivateKey);
        verifier = JWT.require(algorithm)
                .build();
    }

    public String getToken() {
        try {
            return JWT.create()
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            return null;
        }
    }

    public String verifyToken(@RequestHeader HttpHeaders headers) {
        String token = headers.getFirst(TOKEN_HEADER);
        if (token == null || token.isEmpty()) {
            return "No token found!";
        }
        if (!token.contains(TOKEN_FORMAT)) {
            return "Wrong token format!";
        }
        token = token.substring(TOKEN_FORMAT.length());

        try {
            verifier.verify(token);
        } catch (JWTVerificationException exception) {
            return "Token is expired or invalid!";
        }

        return null;
    }
}
