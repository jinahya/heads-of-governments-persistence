package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class HeadTest {

    @DisplayName("toString")
    @Nested
    class ToStringTest {

        @Test
        void _NotBlank_() {
            final var string = new Head().toString();
            log.debug("string: {}", string);
            assertThat(string).isNotBlank();
        }
    }
}
