package bg.latona.santa.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "dtype", "listOptionItemCode"})) //for all unique constraints should be put Drools UNIQUE rules too - in ReportsRepository.checkForUniqueCommonRecord
public abstract class ListOptionItem extends CompanyRecord {
	
	private String listOptionItemName;
	private Long listOptionItemCode;
	
	public ListOptionItem() {};
	
	public ListOptionItem(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.listOptionItemName = listOptionItemName;
		this.listOptionItemCode = listOptionItemCode;
	}
}
