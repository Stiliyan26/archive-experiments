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
@ToString(exclude = {"fPtClosingAccounts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtClosingAccounts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiPtClosingAccountPeriodMonth extends ListOptionItem {

	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_01 = 1L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_02 = 2L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_03 = 3L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_04 = 4L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_05 = 5L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_06 = 6L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_07 = 7L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_08 = 8L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_09 = 9L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_10 = 10L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_11 = 11L;
	public static final Long PT_CLOSING_ACCOUNT_PERIOD_MONTH_CK_12 = 12L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "periodMonth")
	private List<FPtClosingAccount> fPtClosingAccounts;

	public LoiPtClosingAccountPeriodMonth() {
	}

	public LoiPtClosingAccountPeriodMonth(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
