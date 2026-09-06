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
@ToString(exclude = {"wasteTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"wasteTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class WasteKind extends CompanyRecord {

	private String name;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "wasteKind")
	private List<WasteType> wasteTypes;

	public WasteKind() {};

	public WasteKind(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
	}
}
