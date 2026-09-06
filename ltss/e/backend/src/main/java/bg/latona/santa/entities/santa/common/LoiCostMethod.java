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
@ToString(exclude = {"deliveries"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiCostMethod extends ListOptionItem {
	
	public static final Long COST_METHOD_UI = 1L;
	public static final Long COST_METHOD_VL = 2L;
	public static final Long COST_METHOD_QT = 3L;
	public static final Long COST_METHOD_WT = 4L;
	public static final Long COST_METHOD_VM = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "costMethod")
	private List<CDelivery> deliveries;

	public LoiCostMethod() {
		super();
	}
	
	public LoiCostMethod(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
