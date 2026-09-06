package bg.latona.santa.entities.asset;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"assetAttachments","assetComments"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"assetAttachments","assetComments"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
//@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "name"})) //for all unique constraints should be put Drools UNIQUE rules too
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public abstract class Asset extends AllocationOrigin {

	@Column(length= 3000)
	private String assetName;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "asset")
	private List<AssetAttachment> assetAttachments;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "asset")
	private List<AssetComment> assetComments;
	
	public Asset() {};
	
	public Asset(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String assetName) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.assetName = assetName;
	}
}
