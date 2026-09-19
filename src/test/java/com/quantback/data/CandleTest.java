package com.quantback.data;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CandleTest {

    @Test
    @DisplayName("Successfully creates candle with valid attributes")
    void createsValidCandle() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 9, 15);
        Candle candle = new Candle(
                timestamp,
                new BigDecimal("21700.00"),
                new BigDecimal("21850.00"),
                new BigDecimal("21650.00"),
                new BigDecimal("21800.00"),
                125000L
        );

        assertThat(candle.getTimestamp()).isEqualTo(timestamp);
        assertThat(candle.getOpen()).isEqualTo(new BigDecimal("21700.00"));
        assertThat(candle.getHigh()).isEqualTo(new BigDecimal("21850.00"));
        assertThat(candle.getLow()).isEqualTo(new BigDecimal("21650.00"));
        assertThat(candle.getClose()).isEqualTo(new BigDecimal("21800.00"));
        assertThat(candle.getVolume()).isEqualTo(125000L);
    }

    @Test
    @DisplayName("Throws NullPointerException when required fields are null")
    void throwsOnNullFields() {
        LocalDateTime ts = LocalDateTime.now();
        BigDecimal price = new BigDecimal("100.00");

        assertThatThrownBy(() -> new Candle(null, price, price, price, price, 100))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Candle(ts, null, price, price, price, 100))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Candle(ts, price, null, price, price, 100))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Candle(ts, price, price, null, price, 100))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Candle(ts, price, price, price, null, 100))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Throws IllegalArgumentException when volume is negative")
    void throwsOnNegativeVolume() {
        LocalDateTime ts = LocalDateTime.now();
        BigDecimal price = new BigDecimal("100.00");

        assertThatThrownBy(() -> new Candle(ts, price, price, price, price, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Volume cannot be negative");
    }

    @Test
    @DisplayName("Candles with identical properties are equal")
    void testEquality() {
        LocalDateTime ts = LocalDateTime.of(2026, 1, 1, 9, 15);
        Candle c1 = new Candle(ts, new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), 500);
        Candle c2 = new Candle(ts, new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), 500);

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }
}
