package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;


import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cCcPartners"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCcPartners"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiPartnerType extends ListOptionItem {

	public static final Long PARTNER_TYPE_CL = 1L;
	public static final Long PARTNER_TYPE_DV = 2L;
	public static final Long PARTNER_TYPE_CD = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "partnerType")
	private List<CCcPartner> cCcPartners;

	public LoiPartnerType() {
		super();
	}

	public LoiPartnerType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
