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
public class TaskType extends CompanyRecord {
	public static final Long TASK_TYPE_GENERAL = 0L;
	public static final Long TASK_TYPE_MARKETING_CAMPAIGN = 1L;
	public static final Long TASK_TYPE_CLIENT_COMMUNICATION = 2L;
	public static final Long TASK_TYPE_REQUEST_FOR_OFFER = 3L;
	public static final Long TASK_TYPE_PREPARE_OFFER = 4L;
	public static final Long TASK_TYPE_PREPARE_CONTRACT = 5L;
	public static final Long TASK_TYPE_PROJECT = 6L;
	public static final Long TASK_TYPE_TRAINING_PLAN = 7L;
	public static final Long TASK_TYPE_TRAINING = 8L;
	public static final Long TASK_TYPE_EMPLOYEE_TRAINING = 9L;
	public static final Long TASK_TYPE_ATTESTATION_CAMPAIGN = 10L;
	public static final Long TASK_TYPE_EMPLOYEE_CAREER_PLAN = 11L;
	public static final Long TASK_TYPE_EMPLOYEE_ATTESTATION = 12L;
	public static final Long TASK_TYPE_TRANSPORT_ORDER = 13L;
	public static final Long TASK_TYPE_TRANSPORT = 14L;
	
	private String name;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<Task> tasks;
	
	public TaskType() {};
	
	public TaskType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}
}
