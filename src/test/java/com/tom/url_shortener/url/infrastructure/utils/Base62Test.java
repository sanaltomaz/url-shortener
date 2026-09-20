package com.tom.url_shortener.url.infrastructure.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base62Test {

    @Test
    @DisplayName("Deve codificar valor 0 para '0'")
    void shouldEncodeZero() {
        String encoded = Base62.encode(0L);
        assertThat(encoded).isEqualTo("0");
    }

    @Test
    @DisplayName("Deve decodificar '0' para 0L")
    void shouldDecodeZero() {
        long decoded = Base62.decode("0");
        assertThat(decoded).isZero();
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao tentar codificar número negativo")
    void shouldThrowExceptionWhenEncodingNegativeValue() {
        assertThatThrownBy(() -> Base62.encode(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be greater than or equal to zero");

        assertThatThrownBy(() -> Base62.encode(Long.MIN_VALUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value must be greater than or equal to zero");
    }

    @Test
    @DisplayName("Deve codificar e decodificar Long.MAX_VALUE com sucesso")
    void shouldEncodeAndDecodeLongMaxValue() {
        String encoded = Base62.encode(Long.MAX_VALUE);

        assertThat(encoded).isNotEmpty();
        assertThat(encoded.length()).isLessThanOrEqualTo(11);

        long decoded = Base62.decode(encoded);
        assertThat(decoded).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    @SuppressWarnings({"ConstantConditions", "DataFlowIssue"})
    @DisplayName("Deve lançar IllegalArgumentException ao tentar decodificar string nula")
    void shouldThrowExceptionWhenDecodingNull() {
        assertThatThrownBy(() -> Base62.decode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("String is null");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc@123", "hello world", "test-code", "a_b", "xyz#", "123$"})
    @DisplayName("Deve lançar IllegalArgumentException ao tentar decodificar string com caracteres fora do alfabeto Base62")
    void shouldThrowExceptionWhenDecodingInvalidCharacters(String invalidInput) {
        assertThatThrownBy(() -> Base62.decode(invalidInput))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Invalid character:");
    }

    @ParameterizedTest
    @ValueSource(longs = {
            0L,
            1L,
            9L,
            10L,
            35L,
            36L,
            61L,
            62L,
            63L,
            1000L,
            123456789L,
            9876543210L,
            Long.MAX_VALUE - 1,
            Long.MAX_VALUE
    })
    @DisplayName("Deve garantir consistência na conversão ida-e-volta (encode -> decode)")
    void shouldEnsureRoundTripConsistency(long originalValue) {
        String encoded = Base62.encode(originalValue);
        long decoded = Base62.decode(encoded);

        assertThat(decoded).isEqualTo(originalValue);
    }

    @Test
    @DisplayName("Deve garantir consistência ida-e-volta para valores positivos aleatórios")
    void shouldEnsureRoundTripConsistencyForRandomValues() {
        Random random = new Random(42);

        for (int i = 0; i < 1000; i++) {
            long randomPositive = random.nextLong(1, Long.MAX_VALUE);
            String encoded = Base62.encode(randomPositive);
            long decoded = Base62.decode(encoded);

            assertThat(decoded).isEqualTo(randomPositive);
        }
    }
}
