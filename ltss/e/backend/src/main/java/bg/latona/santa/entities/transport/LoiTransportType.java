package bg.latona.santa.entities.transport;

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
@ToString(exclude = {"transports"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"transports"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiTransportType extends ListOptionItem {

	public static final Long TRANSPORT_TYPE_IMPORT = 1L;
	public static final Long TRANSPORT_TYPE_EXPORT = 2L;
	public static final Long TRANSPORT_TYPE_INTRA = 3L;
	public static final Long TRANSPORT_TYPE_UNLOAD = 4L;
	public static final Long TRANSPORT_TYPE_EMPTY = 5L;
	
	public LoiTransportType() {};

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "transportType")
	private List<Transport> transports;
	
	public LoiTransportType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
