package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.invoice.LoiPaymentType;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"deliveryDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveryDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CDelivery extends CompanyRecord{
	private Date deyDate;
	private Integer deyNumber;
	@ManyToOne
	private CTypeDocument tdtId;
	@ManyToOne
	private LoiPaymentType paymentType;
	private Date datePayment;
	@ManyToOne
	private CCcPartner parId;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	private BigDecimal discount;
	private BigDecimal total;
	private BigDecimal vat;
	private String status;
	private Integer dlyId;
	private String groundsNumber;
	private Date groundsDate;
	@ManyToOne
	private CCtCurrency currency;
	@Column(precision=19, scale=5)
	private BigDecimal exchangeRate;
	@ManyToOne
	private LoiTypeDoc typeDoc;
	private BigDecimal vatSum;
	private BigDecimal sum;
	private BigDecimal oblSum;
	private BigDecimal endSum;
	private BigDecimal danOsnova;
	@ManyToOne
	private CCcOrganizationUnit outId;
	private String posted;
	private BigDecimal cost;
	@ManyToOne
	private LoiCostMethod costMethod;
	@Column(length= 3000)
	String remarks;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "deyId")
	private List<CDeliveryDetail> deliveryDetails;

	public CDelivery() {
		super();
	}

	public CDelivery(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			Date deyDate, Integer deyNumber, CTypeDocument tdtId,
			LoiPaymentType paymentType, Date datePayment, CCcPartner parId, CCcOrganizationUnit outCode, BigDecimal discount,
			BigDecimal total, BigDecimal vat, String status, Integer dlyId, String groundsNumber, Date groundsDate,
			CCtCurrency currency, BigDecimal exchangeRate, LoiTypeDoc typeDoc, BigDecimal vatSum,
			BigDecimal sum, BigDecimal oblSum, BigDecimal endSum, BigDecimal danOsnova, CCcOrganizationUnit outId,
			String posted, BigDecimal cost, LoiCostMethod costMethod, String remarks) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.deyDate = deyDate;
		this.tdtId = tdtId;
		this.paymentType = paymentType;
		this.datePayment = datePayment;
		this.parId = parId;
		this.outCode = outCode;
		this.discount = discount;
		this.total = total;
		this.vat = vat;
		this.status = status;
		this.dlyId = dlyId;
		this.groundsNumber = groundsNumber;
		this.groundsDate = groundsDate;
		this.currency = currency;
		this.exchangeRate = exchangeRate;
		this.typeDoc = typeDoc;
		this.deyNumber = deyNumber;
		this.vatSum = vatSum;
		this.sum = sum;
		this.oblSum = oblSum;
		this.endSum = endSum;
		this.danOsnova = danOsnova;
		this.outId = outId;
		this.posted = posted;
		this.cost = cost;
		this.costMethod = costMethod;
		this.remarks = remarks;
	}
	
}
