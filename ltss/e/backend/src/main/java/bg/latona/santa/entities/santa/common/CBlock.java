package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cBlockedQuantities"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cBlockedQuantities"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CBlock extends CompanyRecord{
	
	private String typeDoc;
	private String documentNumber;
	@Column(length= 3000)
	private String remark;
	@ManyToOne
	private CCcOrganizationUnit outCode;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "blkId")
	private List<CBlockedQuantity> cBlockedQuantities;

	public CBlock() {
		super();
	}

	public CBlock(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
	String typeDoc, String documentNumber, String remark, CCcOrganizationUnit outCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.typeDoc = typeDoc;
		this.documentNumber = documentNumber;
		this.remark = remark;
		this.outCode = outCode;
	}

}
