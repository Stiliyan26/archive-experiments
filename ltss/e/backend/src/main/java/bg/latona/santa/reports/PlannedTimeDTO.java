package bg.latona.santa.reports;

import java.math.BigDecimal;

import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;
import lombok.Data;

@Data //auto-create getters and setters
public class PlannedTimeDTO {
	private SecUser resource;
	private BigDecimal minutes;
	private Task task;

	public PlannedTimeDTO(SecUser resource, BigDecimal minutes, Task task) {
		super();
		this.resource = resource;
		this.minutes = minutes;
		this.task = task;
	}
}
