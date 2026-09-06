package bg.latona.santa.entities.transport;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"transports","transportOrderShippingContainers"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"transports","transportOrderShippingContainers"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too - in ReportsRepository.checkForUniqueCommonRecord
public class ShippingContainerType extends CompanyRecord {
	
	private String code;
	private String description;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "containerType")
	private List<Transport> transports;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<TransportOrderShippingContainer> transportOrderShippingContainers;
	
	public ShippingContainerType() {};
	
	public ShippingContainerType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String code, String description) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.description = description;
	}
}
