package com.tjoeun.boxmon.feature.settlement.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.tjoeun.boxmon.feature.settlement.exception.JweDecryptException;
import com.tjoeun.boxmon.feature.settlement.exception.JweEncryptException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class TossJweCrypto {
    private final byte[] securityKey;
    private final ObjectMapper objectMapper;

    public TossJweCrypto(@Value("${toss-api-security-key}") String securityKey, ObjectMapper objectMapper){
        //Hex to Bytes
        this.securityKey = HexFormat.of().parseHex(securityKey);
        this.objectMapper = objectMapper;
    }

    public String encryptToCompactJwe(Map<String,Object> map) {
        try {
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .customParam("iat", OffsetDateTime.now(ZoneId.of("Asia/Seoul")).toString())
                    .customParam("nonce", UUID.randomUUID().toString())
                    .build();

            String payload = objectMapper.writeValueAsString(map);
            JWEObject jwe = new JWEObject(header, new Payload(payload));
            jwe.encrypt(new DirectEncrypter(securityKey));
            return jwe.serialize(); // compact JWE string
        }
        catch (JOSEException e){
            throw new JweEncryptException("JWE 암호화 중 문제가 발생했습니다.",e);
        }
    }

    public <T> T decryptCompactJwe(String compactJwe, Class<T> responseType) {
        try {
            JWEObject jwe = JWEObject.parse(compactJwe);
            jwe.decrypt(new DirectDecrypter(securityKey));
            String json = jwe.getPayload().toString(); // decrypted JSON string
            return objectMapper.readValue(json, responseType);
        } catch (Exception e) {
            log.error("JWE 복호화 실패. 키 길이: {}, jwe 앞 5글자: {}", compactJwe.length(), compactJwe.substring(0, 5));
            throw new JweDecryptException("JWE 복호화 중 문제가 발생했습니다.",e);
        }
    }
}
