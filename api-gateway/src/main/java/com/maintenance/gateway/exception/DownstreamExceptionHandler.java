package com.maintenance.gateway.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Map;

/** Translates connection failures to downstream services into a clear 503. */
@RestControllerAdvice
public class DownstreamExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DownstreamExceptionHandler.class);

    @ExceptionHandler({ResourceAccessException.class, IOException.class})
    ResponseEntity<Map<String, Object>> downstreamUnavailable(Exception ex, HttpServletRequest request) {
        log.warn("Downstream call failed for {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", "Downstream microservice is not yet deployed",
                        "path", request.getRequestURI()));
    }
}
