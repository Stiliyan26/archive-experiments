package bg.latona.santa.entities.task;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"tasks"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"tasks"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class TaskStatus extends CompanyRecord {
	public static final Long TASK_STATUS_PLANNED = Long.valueOf(1);
	public static final Long TASK_STATUS_ASSIGNED = Long.valueOf(2);
	public static final Long TASK_STATUS_STARTED = Long.valueOf(3);
	public static final Long TASK_STATUS_FINISHED = Long.valueOf(4);
	public static final Long TASK_STATUS_FAILED = Long.valueOf(5);
	public static final Long TASK_STATUS_REJECTED = Long.valueOf(6);
	public static final Long TASK_STATUS_ACCEPTED_FINISH = Long.valueOf(7);
	public static final Long TASK_STATUS_CANCELLED = Long.valueOf(8);
	public static final Long TASK_STATUS_ERROR = Long.valueOf(9);
	
	private String name;
	private Long code;
	private Boolean terminal;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "status")
	private List<Task> tasks;
	
	public TaskStatus() {};
	
	public TaskStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			String name, Long code, Boolean terminal) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
		this.terminal = terminal;
	}
}
