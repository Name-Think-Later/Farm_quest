package nutc.sot.farm_quest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final AuthErrorCode errorCode;
    private final HttpStatus httpStatus;

    public AuthException(AuthErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
