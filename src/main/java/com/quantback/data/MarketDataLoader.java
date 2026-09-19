package com.quantback.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Interface defining how market data is loaded from a source.
 */
public interface MarketDataLoader {

    /**
     * Loads market data from a file path.
     *
     * @param symbol financial symbol/ticker
     * @param filePath path to the data file
     * @return MarketData containing loaded and validated candles
     * @throws IOException if an I/O error occurs
     */
    MarketData load(String symbol, Path filePath) throws IOException;

    /**
     * Loads market data from an input stream.
     *
     * @param symbol financial symbol/ticker
     * @param inputStream stream containing the raw data
     * @return MarketData containing loaded and validated candles
     * @throws IOException if an I/O error occurs
     */
    MarketData load(String symbol, InputStream inputStream) throws IOException;
}
