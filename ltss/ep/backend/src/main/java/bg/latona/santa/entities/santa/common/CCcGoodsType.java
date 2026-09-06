package bg.latona.santa.entities.santa.common;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.santa.finance.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cGoods","fDiscounts","fCtGoods","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances"
,"fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cGoods","fDiscounts","fCtGoods","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances"
,"fInvInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CCcGoodsType extends CompanyRecord {


	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String code;
	private String name;
	@ManyToOne
	private LoiGoodsType goodType;
	private LocalDate activeFrom;
	private LocalDate activeTo;
	//private String out_mask parent id of out_code out_mask

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "gteId")
	private List<CGoods> cGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "gteId")
	private List<FDiscount> fDiscounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "gteId")
	private List<FCtGood> fCtGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccGteId")
	private List<FInvInvoice> fInvInvoices;
	
	public CCcGoodsType() {
		super();
	}
	
	public CCcGoodsType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, String code, String name, LoiGoodsType goodType, LocalDate activeFrom, LocalDate activeTo) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.code = code;
		this.name = name;
		this.goodType = goodType;
		this.activeFrom = activeFrom;
		this.activeTo = activeTo;
	}
	
	
}
