package bg.latona.santa.reports;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) //or maybe HttpStatus.PARTIAL_CONTENT?
public class ReportException extends RuntimeException {

	private static final long serialVersionUID = 3171538560029696885L;

	public ReportException(String message) {
		super(message);
	}
}