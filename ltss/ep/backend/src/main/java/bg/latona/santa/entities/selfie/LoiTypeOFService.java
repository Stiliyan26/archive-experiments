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
	
	public static final Long RES_PURCHASE = 343290000L;
	public static final Long PRODUCED = 343290001L;
	public static final Long PLANNED = 343290002L;
	public static final Long EXCESS = 343290003L;
	public static final Long BALANCE = 343290004L;
	public static final Long BALANCE90 = 343290005L;
	public static final Long PLANNED_100_6 = 343290006L;
	public static final Long EXCESS_100_6 = 343290007L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "typeOFService")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	public LoiTypeOFService() {
	}

	public LoiTypeOFService(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
