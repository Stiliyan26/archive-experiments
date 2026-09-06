package bg.latona.santa.entities.allocation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"consumerAllocationRecords","producerAllocationRecords"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"consumerAllocationRecords","producerAllocationRecords"}) //avoid recursion by Lombok
//@Audited //entities with high number of changes shouldn't be audited to avoid DB fill
@Entity //JPA persisted class
@Inheritance(strategy = InheritanceType.JOINED) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public class AllocationProxy extends CompanyRecord {

	@ManyToOne
	private AllocationOrigin allocationOrigin;
	@ManyToOne
	private AllocationType allocationType;
	private BigDecimal quantityToAllocate;
	private BigDecimal totalAllocatedQuantity;
	private Boolean fullyAllocated;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@NotAudited
	@OneToMany(mappedBy = "allocationConsumer")
	private List<AllocationRecord> consumerAllocationRecords;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@NotAudited
	@OneToMany(mappedBy = "allocationProducer")
	private List<AllocationRecord> producerAllocationRecords;

	//default empty constructor
	public AllocationProxy() {}

	public AllocationProxy(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			AllocationOrigin allocationOrigin, AllocationType allocationType, BigDecimal quantityToAllocate, BigDecimal totalAllocatedQuantity, Boolean fullyAllocated) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.allocationOrigin = allocationOrigin;
		this.allocationType = allocationType;
		this.quantityToAllocate = quantityToAllocate;
		this.totalAllocatedQuantity = totalAllocatedQuantity;
		this.fullyAllocated = fullyAllocated;
	}	
}
