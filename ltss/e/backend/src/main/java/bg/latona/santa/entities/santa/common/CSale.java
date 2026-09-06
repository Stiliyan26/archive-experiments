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
import bg.latona.santa.entities.santa.finance.FCtInvDealType;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"saleDetails","cSales"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"saleDetails","cSales"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CSale extends CompanyRecord{
	private Date saleDate;
	@ManyToOne
	private LoiPaymentType paymentType;
	private Date datePayment;
	private BigDecimal discount;
	@ManyToOne
	private CCcOrganizationUnit outCode; //warehouse we get goods from
	private String placeDeals;
	@ManyToOne
	private CCcPartner parId; //client that receivese the goods
	@ManyToOne
	private CSale saeId;
	private String status; //2 - for transfer, 21 - delivery generated
	private BigDecimal total;
	private BigDecimal vat;
	@ManyToOne
	private CCtCurrency currency;
	@Column(precision=19, scale=5)
	private BigDecimal exchangeRate;
	private BigDecimal sum;
	private BigDecimal oblSum;
	private BigDecimal endSum;
	private BigDecimal danOsnova;
	private Integer tdtId; //default 999
	@ManyToOne
	private LoiTypeDoc typeDoc;
	private Integer documentNumber;
	private BigDecimal vatto;
	@ManyToOne
	private CCcOrganizationUnit outId; //warehouse that receives the goods
	private String oldTypeDoc;
	@ManyToOne
	private COffer ofrId;
	private String posted;
	@ManyToOne
	private FCtInvDealType idtCode; //DEFAULT '101'::character varying
	private BigDecimal advanceUsed;
	private BigDecimal cost;
	private BigDecimal totalPayed;
	private String payed;
	@Column(length= 3000)
	private String remark;
	//private cc_partner_objects potId;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "saeId")
	private List<CSaleDetail> saleDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "saeId")
	private List<CSale> cSales;

	public CSale() {
		super();
	}

	public CSale(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, Date saleDate,
	LoiPaymentType paymentType,Date datePayment,BigDecimal discount,CCcOrganizationUnit outCode,String placeDeals, CCcPartner parId,CSale saeId, String status,
	BigDecimal total,BigDecimal vat,CCtCurrency currency,BigDecimal exchangeRate ,BigDecimal sum ,BigDecimal oblSum ,BigDecimal endSum ,
	BigDecimal danOsnova ,Integer tdtId ,LoiTypeDoc typeDoc ,Integer documentNumber ,BigDecimal vatto ,CCcOrganizationUnit outId ,
	String oldTypeDoc ,COffer ofrId ,String posted ,FCtInvDealType idtCode ,BigDecimal advanceUsed ,BigDecimal cost ,BigDecimal totalPayed ,
	String payed ,String remark) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);

		this.saleDate = saleDate;
		this.paymentType = paymentType;
		this.datePayment = datePayment;
		this.discount = discount;
		this.outCode = outCode;
		this.placeDeals = placeDeals;
		this.parId = parId;
		this.saeId = saeId;
		this.status = status;
		this.total = total;
		this.vat = vat;
		this.currency = currency;
		this.exchangeRate = exchangeRate;
		this.sum = sum;
		this.oblSum = oblSum;
		this.endSum = endSum;
		this.danOsnova = danOsnova;
		this.tdtId = tdtId;
		this.typeDoc = typeDoc;
		this.documentNumber = documentNumber;
		this.vatto = vatto;
		this.outId = outId;
		this.oldTypeDoc = oldTypeDoc;
		this.ofrId = ofrId;
		this.posted = posted;
		this.idtCode = idtCode;
		this.advanceUsed = advanceUsed;
		this.cost = cost;
		this.totalPayed = totalPayed;
		this.payed = payed;
		this.remark = remark;
	}
}
