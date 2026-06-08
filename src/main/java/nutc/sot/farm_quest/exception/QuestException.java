package nutc.sot.farm_quest.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class QuestException extends RuntimeException {

    private final QuestErrorCode errorCode;
    private final HttpStatus httpStatus;

    public QuestException(QuestErrorCode errorCode, HttpStatus httpStatus, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
