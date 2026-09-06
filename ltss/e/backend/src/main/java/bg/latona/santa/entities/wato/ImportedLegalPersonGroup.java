package bg.latona.santa.entities.wato;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.AreaCategory;
import bg.latona.santa.entities.person.BusinessCategory;
import bg.latona.santa.entities.person.DirectionCategory;
import bg.latona.santa.entities.person.GeneralCategory;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ImportedLegalPersonGroup extends CompanyRecord {

	@ManyToOne
	private LegalPerson legalPerson;
	@ManyToOne
	private SecUser assignedSales;
	@ManyToOne
	private DirectionCategory direction;
	@ManyToOne
	private AreaCategory area;
	@ManyToOne
	private BusinessCategory business;
	@ManyToOne
	private GeneralCategory generalCategory;
	private String foreignId;
	private String contragentID;
	private String mainGroupID;
	private String groupID;
	private String mainGroupCode;
	private String mainGroupName;
	private String groupCode;
	private String groupName;
	private Integer compId;
	private Long updateCountAsBigInt;

	//default empty constructor
	public ImportedLegalPersonGroup() {}

	//default constructor with all attributes
	public ImportedLegalPersonGroup(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			LegalPerson legalPerson, SecUser assignedSales, DirectionCategory direction, AreaCategory area,
			BusinessCategory business, GeneralCategory generalCategory, String foreignId, String contragentID,
			String mainGroupID, String groupID, String mainGroupCode, String mainGroupName, String groupCode,
			String groupName, Integer compId,
			Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.legalPerson = legalPerson;
		this.assignedSales = assignedSales;
		this.direction = direction;
		this.area = area;
		this.business = business;
		this.generalCategory = generalCategory;
		this.foreignId = foreignId;
		this.contragentID = contragentID;
		this.mainGroupID = mainGroupID;
		this.groupID = groupID;
		this.mainGroupCode = mainGroupCode;
		this.mainGroupName = mainGroupName;
		this.groupCode = groupCode;
		this.groupName = groupName;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
