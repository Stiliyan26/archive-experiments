package bg.latona.santa.entities.transport;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.employee.Employee;
import bg.latona.santa.entities.invoice.InvoiceRow;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.task.TaskPriority;
import bg.latona.santa.entities.task.TaskStatus;
import bg.latona.santa.entities.task.TaskType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"invoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"invoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Transport extends Task {

	@ManyToOne
	private LoiTransportType transportType;
	@ManyToOne
	private Vehicle tractorUnit;
	@ManyToOne
	private Vehicle trailer;
	@Column(length= 3000)
	private String route;
	private BigDecimal distanceKm;
	@ManyToOne
	private LegalPerson orderer;
	@Column(length= 3000)
	private String contents;
	private String containerNumber;
	@ManyToOne
	private ShippingContainerType containerType;
	private Date loadingDate;
	private Date unloadingDate;
	@ManyToOne
	private Employee driver;
	private Date sendDate;
	private Date receiveDate;
	private Date paymentDate;
	private String confirmation;
	private Boolean isPaid;
	@ManyToOne
	private Currency paymentCurrency;
	private BigDecimal paymentAmount;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "transport")
	private List<InvoiceRow> invoiceRows;
	
	public Transport() {}

	public Transport(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			TaskStatus status, TaskType type, TaskPriority priority, String title, String description, SecUser assigned,
			LegalPerson counterParty, Date deadline,
			LoiTransportType transportType, Vehicle tractorUnit, Vehicle trailer, String route, BigDecimal distanceKm,
			LegalPerson orderer, String contents, String containerNumber, ShippingContainerType containerType, Date loadingDate, Date unloadingDate, Employee driver, Date sendDate,
			Date receiveDate, Date paymentDate, String confirmation, Boolean isPaid, Currency paymentCurrency,
			BigDecimal paymentAmount) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, status, type, priority, title, description, assigned, counterParty, deadline);
		this.transportType = transportType;
		this.tractorUnit = tractorUnit;
		this.trailer = trailer;
		this.route = route;
		this.distanceKm = distanceKm;
		this.orderer = orderer;
		this.contents = contents;
		this.containerNumber = containerNumber;
		this.containerType = containerType;
		this.loadingDate = loadingDate;
		this.unloadingDate = unloadingDate;
		this.driver = driver;
		this.sendDate = sendDate;
		this.receiveDate = receiveDate;
		this.paymentDate = paymentDate;
		this.confirmation = confirmation;
		this.isPaid = isPaid;
		this.paymentCurrency = paymentCurrency;
		this.paymentAmount = paymentAmount;
	}

}
