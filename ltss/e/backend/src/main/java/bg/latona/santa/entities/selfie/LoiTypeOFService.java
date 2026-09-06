package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"agreementsSelfInvoicing"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"agreementsSelfInvoicing"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiTypeOFService extends ListOptionItem {

	public static final Long SCHEDULED_ENERGY = 914050000L;
	public static final Long METERED_ENERGY = 914050001L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "typeOFService")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	public LoiTypeOFService() {
	}

	public LoiTypeOFService(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
