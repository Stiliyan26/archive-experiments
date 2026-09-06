package bg.latona.santa.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"expenditures"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"expenditures"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiExpenditureType extends ListOptionItem {

	public static final Long EXPENDITURE_TYPE_MAINTENANCE = 1L;
	public static final Long EXPENDITURE_TYPE_TASK = 2L;
	public static final Long EXPENDITURE_TYPE_SHARED = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "expenseType")
	private List<Expenditure> expenditures;
	
	public LoiExpenditureType() {};
	
	public LoiExpenditureType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
