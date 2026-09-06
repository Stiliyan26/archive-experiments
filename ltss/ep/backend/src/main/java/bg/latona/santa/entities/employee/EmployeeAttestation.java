package bg.latona.santa.entities.employee;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.HashTag;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class EmployeeAttestation extends Attachable {

	@ManyToOne
	private Employee employee;
	private LocalDate attestationFromDate;
	private LocalDate attestationToDate;
	@Column(length= 3000)
	private String employeeGoals;
	@Column(length= 3000)
	private String attestationText;
	@Column(length= 3000)
	private String motivesText;
	@Column(length= 3000)
	private String potentialText;
	@Column(length= 3000)
	private String employeeCommentText;
	@Column(length= 3000)
	private String controllingOfficerCommentText;
	@ManyToOne
	private Employee watcher;
	@ManyToOne
	private Employee certifier;

	public EmployeeAttestation() {};
	
	public EmployeeAttestation(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			Employee employee, LocalDate attestationFromDate, LocalDate attestationToDate, String employeeGoals, 
			String attestationText, String motivesText, String potentialText,
			String employeeCommentText, String controllingOfficerCommentText,
			Employee watcher, Employee certifier) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.employee = employee;
		this.attestationFromDate = attestationFromDate;
		this.attestationToDate = attestationToDate;
		this.employeeGoals = employeeGoals;
		this.attestationText = attestationText;
		this.motivesText = motivesText;
		this.potentialText = potentialText;
		this.employeeCommentText = employeeCommentText;
		this.controllingOfficerCommentText = controllingOfficerCommentText;
		this.watcher = watcher;
		this.certifier = certifier;
	}
}
