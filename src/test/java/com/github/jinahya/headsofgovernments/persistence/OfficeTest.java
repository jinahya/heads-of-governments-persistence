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
class OfficeTest {

    @DisplayName("toString")
    @Nested
    class ToStringTest {

        @Test
        void _NotBlank_() {
            final var string = new Office().toString();
            log.debug("string: {}", string);
            assertThat(string).isNotBlank();
        }
    }

    @DisplayName("equals/hashCode")
    @Nested
    class EqualsTest {

        private static Head of(final Long id) throws ReflectiveOperationException {
            final var head = new Head();
            final var field = Head.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(head, id);
            return head;
        }

        @Test
        void __() throws ReflectiveOperationException {
            EqualsVerifier.forClass(Office.class)
                    .suppress(Warning.SURROGATE_KEY)
                    .withPrefabValues(Head.class, of(1L), of(2L))
                    .verify();
        }
    }
}
