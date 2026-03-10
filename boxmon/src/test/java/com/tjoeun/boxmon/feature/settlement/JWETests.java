package com.tjoeun.boxmon.feature.settlement;

import com.tjoeun.boxmon.feature.settlement.util.TossJweCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class JWETests {

    private TossJweCrypto tossJweCrypto;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        tossJweCrypto = new TossJweCrypto("9f4c2a7d8e1b3c6fa0d5e7b91234c8de55aa01ff23bc4498d7610e2c9ab47f31", objectMapper); //테스트용 가짜키
    }
    
    @Test
    void testDirect(){
        Map<String,Object> map = Map.of("test","testStr");
        String encrypted = tossJweCrypto.encryptToCompactJwe(map);
        String decrypted = tossJweCrypto.decryptCompactJwe(encrypted, Map.class).toString();
        
        System.out.println(decrypted);
    }
}
