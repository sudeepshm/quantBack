package com.quantback.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads and parses market data from CSV files or input streams.
 */
public class CsvMarketDataLoader implements MarketDataLoader {

    private final MarketDataValidator validator;

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    private static final List<DateTimeFormatter> DATE_ONLY_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    public CsvMarketDataLoader() {
        this(new MarketDataValidator());
    }

    public CsvMarketDataLoader(MarketDataValidator validator) {
        this.validator = Objects.requireNonNull(validator, "Validator cannot be null");
    }

    @Override
    public MarketData load(String symbol, Path filePath) throws IOException {
        Objects.requireNonNull(filePath, "File path cannot be null");
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Market data file does not exist: " + filePath);
        }
        try (InputStream in = Files.newInputStream(filePath)) {
            return load(symbol, in);
        }
    }

    @Override
    public MarketData load(String symbol, InputStream inputStream) throws IOException {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(inputStream, "InputStream cannot be null");

        List<Candle> candles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String headerLine = null;
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    headerLine = trimmed;
                    break;
                }
            }

            if (headerLine == null) {
                throw new IllegalArgumentException("CSV content is empty");
            }

            Map<String, Integer> columnIndexMap = parseHeader(headerLine);
            int timestampIdx = getRequiredColumnIndex(columnIndexMap, "timestamp", "datetime", "date");
            int openIdx = getRequiredColumnIndex(columnIndexMap, "open");
            int highIdx = getRequiredColumnIndex(columnIndexMap, "high");
            int lowIdx = getRequiredColumnIndex(columnIndexMap, "low");
            int closeIdx = getRequiredColumnIndex(columnIndexMap, "close");
            Integer volumeIdx = getOptionalColumnIndex(columnIndexMap, "volume", "vol");

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                String[] tokens = trimmed.split(",");
                int maxRequiredIdx = Math.max(Math.max(timestampIdx, openIdx),
                        Math.max(Math.max(highIdx, lowIdx), closeIdx));

                if (volumeIdx != null) {
                    maxRequiredIdx = Math.max(maxRequiredIdx, volumeIdx);
                }

                if (tokens.length <= maxRequiredIdx) {
                    throw new IllegalArgumentException(
                            String.format("Malformed CSV line %d: expected at least %d columns, found %d",
                                    lineNumber, maxRequiredIdx + 1, tokens.length)
                    );
                }

                LocalDateTime timestamp = parseTimestamp(tokens[timestampIdx].trim());
                BigDecimal open = new BigDecimal(tokens[openIdx].trim());
                BigDecimal high = new BigDecimal(tokens[highIdx].trim());
                BigDecimal low = new BigDecimal(tokens[lowIdx].trim());
                BigDecimal close = new BigDecimal(tokens[closeIdx].trim());

                long volume = 0;
                if (volumeIdx != null && volumeIdx < tokens.length) {
                    String volStr = tokens[volumeIdx].trim();
                    if (!volStr.isEmpty()) {
                        // Handle potential floating volume representation like 125000.0
                        if (volStr.contains(".")) {
                            volume = (long) Double.parseDouble(volStr);
                        } else {
                            volume = Long.parseLong(volStr);
                        }
                    }
                }

                candles.add(new Candle(timestamp, open, high, low, close, volume));
            }
        }

        validator.validate(candles);
        return new MarketData(symbol, candles);
    }

    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim().toLowerCase(), i);
        }
        return map;
    }

    private int getRequiredColumnIndex(Map<String, Integer> headerMap, String... names) {
        for (String name : names) {
            Integer idx = headerMap.get(name.toLowerCase());
            if (idx != null) {
                return idx;
            }
        }
        throw new IllegalArgumentException("Missing required column in CSV header: " + String.join(" / ", names));
    }

    private Integer getOptionalColumnIndex(Map<String, Integer> headerMap, String... names) {
        for (String name : names) {
            Integer idx = headerMap.get(name.toLowerCase());
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private LocalDateTime parseTimestamp(String text) {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        for (DateTimeFormatter formatter : DATE_ONLY_FORMATTERS) {
            try {
                return LocalDate.parse(text, formatter).atStartOfDay();
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Unsupported timestamp format: " + text);
    }
}
