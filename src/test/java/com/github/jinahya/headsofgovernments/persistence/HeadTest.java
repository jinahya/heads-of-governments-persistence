package com.github.jinahya.headsofgovernments.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
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

    @DisplayName("equals/hashCode")
    @Nested
    class EqualsTest {

        private static Office of(final String country) throws ReflectiveOperationException {
            final var office = new Office();
            final var field = Office.class.getDeclaredField("country");
            field.setAccessible(true);
            field.set(office, country);
            return office;
        }

        @Test
        void __() throws ReflectiveOperationException {
            EqualsVerifier.forClass(Head.class)
                    .suppress(Warning.SURROGATE_KEY)
                    .withPrefabValues(Office.class, of("FR"), of("GB"))
                    .verify();
        }
    }
}
