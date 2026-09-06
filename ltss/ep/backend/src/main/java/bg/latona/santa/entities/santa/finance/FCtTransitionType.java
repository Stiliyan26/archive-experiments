package bg.latona.santa.entities.santa.finance;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fCtBatchTypeRules","fCtBatchTteTypes","fCtBatchTteLinks","fBreTransitions","fPtBatchCcDetails","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTypeRules","fCtBatchTteTypes","fCtBatchTteLinks","fBreTransitions","fPtBatchCcDetails","fInvInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class FCtTransitionType extends CompanyRecord{

	private String code;
	private String name;
	@ManyToOne
	private LoiCTransitionType tteType;
	@ManyToOne
	private LoiCTransitionSide tteSide;
	// not used private String tteFlag;
	// not used private int orderIndex;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteId")
	private List<FCtBatchTypeRule> fCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteId")
	private List<FCtBatchTteType> fCtBatchTteTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteId")
	private List<FCtBatchTteLink> fCtBatchTteLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteCode")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteId")
	private List<FInvInvoice> fInvInvoices;

	public FCtTransitionType() {
		super();
	}

	public FCtTransitionType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String code, String name, LoiCTransitionType tteType, LoiCTransitionSide tteSide) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.tteType = tteType;
		this.tteSide = tteSide;
	}

}
