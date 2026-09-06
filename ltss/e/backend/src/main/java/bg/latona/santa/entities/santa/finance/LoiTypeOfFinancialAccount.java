package bg.latona.santa.entities.santa.finance;

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
@ToString(exclude = {"fChartAccounts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fChartAccounts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiTypeOfFinancialAccount extends ListOptionItem {

	public static final Long COA_TYPE_SYNTHETIC_Y = 1L;
	public static final Long COA_TYPE_SYNTHETIC_N = 2L;
	public static final Long COA_TYPE_SYNTHETIC_S = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "isSynthetic")
	private List<FChartAccount> fChartAccounts;

	public LoiTypeOfFinancialAccount() {
		super();
	}

	public LoiTypeOfFinancialAccount(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
