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
@ToString(exclude = {"vehicles"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"vehicles"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiVehicleType extends ListOptionItem {

	public static final Long VEHICLE_TYPE_TRACTOR_TRUCK = 1L;
	public static final Long VEHICLE_TYPE_TRAILER = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "vehicleType")
	private List<Vehicle> vehicles;
	
	public LoiVehicleType() {};
	
	public LoiVehicleType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
