package nutc.sot.farm_quest.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(new ApiErrorResponse(
                        exception.getErrorCode().name(),
                        exception.getMessage(),
                        OffsetDateTime.now(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException exception,
                                                                      HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        AuthErrorCode.INVALID_EMAIL.name(),
                        exception.getBindingResult().getAllErrors().stream()
                                .findFirst()
                                .map(error -> error.getDefaultMessage())
                                .orElse("Validation failed"),
                        OffsetDateTime.now(),
                        request.getRequestURI()
                ));
    }
}
