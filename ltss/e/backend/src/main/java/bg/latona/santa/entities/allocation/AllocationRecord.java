package bg.latona.santa.entities.allocation;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

//import org.hibernate.envers.Audited;

import lombok.Data;

@Data //auto-create getters and setters
//@Audited
@Entity //JPA persisted class
public class AllocationRecord extends CompanyRecord {

	private BigDecimal allocatedQuantity;
	@ManyToOne
	private AllocationProxy allocationConsumer;
	@ManyToOne
	private AllocationProxy allocationProducer;
	
	public AllocationRecord() {};
	
	public AllocationRecord(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			BigDecimal allocatedQuantity, AllocationProxy allocationConsumer, AllocationProxy allocationProducer) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.allocatedQuantity = allocatedQuantity;
		this.allocationConsumer = allocationConsumer;
		this.allocationProducer = allocationProducer;
	}
}
