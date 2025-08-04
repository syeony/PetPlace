//package com.minjeok4go.petplace.common.exception;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    // Validation 오류 처리
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidationException(
//            MethodArgumentNotValidException e,
//            HttpServletRequest request) {
//
//        log.warn("Validation 오류: {}", e.getMessage());
//
//        String errorMessage = e.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .findFirst()
//                .map(error -> error.getDefaultMessage())
//                .orElse("입력 값이 올바르지 않습니다.");
//
//        Map<String, Object> errorResponse = createErrorResponse(
//                errorMessage,
//                "VALIDATION_ERROR",
//                request.getRequestURI()
//        );
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }
//
//    // IllegalArgumentException 처리
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
//            IllegalArgumentException e,
//            HttpServletRequest request) {
//
//        log.warn("잘못된 인자: {}", e.getMessage());
//
//        Map<String, Object> errorResponse = createErrorResponse(
//                e.getMessage(),
//                "INVALID_ARGUMENT",
//                request.getRequestURI()
//        );
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }
//
//    // RuntimeException 처리 (Exception.class 대신 사용)
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<Map<String, Object>> handleRuntimeException(
//            RuntimeException e,
//            HttpServletRequest request) {
//
//        // Swagger 관련 요청은 제외
//        String requestURI = request.getRequestURI();
//        if (requestURI.contains("/v3/api-docs") ||
//                requestURI.contains("/swagger-ui") ||
//                requestURI.contains("/swagger-resources")) {
//            // Swagger 관련 예외는 다시 던져서 Spring이 처리하도록 함
//            throw e;
//        }
//
//        log.error("런타임 오류 발생: ", e);
//
//        Map<String, Object> errorResponse = createErrorResponse(
//                "요청 처리 중 오류가 발생했습니다.",
//                "RUNTIME_ERROR",
//                request.getRequestURI()
//        );
//
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
//
//    // 공통 에러 응답 생성 메서드
//    private Map<String, Object> createErrorResponse(String message, String code, String path) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("success", false);
//        errorResponse.put("message", message);
//        errorResponse.put("code", code);
//        errorResponse.put("timestamp", LocalDateTime.now().toString());
//        errorResponse.put("path", path);
//        return errorResponse;
//    }
//}
