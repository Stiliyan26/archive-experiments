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
@ToString(exclude = {"fCtBatchTteTypes","fCtBatchTteLinks","fCtBatchJteDefaults","fPtBatches"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTteTypes","fCtBatchTteLinks","fCtBatchJteDefaults","fPtBatches"}) //avoid recursion by Lombok
@Audited
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtBatchType extends CompanyRecord{
	private String code;
	private String name;
	private String descr;
	private Boolean isDds;
	//to be related with document numbers
	private String typeNo;
	@ManyToOne
	private LoiBatchCalculationType type;
	@ManyToOne
	private LoiBatchJteDefaultSide side;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bteId")
	private List<FCtBatchTteType> fCtBatchTteTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bteId")
	private List<FCtBatchTteLink> fCtBatchTteLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bteId")
	private List<FCtBatchJteDefault> fCtBatchJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bteId")
	private List<FPtBatch> fPtBatches;

	public FCtBatchType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String code, String name, String descr, Boolean isDds,
			String typeNo, LoiBatchCalculationType type, LoiBatchJteDefaultSide side) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.isDds = isDds;
		this.typeNo = typeNo;
		this.type = type;
		this.side = side;
	}

}
