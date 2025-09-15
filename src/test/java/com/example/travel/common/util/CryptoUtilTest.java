package com.example.travel.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "uuid.crypto.secret=test-secret-key-for-uuid-32bytes")
class CryptoUtilTest {

    private static final Logger log = LogManager.getLogger(CryptoUtilTest.class);

    @Autowired
    private CryptoUtil cryptoUtil;

    @Test
    @DisplayName("UUID 암호화 및 복호화 테스트")
    void encrypt() {
        // given
        String originalUuid = UUID.randomUUID().toString();
        log.debug("originalUuid: {}", originalUuid);
        // when
        String encryptedUuid = cryptoUtil.encrypt(originalUuid);
        log.debug("encryptedUuid: {}", encryptedUuid);

        String decryptedUuid = cryptoUtil.decrypt(encryptedUuid);
        log.debug("decryptedUuid: {}", encryptedUuid);
        // then
        assertThat(decryptedUuid).isEqualTo(originalUuid);
    }

    @Test
    @DisplayName("유효한 암호화 문자열 확인 테스트")
    void decrypt() {
        // given
        String originalUuid = UUID.randomUUID().toString();
        log.debug("originalUuid: {}", originalUuid);
        String encryptedUuid = cryptoUtil.encrypt(originalUuid);
        log.debug("decryptedUuid: {}", encryptedUuid);

        // when & then
        assertThat(cryptoUtil.isEncrypted(encryptedUuid)).isTrue();
        log.debug("cryptoUtil.isEncrypted(encryptedUuid): {}",
            cryptoUtil.isEncrypted(encryptedUuid));
    }

    @Test
    @DisplayName("유효하지 않은 문자열 확인 테스트")
    void isEncrypted() {
        // given
        String invalidString = "invalid-uuid-string";

        // when & then
        assertThat(cryptoUtil.isEncrypted(invalidString)).isFalse();
        log.debug("cryptoUtil.isEncrypted(encryptedUuid): {}",
            cryptoUtil.isEncrypted(invalidString));
    }
}