package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import lombok.Data;


@Data //auto-create getters and setters
@ToString(exclude = {"deliveries"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CTypeDocument extends CompanyRecord{
	private String name;
	private String shortName;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tdtId")
	private List<CDelivery> deliveries;

	public CTypeDocument() {
		super();
	}

	public CTypeDocument(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, String name, String shortName) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.shortName = shortName;
	}
}
