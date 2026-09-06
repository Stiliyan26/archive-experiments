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
@ToString(exclude = {"cOrders"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cOrders"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiOrderStatus extends ListOptionItem {

	public static final Long ORDER_STATUS_CR = 1L;
	public static final Long ORDER_STATUS_CF = 2L;
	public static final Long ORDER_STATUS_FN = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "status")
	private List<COrder> cOrders;

	public LoiOrderStatus() {
		super();
	}

	public LoiOrderStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
