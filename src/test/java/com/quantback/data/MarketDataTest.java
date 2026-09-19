package com.quantback.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarketDataTest {

    private List<Candle> sampleCandles;
    private MarketData marketData;

    @BeforeEach
    void setUp() {
        sampleCandles = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 1, 9, 15);
        for (int i = 0; i < 5; i++) {
            sampleCandles.add(new Candle(
                    baseTime.plusDays(i),
                    new BigDecimal("100.00").add(BigDecimal.valueOf(i)),
                    new BigDecimal("105.00").add(BigDecimal.valueOf(i)),
                    new BigDecimal("95.00").add(BigDecimal.valueOf(i)),
                    new BigDecimal("102.00").add(BigDecimal.valueOf(i)),
                    1000L + i * 100
            ));
        }
        marketData = new MarketData("TEST_SYM", sampleCandles);
    }

    @Test
    @DisplayName("MarketData preserves candles and reports correct size")
    void testBasicProperties() {
        assertThat(marketData.getSymbol()).isEqualTo("TEST_SYM");
        assertThat(marketData.size()).isEqualTo(5);
        assertThat(marketData.isEmpty()).isFalse();
        assertThat(marketData.get(0)).isEqualTo(sampleCandles.get(0));
        assertThat(marketData.getLatestCandle()).isEqualTo(sampleCandles.get(4));
    }

    @Test
    @DisplayName("MarketData creates defensive copy and is immutable")
    void testImmutability() {
        List<Candle> retrieved = marketData.getCandles();
        assertThatThrownBy(() -> retrieved.add(sampleCandles.get(0)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("slice(T) prevents look-ahead bias by only exposing candles up to T")
    void testSliceNoLookAheadBias() {
        MarketData sliced = marketData.slice(2);

        assertThat(sliced.size()).isEqualTo(3); // indices 0, 1, 2
        assertThat(sliced.getSymbol()).isEqualTo("TEST_SYM");
        assertThat(sliced.getLatestCandle()).isEqualTo(sampleCandles.get(2));
        assertThat(sliced.getCandles()).containsExactly(
                sampleCandles.get(0),
                sampleCandles.get(1),
                sampleCandles.get(2)
        );
    }

    @Test
    @DisplayName("slice with out-of-bounds index throws IndexOutOfBoundsException")
    void testSliceOutOfBounds() {
        assertThatThrownBy(() -> marketData.slice(-1))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> marketData.slice(5))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }
}
