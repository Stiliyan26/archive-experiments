package bg.latona.santa.entities.person;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"legalPersonList"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"legalPersonList"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiLegalPersonType extends ListOptionItem {

	public static final Long LEGAL_PERSON_TYPE_REQUESTER = 1L;
	public static final Long LEGAL_PERSON_TYPE_SENDER = 2L;
	public static final Long LEGAL_PERSON_TYPE_TRANSPORTER = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalPersonType")
	private List<LegalPerson> legalPersonList;
	
	public LoiLegalPersonType() {};
	
	public LoiLegalPersonType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
