package ai.duclo.scimtest.common.helper;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;

public class JwtTokenGenerator {

    //TODO 임시 키
    private final String SAMPLE_SECRET_KEY = "5eR$9kL#7tF@2zQwP8dS&6uV!3mH*1xYb";

    // JWT 토큰 생성 메서드
    public String generateToken(String subject, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        // 문자열 키를 byte[]로 변환하여 Key 객체 생성
        Key key = new SecretKeySpec(
                SAMPLE_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰 생성 메서드 (다중 클레임 지원)
    public String generateToken(String subject, long expirationMillis, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        // 문자열 키를 byte[]로 변환하여 Key 객체 생성
        Key key = new SecretKeySpec(
                SAMPLE_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate);

        // 클레임 추가
        if (claims != null && !claims.isEmpty()) {
            builder.addClaims(claims);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public static void main(String[] args) {
        JwtTokenGenerator tokenGenerator = new JwtTokenGenerator();

        // 여러 클레임 예시
        Map<String, Object> claims = new HashMap<>();
        claims.put("appId", "oktatestAppId");
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("loginTime", System.currentTimeMillis());

        String token = tokenGenerator.generateToken("OktaToken", 31536000000L, claims); // 1 hour expiration
        System.out.println("Generated JWT Token: " + token);
    }

}
