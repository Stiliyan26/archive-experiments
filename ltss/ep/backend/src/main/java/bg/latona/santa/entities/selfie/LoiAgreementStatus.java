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
public class LoiAgreementStatus extends ListOptionItem {

	public static final Long SIGNED = 914050000L;
	public static final Long FUTURE = 914050001L;
	public static final Long ACTIVE = 914050002L;
	public static final Long WAITING_FOR_PERMISSION_TO_USE = 914050003L;
	public static final Long DECLINED_FROM_CUSTOMER = 914050004L;
	public static final Long EXPIRED = 914050005L;
	public static final Long TERMINATED = 914050006L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "agreementStatus")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	public LoiAgreementStatus() {
	}

	public LoiAgreementStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
