package bg.latona.santa.entities.santa.common;

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
@ToString(exclude = {"cCcPartners"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCcPartners"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiLegalStatus extends ListOptionItem {
	
	public static final Long LEGAL_STATUS_BJT = 1L;
	public static final Long LEGAL_STATUS_BJN = 2L;
	public static final Long LEGAL_STATUS_BN = 3L;
	public static final Long LEGAL_STATUS_ORJ = 4L;
	public static final Long LEGAL_STATUS_ORN = 5L;
	public static final Long LEGAL_STATUS_ORU = 6L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalStatus")
	private List<CCcPartner> cCcPartners;

	public LoiLegalStatus() {
		super();
	}
	
	public LoiLegalStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
