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
@ToString(exclude = {"taskRelations"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"taskRelations"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class TaskRelationType extends CompanyRecord {
	public static final Long TASK_RELATION_TYPE_RELATED = 1L;
	public static final Long TASK_RELATION_TYPE_SUBTASK = 2L;
	public static final Long TASK_RELATION_TYPE_PLAN = 3L;
	public static final Long TASK_RELATION_TYPE_FINISH_TO_START = 4L;
	public static final Long TASK_RELATION_TYPE_START_TO_START = 5L;
	public static final Long TASK_RELATION_TYPE_START_TO_FINISH = 6L;
	public static final Long TASK_RELATION_TYPE_FINISH_TO_FINISH = 7L;
	
	private String name;
	private String inverseName;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "relation")
	private List<TaskRelation> taskRelations;
	
	public TaskRelationType() {};
	
	public TaskRelationType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String inverseName, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.inverseName = inverseName;
		this.code = code;
	}
}
