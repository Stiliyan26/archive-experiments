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
public class TaskPriority extends CompanyRecord {
	public static final Long TASK_PRIORITY_LOW = 1L;
	public static final Long TASK_PRIORITY_MEDIUM = 2L;
	public static final Long TASK_PRIORITY_HIGH = 3L;
	public static final Long TASK_PRIORITY_URGENT = 4L;
	
	private String name;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "priority")
	private List<Task> tasks;
	
	public TaskPriority() {};
	
	public TaskPriority(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}
}
