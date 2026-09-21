package com.quantback.api.controller;

import com.quantback.api.dto.BacktestRequestDto;
import com.quantback.api.dto.BacktestResultDto;
import com.quantback.api.service.BacktestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST API controller exposing backtest endpoints.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BacktestController {

    private final BacktestService backtestService;
    private final ObjectMapper objectMapper;

    public BacktestController(BacktestService backtestService, ObjectMapper objectMapper) {
        this.backtestService = backtestService;
        this.objectMapper = objectMapper;
    }

    /**
     * POST /api/backtests
     * Accepts multipart form with JSON params and optional CSV file.
     */
    @PostMapping(value = "/backtests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> runBacktest(
            @RequestPart("params") String paramsJson,
            @RequestPart(value = "file", required = false) MultipartFile csvFile) {

        try {
            BacktestRequestDto req = objectMapper.readValue(paramsJson, BacktestRequestDto.class);
            BacktestResultDto result = backtestService.runBacktest(req, csvFile);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/health
     * Simple health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "QuantBack API",
                "version", "1.0"
        ));
    }

    /**
     * GET /api/strategies
     * Returns available strategies and their parameters.
     */
    @GetMapping("/strategies")
    public ResponseEntity<?> getStrategies() {
        return ResponseEntity.ok(java.util.List.of(
                Map.of(
                        "id", "moving-average",
                        "name", "Moving Average Crossover",
                        "description", "Buys when fast MA crosses above slow MA; sells when it crosses below",
                        "params", java.util.List.of(
                                Map.of("name", "fastPeriod", "label", "Fast MA Period", "default", 20, "min", 1),
                                Map.of("name", "slowPeriod", "label", "Slow MA Period", "default", 50, "min", 2)
                        )
                )
        ));
    }
}
