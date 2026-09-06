package bg.latona.santa.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.asset.AssetAttachment;
import bg.latona.santa.entities.employee.EmployeeAttachment;
import bg.latona.santa.entities.person.LegalPersonAttachment;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.TaskAttachment;
import bg.latona.santa.entities.task.TaskRequiredAttachment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"attachableRevenuesAndExpenses", "taskAttachments", "legalPersonAttachments", "attachmentToEmployee", "attachmentToAsset","taskRequiredAttachments"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"attachableRevenuesAndExpenses", "taskAttachments", "legalPersonAttachments", "attachmentToEmployee", "attachmentToAsset","taskRequiredAttachments"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Inheritance(strategy = InheritanceType.JOINED) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public abstract class Attachable extends CompanyRecord {

	@Column(length= 3000)
	private String name;

	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachment")
	private List<TaskAttachment> taskAttachments;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachmentToPerson")
	private List<LegalPersonAttachment> legalPersonAttachments;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachmentToEmployee")
	private List<EmployeeAttachment> employeeAttachments;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachable")
	private List<AttachableRevenuesAndExpenses> attachableRevenuesAndExpenses;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachmentToAsset")
	private List<AssetAttachment> assetAttachments;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "attachment")
	private List<TaskRequiredAttachment> taskRequiredAttachments;

	@ManyToMany
	private List<HashTag> hashTags;

	

	
	public Attachable() {};
	
	public Attachable(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.hashTags = hashTags;
	}
}
