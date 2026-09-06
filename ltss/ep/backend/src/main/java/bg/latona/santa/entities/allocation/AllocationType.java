package bg.latona.santa.entities.allocation;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import org.hibernate.envers.NotAudited;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data //auto-create getters and setters
@ToString(exclude = {"allocationProxies"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"allocationProxies"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "name"})) //for all unique constraints should be put Drools UNIQUE rules too
public class AllocationType extends CompanyRecord {

	String name;
	@Column(length= 3000)
	String producer;
	@Column(length= 3000)
	String consumer;

	@JsonIgnore //avoid serialization recursion by Jackson
	@NotAudited
	@OneToMany(mappedBy = "allocationType")
	private List<AllocationProxy> allocationProxies;

	public AllocationType() {}

	public AllocationType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String producer, String consumer) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.producer = producer;
		this.consumer = consumer;
	}

}
