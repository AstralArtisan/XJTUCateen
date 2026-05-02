package com.xjtu.canteen;

import com.xjtu.canteen.security.PasswordUtil;
import com.xjtu.canteen.security.TokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilTest extends CanteenTestBase {
    @Autowired
    private TokenUtil tokenUtil;

    @Test
    void tokenRoundTripPreservesUserAndRole() {
        String token = tokenUtil.createToken(42L, 1);
        Map<String, Object> payload = tokenUtil.parseToken(token);

        assertThat(payload).isNotNull();
        assertThat(payload.get("user_id")).isEqualTo(42);
        assertThat(payload.get("role")).isEqualTo(1);
        assertThat(((Number) payload.get("exp")).longValue()).isGreaterThan(Instant.now().getEpochSecond());
    }

    @Test
    void invalidTokensAreRejected() {
        String token = tokenUtil.createToken(1L, 0);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(tokenUtil.parseToken(tampered)).isNull();
        assertThat(tokenUtil.parseToken("not.a.valid.token")).isNull();
        assertThat(tokenUtil.parseToken("")).isNull();
    }

    @Test
    void passwordHashVerifiesOnlyCorrectPassword() {
        String first = PasswordUtil.hashPassword("samepassword");
        String second = PasswordUtil.hashPassword("samepassword");

        assertThat(first).isNotEqualTo("samepassword");
        assertThat(first).isNotEqualTo(second);
        assertThat(PasswordUtil.verifyPassword("samepassword", first)).isTrue();
        assertThat(PasswordUtil.verifyPassword("samepassword", second)).isTrue();
        assertThat(PasswordUtil.verifyPassword("wrongpassword", first)).isFalse();
    }
}
