package bg.latona.santa.entities.employee;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.transport.Transport;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"employeeAttestations","employeeAttachments","transportsForDrivers","employeeAttestationWatchers"
,"employeeAttestationCertifiers","employeeCompetences"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"employeeAttestations","employeeAttachments","transportsForDrivers","employeeAttestationWatchers"
,"employeeAttestationCertifiers","employeeCompetences"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Employee extends AllocationOrigin {

	private String employeeName;
	private String employeeEgn;
	private String employeeAddress;
	private String businessPhone;
	private String personalPhone;
	@ManyToOne
	private SecUser secUser;
	@ManyToOne
	private JobPosition position;
	@ManyToOne
	private CompanyDepartment department;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "employee")
	private List<EmployeeAttestation> employeeAttestations;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "employee")
	private List<EmployeeAttachment> employeeAttachments;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "driver")
	private List<Transport> transportsForDrivers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "watcher")
	private List<EmployeeAttestation> employeeAttestationWatchers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "certifier")
	private List<EmployeeAttestation> employeeAttestationCertifiers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "employee")
	private List<EmployeeCompetence> employeeCompetences;

	
	public Employee() {
	}

	public Employee(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String employeeName, String employeeEgn, String employeeAddress, String businessPhone, String personalPhone,
			SecUser secUser, JobPosition position, CompanyDepartment department) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.employeeName = employeeName;
		this.employeeEgn = employeeEgn;
		this.employeeAddress = employeeAddress;
		this.businessPhone = businessPhone;
		this.personalPhone = personalPhone;
		this.secUser = secUser;
		this.position = position;
		this.department = department;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return new BigDecimal(-1);
	}
}
