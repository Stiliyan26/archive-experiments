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
@ToString(exclude = {"shippingContainers"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"shippingContainers"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class TransportOrder extends Task {

	private Date orderDate;
	@ManyToOne
	private LegalPerson transporter;
	@ManyToOne
	private LegalPerson orderOrderer;
	@ManyToOne
	private LegalPerson sender;
	@Column(length= 3000)
	private String senderContact;
	@Column(length= 3000)
	private String orderRoute;
	@Column(length= 3000)
	private String loadingPoint;
	private Date orderLoadingDate;
	@Column(length= 3000)
	private String exportCustoms;
	@Column(length= 3000)
	private String exportCustomsAgent;
	@Column(length= 3000)
	private String cargo;
	@Column(length= 3000)
	private String orderContents;
	@Column(length= 3000)
	private BigDecimal weight;
	private String loadingWarehouseRef;
	@Column(length= 3000)
	private String receiver;
	@Column(length= 3000)
	private String receiverContact;
	@Column(length= 3000)
	private String unloadingPoint;
	private Date orderUnloadingDate;
	@Column(length= 3000)
	private String importCustoms;
	@Column(length= 3000)
	private String importCustomsAgent;
	@Column(length= 3000)
	private String emptiesReturnPoint;
	@ManyToOne
	private Currency orderPaymentCurrency;
	private String orderPaymentAmount;
	private String payer;
	@Column(length= 3000)
	private String paymentDetails;
	@Column(length= 3000)
	private String notes;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "transportOrder")
	private List<TransportOrderShippingContainer> shippingContainers;
	
	
	public TransportOrder() {}

	public TransportOrder(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			TaskStatus status, TaskType type, TaskPriority priority, String title, String description, SecUser assigned,
			LegalPerson counterParty, Date deadline,
			Date orderDate, LegalPerson transporter, LegalPerson orderOrderer, LegalPerson sender,
			String senderContact, String orderRoute, String loadingPoint, Date orderLoadingDate, String exportCustoms,
			String exportCustomsAgent, String cargo, String orderContents, BigDecimal weight,
			String loadingWarehouseRef, String receiver, String receiverContact, String unloadingPoint,
			Date orderUnloadingDate, String importCustoms, String importCustomsAgent, String emptiesReturnPoint,
			Currency orderPaymentCurrency, String orderPaymentAmount, String payer,
			String paymentDetails, String notes) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, status, type, priority, title, description, assigned, counterParty, deadline);
		this.orderDate = orderDate;
		this.transporter = transporter;
		this.orderOrderer = orderOrderer;
		this.sender = sender;
		this.senderContact = senderContact;
		this.orderRoute = orderRoute;
		this.loadingPoint = loadingPoint;
		this.orderLoadingDate = orderLoadingDate;
		this.exportCustoms = exportCustoms;
		this.exportCustomsAgent = exportCustomsAgent;
		this.cargo = cargo;
		this.orderContents = orderContents;
		this.weight = weight;
		this.loadingWarehouseRef = loadingWarehouseRef;
		this.receiver = receiver;
		this.receiverContact = receiverContact;
		this.unloadingPoint = unloadingPoint;
		this.orderUnloadingDate = orderUnloadingDate;
		this.importCustoms = importCustoms;
		this.importCustomsAgent = importCustomsAgent;
		this.emptiesReturnPoint = emptiesReturnPoint;
		this.orderPaymentCurrency = orderPaymentCurrency;
		this.orderPaymentAmount = orderPaymentAmount;
		this.payer = payer;
		this.paymentDetails = paymentDetails;
		this.notes = notes;
	}


}
