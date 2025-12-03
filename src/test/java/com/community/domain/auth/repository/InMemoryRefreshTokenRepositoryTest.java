package com.community.domain.auth.repository;

import com.community.domain.auth.model.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRefreshTokenRepositoryTest {

    private final RefreshTokenRepository repository = new InMemoryRefreshTokenRepository();

    @Test
    @DisplayName("save 후 find 로 동일한 리프레시 토큰을 조회할 수 있다.")
    void save_and_find() {
        //given
        RefreshToken refreshToken = new RefreshToken("token-value", 10L, Instant.now().plusSeconds(3600));

        //when
        repository.save(refreshToken);
        Optional<RefreshToken> found = repository.find("token-value");

        //then
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(10L);
        assertThat(found.get().getExpiresAt()).isEqualTo(refreshToken.getExpiresAt());
    }

    @Test
    @DisplayName("delete 호출 시 저장소에서 리프레시 토큰을 제거한다.")
    void delete_() {
        //given
        RefreshToken refreshToken = new RefreshToken("token-to-delete", 1L, Instant.now().plusSeconds(60));
        repository.save(refreshToken);

        //when
        repository.delete("token-to-delete");

        //then
        assertThat(repository.find("token-to-delete")).isEmpty();
    }
}
