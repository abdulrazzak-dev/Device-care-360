package com.devicecare360.auth.exception;

import com.devicecare360.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        log.warn("[CorrelationId: {}] Validation or conflict on path {}: {}", 
                correlationId, request.getRequestURI(), ex.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();
        if (message != null && (message.contains("already taken") || message.contains("already in use") || message.contains("already registered"))) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        String msg = ex.getBindingResult().getFieldError() != null 
                ? ex.getBindingResult().getFieldError().getDefaultMessage() 
                : "Validation error";
        log.warn("[CorrelationId: {}] MethodArgumentNotValidException on path {}: {}", correlationId, request.getRequestURI(), msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }

    @ExceptionHandler({ConnectException.class, AmqpConnectException.class, ResourceAccessException.class})
    public ResponseEntity<ApiResponse<Void>> handleConnectionFailure(Exception ex, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        log.error("[CorrelationId: {}] Downstream connection failure on path {}: {}", 
                correlationId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Registration is temporarily unavailable. Please try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        log.error("[CorrelationId: {}] Internal error processing request on path {}: {}", 
                correlationId, request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}
