package com.ssafy.core.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class MessageUtil {

    @Value("${nurigo.api.key}")
    private String publicKey;

    @Value("${nurigo.api.secretKey}")
    private String secretKey;

    @Value("${nurigo.api.sender}")
    private String sender;

    public String getHeaders() {
        try {
            String apiKey = publicKey;
            String apiSecret = secretKey;
            String salt = UUID.randomUUID().toString().replaceAll("-", "");
            String date = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString().split("\\[")[0];

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String signature = new String(Hex.encodeHex(sha256_HMAC.doFinal((date + salt).getBytes(StandardCharsets.UTF_8))));
            return "HMAC-SHA256 apiKey=" + apiKey + ", date=" + date + ", salt=" + salt + ", signature=" + signature;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
