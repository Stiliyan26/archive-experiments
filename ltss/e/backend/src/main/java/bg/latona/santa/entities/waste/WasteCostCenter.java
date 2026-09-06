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
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"wastes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"wastes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class WasteCostCenter extends CompanyRecord {

	private String name;
	private String eik;
	private String possessionReason;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "costCenter")
	private List<Waste> wastes;

	public WasteCostCenter() {};

	public WasteCostCenter(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String eik, String possessionReason) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.eik = eik;
		this.possessionReason = possessionReason;
	}
}
