package bg.latona.santa.entities.santa.common;

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
@ToString(exclude = {"deliveries","cSales"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries","cSales"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiTypeDoc extends ListOptionItem {

	public static final Long TYPE_DOC_F = 1L;
	public static final Long TYPE_DOC_PB = 2L;
	public static final Long TYPE_DOC_PP = 3L;
	public static final Long TYPE_DOC_AP = 4L;
	public static final Long TYPE_DOC_KI = 5L;
	public static final Long TYPE_DOC_PZ = 6L;
	public static final Long TYPE_DOC_S = 7L;
	public static final Long TYPE_DOC_OFR = 8L;
	public static final Long TYPE_DOC_ORR = 9L;
	public static final Long TYPE_DOC_ODY = 10L;
	public static final Long TYPE_DOC_ISR = 11L;
	public static final Long TYPE_DOC_INV = 12L;
	public static final Long TYPE_DOC_SR = 13L;
	public static final Long TYPE_DOC_MRT = 14L;
	public static final Long TYPE_DOC_DI = 15L;
	public static final Long TYPE_DOC_RL = 16L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "typeDoc")
	private List<CDelivery> deliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "typeDoc")
	private List<CSale> cSales;

	public LoiTypeDoc() {
		super();
	}

	public LoiTypeDoc(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
