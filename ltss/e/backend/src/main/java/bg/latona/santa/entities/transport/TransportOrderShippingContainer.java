package bg.latona.santa.entities.transport;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class TransportOrderShippingContainer extends CompanyRecord {

	@ManyToOne
	private TransportOrder transportOrder;
	private String number;
	@ManyToOne
	private ShippingContainerType type;
	
	public TransportOrderShippingContainer() {};
	
	public TransportOrderShippingContainer(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, boolean calculateOnly, Date lastModifiedDate, ManagedCompany company,
			TransportOrder transportOrder, String number, ShippingContainerType type) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.transportOrder = transportOrder;
		this.number = number;
		this.type = type;
	}
}
