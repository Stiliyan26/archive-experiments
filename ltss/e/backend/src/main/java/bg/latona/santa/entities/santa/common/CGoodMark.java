package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Data //auto-create getters and setters
@ToString(exclude = {"markGoods"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"markGoods"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CGoodMark extends CompanyRecord{
	private String markCode;
	private String markName;
	private String description;
	@ManyToOne
	private CCcOrganizationUnit outCode; 
	private Integer lastNumber;
	@ManyToOne 
	private CCcPartner defaultVendor;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "goodMark")
	private List<CGoods> markGoods;
	
	public CGoodMark() {
		super();
	}
	
	public CGoodMark(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, String markCode, String markName, String description,
			CCcOrganizationUnit outCode, Integer lastNumber, CCcPartner defaultVendor) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.markCode = markCode;
		this.markName = markName;
		this.description = description;
		this.outCode = outCode;
		this.lastNumber = lastNumber;
		this.defaultVendor = defaultVendor;
	}
}
