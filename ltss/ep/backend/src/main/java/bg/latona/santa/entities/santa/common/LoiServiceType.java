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
@ToString(exclude = {"cServices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cServices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiServiceType extends ListOptionItem {

	public static final Long SERVICE_TYPE_SV = 1L;
	public static final Long SERVICE_TYPE_PT = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "serviceType")
	private List<CService> cServices;

	public LoiServiceType() {
		super();
	}

	public LoiServiceType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
