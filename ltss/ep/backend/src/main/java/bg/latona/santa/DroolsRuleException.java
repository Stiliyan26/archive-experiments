package bg.latona.santa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import bg.latona.santa.DroolsRepositoryValidator.Result;

@ResponseStatus(HttpStatus.BAD_REQUEST) //or maybe HttpStatus.PARTIAL_CONTENT?
public class DroolsRuleException extends RuntimeException {

	private static final long serialVersionUID = -3739113094007641366L;
	private Result result;

	public DroolsRuleException(String message, Result result) {
		super(message);
		this.result = result;
	}

	public Result getResult() {
		return result;
	}
}