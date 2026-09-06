package bg.latona.santa.selfie.exception;

import bg.latona.santa.reports.ReportException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<String> handleReportException(ReportException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Reporting Exception: " + ex.getMessage());
    }
}