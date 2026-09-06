package bg.latona.santa.entities.wato;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"importedExpeditionListRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedExpeditionListRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ImportedExpeditionList extends CompanyRecord {

	@ManyToOne
	private ImportedOrder order;
	private String foreignId;
	private String foreignOrderId;
	private String docNum;
	private Date docDate;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "expeditionList")
	private List<ImportedExpeditionListRow> importedExpeditionListRows;

	//default empty constructor
	public ImportedExpeditionList() {}

	//default constructor with all attributes
	public ImportedExpeditionList(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			ImportedOrder order, String foreignId, String foreignOrderId, String docNum, Date docDate,
			Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.order = order;
		this.foreignId = foreignId;
		this.foreignOrderId = foreignOrderId;
		this.docNum = docNum;
		this.docDate = docDate;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
