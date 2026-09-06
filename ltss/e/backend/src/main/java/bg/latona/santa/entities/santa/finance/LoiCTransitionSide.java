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
public class LoiCTransitionSide extends ListOptionItem {

	public static final Long CT_TRANSITION_SIDE_CHI = 1L;
	public static final Long CT_TRANSITION_SIDE_CHE = 2L;
	public static final Long CT_TRANSITION_SIDE_BKI = 3L;
	public static final Long CT_TRANSITION_SIDE_BKE = 4L;
	public static final Long CT_TRANSITION_SIDE_N = 5L;
	public static final Long CT_TRANSITION_SIDE_INI = 6L;
	public static final Long CT_TRANSITION_SIDE_INE = 7L;
	public static final Long CT_TRANSITION_SIDE_DE1 = 8L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tteSide")
	private List<FCtTransitionType> fCtTransitionTypes;

	public LoiCTransitionSide() {
		super();
	}

	public LoiCTransitionSide(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}