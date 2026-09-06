package bg.latona.santa.reports;

import java.util.Date;

import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;
import lombok.Data;

@Data //auto-create getters and setters
public class ActualTimeDTO {
	private SecUser resource;
	private Date fromTime;
	private Date toTime;
	private Task task;
	
	public ActualTimeDTO(SecUser resource, Date fromTime, Date toTime, Task task) {
		super();
		this.resource = resource;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.task = task;
	}
}
