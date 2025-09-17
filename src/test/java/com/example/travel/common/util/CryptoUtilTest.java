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

/**
 * CryptoUtilTest는 CryptoUtil 클래스의 기능을 검증하기 위한 테스트 클래스입니다.
 * UUID 암호화, 복호화 및 암호화 여부 검사를 포함한 주요 메서드들을 테스트합니다.
 */
@SpringBootTest
@TestPropertySource(properties = "uuid.crypto.secret=test-secret-key-for-uuid-32bytes")
class CryptoUtilTest {

    private static final Logger log = LogManager.getLogger(CryptoUtilTest.class);

    @Autowired
    private CryptoUtil cryptoUtil;

    /**
     * 무작위로 생성된 UUID의 암호화 및 복호화를 테스트합니다.
     * <br/>
     * 이 방법은 CryptoUtil 구성 요소가 UUID를 안전한 형식으로 올바르게 암호화한 다음 원래 값으로 복호화할 수 있는지 확인합니다.
     * 이 테스트는 암호화 및 복호화 프로세스의 일관성과 신뢰성을 보장합니다.
     * <br/>
     * 단계:
     * 1. 원래 값으로 임의의 UUID를 생성합니다.
     * 2. CryptoUtil의 encrypt 메서드를 사용하여 생성된 UUID를 암호화합니다.
     * 3. CryptoUtil의 decrypt 메서드를 사용하여 암호화된 UUID를 원래 값으로 복호화합니다.
     * 4. 복호화된 값이 원래 UUID와 일치하는지 확인합니다.
     * <br/>
     * 단언:
     * - 복호화된 UUID는 원래 무작위로 생성된 UUID와 일치해야 합니다.
     */
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

    /**
     * 암호화된 UUID 문자열의 유효성을 테스트합니다.
     * <br/>
     * 이 메서드에서는 무작위로 생성된 UUID를 암호화하고, 암호화된 문자열이
     * 유효한 상태인지 CryptoUtil의 isEncrypted 메서드를 사용하여 확인합니다.
     * <br/>
     * 테스트 목적:
     * - CryptoUtil 구성 요소가 UUID를 암호화한 후, 해당 암호화된 문자열이
     *   유효한지 올바르게 확인할 수 있는지 검증합니다.
     * <br/>
     * 테스트 과정:
     * 1. 무작위로 UUID를 생성합니다.
     * 2. 생성된 UUID를 CryptoUtil의 encrypt 메서드를 사용하여 암호화합니다.
     * 3. 암호화된 문자열을 isEncrypted 메서드에 전달하여 유효성을 확인합니다.
     * <br/>
     * 단언:
     * - 암호화된 문자열은 isEncrypted 메서드를 통해 유효한 것으로 판정되어야 합니다.
     */
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

    /**
     * {@code isEncrypted} 메서드를 테스트하여 잘못된 문자열 입력 사례를 확인합니다.
     * <br/>
     * 이 테스트는 {@link CryptoUtil#isEncrypted(String)} 메서드가
     * 암호화되지 않은 유효하지 않은 UUID 문자열을 올바르게 식별하는지 확인합니다.
     * <br/>
     * 테스트 단계:
     * 1. 하드코딩된 유효하지 않은 문자열이 입력으로 제공됩니다.
     * 2. {@link CryptoUtil#isEncrypted(String)}가 유효하지 않은 문자열과 함께 호출됩니다.
     * 3. 반환 값이 {@code false}라고 가정합니다. 이는 문자열이 유효한 암호화된 UUID가 아님을 나타냅니다.
     * <br/>
     * 단언:
     * - 이 메서드는 암호화되지 않고 유효하지 않은 UUID 문자열에 대해 {@code false}를 반환해야 합니다.
     */
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