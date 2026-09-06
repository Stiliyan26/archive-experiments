package bg.latona.santa.entities.waste;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"wastes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"wastes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class WasteType extends CompanyRecord {

	private String wasteCode;
	private String name;
	@ManyToOne
	private WasteKind wasteKind;
	private String origin;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "wasteType")
	private List<Waste> wastes;

	public WasteType() {};

	public WasteType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String wasteCode, String name, WasteKind wasteKind, String origin) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.wasteCode = wasteCode;
		this.name = name;
		this.wasteKind = wasteKind;
		this.origin = origin;
	}
}
