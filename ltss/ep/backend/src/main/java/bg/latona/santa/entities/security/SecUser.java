package bg.latona.santa.entities.security;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import bg.latona.santa.entities.employee.Employee;
import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.person.Customer;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CInvoice;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TaskWatcher;
import bg.latona.santa.entities.task.PlannedTime;
import bg.latona.santa.entities.task.TimeChargeRate;
import bg.latona.santa.entities.task.TimeSheetItem;
import bg.latona.santa.entities.wato.ImportedLegalPersonGroup;
import org.hibernate.envers.Audited;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@JsonIgnoreProperties(value={"createdBy", "lastModifiedBy" }, allowSetters=true) //avoid serialization by Jackson but allow deserialization
//not OK - bug with JSONUnwrapped, which is used by Spring... @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class) //avoid cycles by createdBy and lastModifiedBy
@ToString(exclude = {"password", "roles", "timeChargeRates","assignedTasks","cCcPartners","employees","invoices"
,"customers","accessControls","plannedTimes","taskWatchers","timeChargeRateProviders","timeSheetItems","importedLegalPersonGroups","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"roles", "timeChargeRates","assignedTasks","cCcPartners","employees","invoices"
,"customers","accessControls","plannedTimes","taskWatchers","timeChargeRateProviders","timeSheetItems","importedLegalPersonGroups","cInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"name"})) //for all unique constraints should be put Drools UNIQUE rules too
public class SecUser extends CompanyRecord {
	public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

	private String name;
	private String fullName; 
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //avoid serialization by Jackson but allow deserialization
	private String password;
	private String email;
	private String code;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "user")
	private List<SecUserRole> roles;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "resource")
	private List<TimeChargeRate> timeChargeRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "assigned")
	private List<Task> assignedTasks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "accountMgrUser")
	private List<CCcPartner> cCcPartners;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "secUser")
	private List<Employee> employees;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "issuedBy")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "assignedSales")
	private List<Customer> customers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "secUser")
	private List<AccessControl> accessControls;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "resource")
	private List<PlannedTime> plannedTimes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "watcher")
	private List<TaskWatcher> taskWatchers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "provider")
	private List<TimeChargeRate> timeChargeRateProviders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "resource")
	private List<TimeSheetItem> timeSheetItems;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "assignedSales")
	private List<ImportedLegalPersonGroup> importedLegalPersonGroups;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "issuedBy")
	private List<CInvoice> cInvoices;
	
	public SecUser() {}

	public SecUser(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String fullName, String password, String email, String code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.fullName = fullName;
		this.setPassword(password);
		this.email = email;
		this.code = code;
	};

	public void setPassword(String password) {
		//System.out.println("Set password: "+password);
		this.password = PASSWORD_ENCODER.encode(password);
		//System.out.println("Encrypted password: "+this.password);
	}
}
