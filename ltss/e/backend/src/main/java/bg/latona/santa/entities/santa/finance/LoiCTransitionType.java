package bg.latona.santa.entities.santa.finance;


import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Data //auto-create getters and setters
@ToString(exclude = {"fCtTransitionTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtTransitionTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiCTransitionType extends ListOptionItem {

	public static final Long CT_TRANSITION_TYPE_L = 1L;
	public static final Long CT_TRANSITION_TYPE_P = 2L;
	public static final Long CT_TRANSITION_TYPE_N = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteType")
	private List<FCtTransitionType> fCtTransitionTypes;

	public LoiCTransitionType() {
		super();
	}

	public LoiCTransitionType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}