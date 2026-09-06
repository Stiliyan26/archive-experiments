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
public class LoiReasonForTermination extends ListOptionItem {

	public static final Long CUSTOMER_REFUSAL_OF_THE_SERVICE = 914050000L;
	public static final Long OWN_INITIATIVE = 914050001L;
	public static final Long VAT_NUMBER_REGISTRATION_CHANGE = 914050002L;
	public static final Long TERMINATION_OF_THE_MAIN_CONTRACT = 914050003L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "reasonForTermination")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	public LoiReasonForTermination() {
	}

	public LoiReasonForTermination(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
