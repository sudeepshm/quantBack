package com.quantback.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvMarketDataLoaderTest {

    private CsvMarketDataLoader loader;

    @BeforeEach
    void setUp() {
        loader = new CsvMarketDataLoader();
    }

    @Test
    @DisplayName("Successfully parses valid CSV market data from stream")
    void loadsValidCsv() throws IOException {
        String csv = """
                timestamp,open,high,low,close,volume
                2026-01-01T09:15:00,100.0,105.0,98.0,104.0,5000
                2026-01-02T09:15:00,104.0,108.0,103.0,107.0,6000
                """;

        MarketData data = loader.load("TEST", new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(data.getSymbol()).isEqualTo("TEST");
        assertThat(data.size()).isEqualTo(2);
        assertThat(data.get(0).getOpen()).isEqualTo(new BigDecimal("100.0"));
        assertThat(data.get(0).getClose()).isEqualTo(new BigDecimal("104.0"));
        assertThat(data.get(1).getVolume()).isEqualTo(6000L);
    }

    @Test
    @DisplayName("Successfully loads sample NIFTY data file from disk")
    void loadsSampleFile() throws IOException {
        Path path = Paths.get("src/main/resources/data/sample_nifty.csv");
        MarketData data = loader.load("NIFTY", path);

        assertThat(data.getSymbol()).isEqualTo("NIFTY");
        assertThat(data.size()).isEqualTo(22);
        assertThat(data.get(0).getOpen()).isEqualTo(new BigDecimal("21700.00"));
        assertThat(data.getLatestCandle().getClose()).isEqualTo(new BigDecimal("22180.00"));
    }

    @Test
    @DisplayName("Throws exception on missing required columns")
    void throwsOnMissingColumns() {
        String csv = """
                timestamp,open,close,volume
                2026-01-01T09:15:00,100.0,104.0,5000
                """;

        assertThatThrownBy(() -> loader.load("TEST", new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required column");
    }

    @Test
    @DisplayName("Skips comments and blank lines cleanly")
    void skipsCommentsAndBlankLines() throws IOException {
        String csv = """
                # Daily OHLCV data
                timestamp,open,high,low,close,volume

                2026-01-01T09:15:00,100.0,105.0,98.0,104.0,5000
                # End of day 1

                2026-01-02T09:15:00,104.0,108.0,103.0,107.0,6000
                """;

        MarketData data = loader.load("TEST", new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        assertThat(data.size()).isEqualTo(2);
    }
}
