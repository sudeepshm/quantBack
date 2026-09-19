package com.quantback.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarketDataValidatorTest {

    private MarketDataValidator validator;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        validator = new MarketDataValidator();
        baseTime = LocalDateTime.of(2026, 1, 1, 9, 15);
    }

    @Test
    @DisplayName("Valid list of candles passes validation")
    void validCandlesPass() {
        List<Candle> candles = List.of(
                new Candle(baseTime, new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), 100),
                new Candle(baseTime.plusMinutes(5), new BigDecimal("102"), new BigDecimal("108"), new BigDecimal("101"), new BigDecimal("107"), 200)
        );

        assertThatCode(() -> validator.validate(candles)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Throws exception on null or empty candles")
    void throwsOnEmptyOrNull() {
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> validator.validate(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("Throws exception when High is less than Open/Close/Low")
    void throwsOnInvalidHigh() {
        List<Candle> invalidHigh = List.of(
                new Candle(baseTime, new BigDecimal("100"), new BigDecimal("90"), new BigDecimal("85"), new BigDecimal("95"), 100)
        );
        assertThatThrownBy(() -> validator.validate(invalidHigh))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("High");
    }

    @Test
    @DisplayName("Throws exception when Low is greater than Open/Close")
    void throwsOnInvalidLow() {
        List<Candle> invalidLow = List.of(
                new Candle(baseTime, new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("102"), new BigDecimal("99"), 100)
        );
        assertThatThrownBy(() -> validator.validate(invalidLow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Low");
    }

    @Test
    @DisplayName("Throws exception on duplicate timestamps")
    void throwsOnDuplicateTimestamps() {
        List<Candle> duplicates = List.of(
                new Candle(baseTime, new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), 100),
                new Candle(baseTime, new BigDecimal("102"), new BigDecimal("108"), new BigDecimal("101"), new BigDecimal("107"), 200)
        );

        assertThatThrownBy(() -> validator.validate(duplicates))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate timestamp");
    }

    @Test
    @DisplayName("Throws exception when candles are not chronological")
    void throwsOnNonChronologicalOrder() {
        List<Candle> outOfOrder = List.of(
                new Candle(baseTime.plusMinutes(10), new BigDecimal("100"), new BigDecimal("105"), new BigDecimal("95"), new BigDecimal("102"), 100),
                new Candle(baseTime, new BigDecimal("102"), new BigDecimal("108"), new BigDecimal("101"), new BigDecimal("107"), 200)
        );

        assertThatThrownBy(() -> validator.validate(outOfOrder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not chronologically ordered");
    }
}
