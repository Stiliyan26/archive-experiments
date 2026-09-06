package bg.latona.santa.entities.santa.finance;

import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CSale;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"fCtBatchTteLinks","fPtBatches","fBreTransitions","sales","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTteLinks","fPtBatches","fBreTransitions","sales","fInvInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class FCtInvDealType extends CompanyRecord{
	
	private String code;
	private String name;
	@Column(length= 3000)
	private String descr;
	@ManyToOne
	private LoiBatchJteDefaultSide side;
	private BigDecimal percentVat;
	private String status;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ideId")
	private List<FCtBatchTteLink> fCtBatchTteLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ideId")
	private List<FPtBatch> fPtBatches;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ideCode")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "idtCode")
	private List<CSale> sales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ideId")
	private List<FInvInvoice> fInvInvoices;

	public FCtInvDealType() {
		super();
	}

	public FCtInvDealType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String code,
						  String name, String descr, LoiBatchJteDefaultSide side, BigDecimal percentVat, String status) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.side = side;
		this.percentVat = percentVat;
		this.status = status;
	}

}
