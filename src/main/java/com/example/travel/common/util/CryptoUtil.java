package com.example.travel.common.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtil {

    private static final Logger log = LogManager.getLogger(CryptoUtil.class);
    @Value("${uuid.crypto.secret:travel-app-uuid-secret-key-32}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * UUID를 암호화합니다.
     * @param uuid 암호화할 UUID
     * @return 암호화된 UUID (Base64 인코딩)
     */
    public String encrypt(String uuid) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encryptedBytes = cipher.doFinal(uuid.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("UUID 암호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 암호화된 UUID를 복호화합니다.
     * @param encryptedUuid 암호화된 UUID (Base64 인코딩)
     * @return 복호화된 UUID
     */
    public String decrypt(String encryptedUuid) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encryptedUuid);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("UUID 복호화 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 문자열이 암호화된 UUID인지 확인합니다.
     * @param str 확인할 문자열
     * @return 암호화된 UUID 여부
     */
    public boolean isEncrypted(String str) {
        try {
            // Base64 형식인지 확인
            Base64.getUrlDecoder().decode(str);
            // 복호화 시도
            decrypt(str);
            return true;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
    }
}
