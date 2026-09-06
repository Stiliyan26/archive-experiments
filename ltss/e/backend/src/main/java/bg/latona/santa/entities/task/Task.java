package bg.latona.santa.entities.task;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"comments","plannedIncomeOrExpenses","plannedTimes","taskAttachments","relationsToTask","relationsFromTask","requiredAttachments","timeSheetItems","watchers"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"comments","plannedIncomeOrExpenses","plannedTimes","taskAttachments","relationsToTask","relationsFromTask","requiredAttachments","timeSheetItems","watchers"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Inheritance(strategy = InheritanceType.JOINED) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public class Task extends CompanyRecord {

	@ManyToOne
	private TaskStatus status;
	@ManyToOne
	private TaskType type;
	@ManyToOne
	private TaskPriority priority;
	private String title;
	@Column(length= 3000)
	private String description;
	@ManyToOne
	private SecUser assigned;
	@ManyToOne
	private LegalPerson counterParty;
	private Date deadline;
	

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<Comment> comments;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<PlannedIncomeOrExpense> plannedIncomeOrExpenses;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<PlannedTime> plannedTimes;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<TaskAttachment> taskAttachments;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "fromTask")
	private List<TaskRelation> relationsToTask;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "toTask")
	private List<TaskRelation> relationsFromTask;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<TaskRequiredAttachment> requiredAttachments;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<TimeSheetItem> timeSheetItems;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "task")
	private List<TaskWatcher> watchers;

	//default empty constructor
	public Task() {}

	public Task(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			TaskStatus status, TaskType type, TaskPriority priority, String title, String description, SecUser assigned,
			LegalPerson counterParty, Date deadline) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.status = status;
		this.type = type;
		this.priority = priority;
		this.title = title;
		this.description = description;
		this.assigned = assigned;
		this.counterParty = counterParty;
		this.deadline = deadline;
	}	
}