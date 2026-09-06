package bg.latona.santa.entities.allocation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"allocationProxies"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"allocationProxies"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Inheritance(strategy = InheritanceType.JOINED) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public class AllocationOrigin extends CompanyRecord {
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@NotAudited
	@OneToMany(mappedBy = "allocationOrigin")
	private List<AllocationProxy> allocationProxies;

	//default empty constructor
	public AllocationOrigin() {
		super();
	}

	public AllocationOrigin(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company
			) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
	}
	
	public BigDecimal getQuantityToAllocate() throws Exception {
		throw new Exception("AllocationOrigin.getQuantityToAllocate must be overriden in class "+this.getClass());
	}
}