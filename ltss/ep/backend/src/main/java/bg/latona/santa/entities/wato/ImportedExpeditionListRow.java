package bg.latona.santa.entities.wato;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ImportedExpeditionListRow extends CompanyRecord {

	@ManyToOne
	private ImportedOrderRow orderRow;
	@ManyToOne
	private ImportedExpeditionList expeditionList;
	private String foreignId;
	private String foreignOrderRowId;
	private String foreignExpeditionListId;
	private BigDecimal quantity;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;


	//default empty constructor
	public ImportedExpeditionListRow() {}

	//default constructor with all attributes
	public ImportedExpeditionListRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			ImportedOrderRow orderRow, ImportedExpeditionList expeditionList, String foreignId, String foreignOrderRowId, String foreignExpeditionListId,
			BigDecimal quantity, Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.orderRow = orderRow;
		this.expeditionList = expeditionList;
		this.foreignId = foreignId;
		this.foreignOrderRowId = foreignOrderRowId;
		this.foreignExpeditionListId = foreignExpeditionListId;
		this.quantity = quantity;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
