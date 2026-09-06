package bg.latona.santa.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"users"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"users"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class ManagedCompany extends CommonRecord {

	private String name;
	private Long code;
	private String storeId;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "company")
	private List<SecUser> users;
	
	public ManagedCompany() {}

	public ManagedCompany(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly,
			String name, Long code, String storeId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly);
		this.name = name;
		this.code = code;
		this.storeId = storeId;
	}

}
