package com.minjeok4go.petplace.common.exception;

import com.minjeok4go.petplace.common.dto.ApiResponse;
import com.minjeok4go.petplace.common.dto.ErrorResponse;
import com.minjeok4go.petplace.hotel.exception.HotelNotFoundException;
import com.minjeok4go.petplace.hotel.exception.ReservationNotFoundException;
import com.minjeok4go.petplace.payment.exception.PaymentVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 🔥 IllegalArgumentException 처리 (회원가입 중복 에러 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("잘못된 요청: {} - {}", request.getRequestURI(), e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(e.getMessage()));
    }

    // Validation 오류 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        log.warn("Validation 오류: {}", e.getMessage());

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력 값이 올바르지 않습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(errorMessage));
    }

    // 일반적인 RuntimeException 처리
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request) {

        // Swagger 관련 요청은 제외
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/v3/api-docs") ||
                requestURI.contains("/swagger-ui") ||
                requestURI.contains("/swagger-resources")) {
            // Swagger 관련 예외는 다시 던져서 Spring이 처리하도록 함
            throw e;
        }

        log.error("런타임 오류 발생: ", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("요청 처리 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHotelNotFoundException(HotelNotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("HOTEL_NOT_FOUND")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFoundException(ReservationNotFoundException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("RESERVATION_NOT_FOUND")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<ErrorResponse> handlePaymentVerificationException(PaymentVerificationException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("PAYMENT_VERIFICATION_FAILED")
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



}
