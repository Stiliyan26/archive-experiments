package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.santa.finance.*;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import bg.latona.santa.entities.santa.common.CCtPartnerGroup;
import bg.latona.santa.entities.santa.common.LoiLegalStatus;
import bg.latona.santa.entities.security.SecUser;

@Data //auto-create getters and setters
@ToString(exclude = {"defaultVendorGoods","orders","deliveries","cDeliveryGoodMaps","cDeliveryOffers","cOffers","cPriceLists","cRequests"
,"cReserveQuantities","cSales","cStocks","fDiscounts","fCtRepresentatives","fCcContracts","fPtBatches","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances"
,"fPtCcBalances","fInvInvoices","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"defaultVendorGoods","orders","deliveries","cDeliveryGoodMaps","cDeliveryOffers","cOffers","cPriceLists","cRequests"
,"cReserveQuantities","cSales","cStocks","fDiscounts","fCtRepresentatives","fCcContracts","fPtBatches","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances"
,"fPtCcBalances","fInvInvoices","cInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CCcPartner extends CompanyRecord{
	
	@ManyToOne
	private CCcOrganizationUnit outCode;
	// Searchable field populated by eng or  ek
	private String code;
	private String name;
	private String bulstat;
	private String egn;
	private String address;
	@ManyToOne 
	private LoiPartnerType partnerType;
	@ManyToOne
	private CCtPartnerGroup partnerGroup;
	private String tel;
	private String fax;
	private String mol;
	private LocalDate activeFrom;
	private LocalDate activeTo;
	private String vatNo;
	private Integer oldCode;
	private String foreignNo;
	@ManyToOne
	private LoiLegalStatus legalStatus;
	private String email;
	//Not used inside_cons
	//parent id of out_code out_mask
	@ManyToOne
	private SecUser accountMgrUser;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "defaultVendor")
	private List<CGoods> defaultVendorGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<COrder> orders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CDelivery> deliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CDeliveryGoodMap> cDeliveryGoodMaps;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CDeliveryOffer> cDeliveryOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "partner")
	private List<COffer> cOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CPriceList> cPriceLists;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "partner")
	private List<CRequest> cRequests;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "partner")
	private List<CReserveQuantity> cReserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CSale> cSales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<CStock> cStocks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<FDiscount> fDiscounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<FCtRepresentative> fCtRepresentatives;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<FCcContract> fCcContracts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parId")
	private List<FPtBatch> fPtBatches;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccParId")
	private List<FInvInvoice> fInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoiceCounterParty")
	private List<CInvoice> cInvoices;

	public CCcPartner() {
		super();
	}
	
	public CCcPartner(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CCcOrganizationUnit outCode, String code, String name,
			String bulstat, String egn, String address, LoiPartnerType partnerType, CCtPartnerGroup partnerGroup,
			String tel, String fax, String mol, LocalDate activeFrom, LocalDate activeTo, String vatNo, Integer oldCode,
			String foreignNo, LoiLegalStatus legalStatus, String email, SecUser accountMgrUser) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.code = code;
		this.name = name;
		this.bulstat = bulstat;
		this.egn = egn;
		this.address = address;
		this.partnerType = partnerType;
		this.partnerGroup = partnerGroup;
		this.tel = tel;
		this.fax = fax;
		this.mol = mol;
		this.activeFrom = activeFrom;
		this.activeTo = activeTo;
		this.vatNo = vatNo;
		this.oldCode = oldCode;
		this.foreignNo = foreignNo;
		this.legalStatus = legalStatus;
		this.email = email;
		this.accountMgrUser = accountMgrUser;
	}

}
