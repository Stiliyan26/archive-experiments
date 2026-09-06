package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"offerDetails","cOfferStatuses","cReserveQuantities","cSales"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"offerDetails","cOfferStatuses","cReserveQuantities","cSales"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class COffer extends CompanyRecord {

	private LocalDate startDate; //not null
	private LocalDate endDate; //not null
	@ManyToOne
	private CCcPartner partner; //par_id
	@ManyToOne
	private CCcOrganizationUnit outCode; // not null
	@ManyToOne
	private LoiOfferStatus status; //not null Default 'A'
	private Integer numberOfr; // not null
	@Column(length= 3000)
	private String remark;
	//private CRequest retId; // not used???
	private BigDecimal discount; // not null Default 0
	private BigDecimal vat; // not null Default 0
	private String person;
	private BigDecimal total; // not null
	private BigDecimal totalWithDisc; // not null
	@ManyToOne
	private CCtCurrency currency;
	private String proformNumber;
	private LocalDate proformDate;
	private BigDecimal advanceTotal;  // not null default 0
	@ManyToOne
	private CCcOrganizationUnit outId;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ofrId")
	private List<COfferDetail> offerDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ofrId")
	private List<COfferStatus> cOfferStatuses;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ofrId")
	private List<CReserveQuantity> cReserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ofrId")
	private List<CSale> cSales;
	
	public COffer() {
		super();
	}
	
	public COffer(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, LocalDate startDate, LocalDate endDate, CCcPartner partner,
			CCcOrganizationUnit outCode, LoiOfferStatus status, Integer numberOfr, String remark, BigDecimal discount,
			BigDecimal vat, String person, BigDecimal total, BigDecimal totalWithDisc, CCtCurrency currency,
			String proformNumber, LocalDate proformDate, BigDecimal advanceTotal, CCcOrganizationUnit outId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.startDate = startDate;
		this.endDate = endDate;
		this.partner = partner;
		this.outCode = outCode;
		this.status = status;
		this.numberOfr = numberOfr;
		this.remark = remark;
		this.discount = discount;
		this.vat = vat;
		this.person = person;
		this.total = total;
		this.totalWithDisc = totalWithDisc;
		this.currency = currency;
		this.proformNumber = proformNumber;
		this.proformDate = proformDate;
		this.advanceTotal = advanceTotal;
		this.outId = outId;
	}
}
