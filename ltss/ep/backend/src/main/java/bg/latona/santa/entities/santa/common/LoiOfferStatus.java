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
@ToString(exclude = {"cOffers","cOfferStatuses","oldStatusCOfferStatuses"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cOffers","cOfferStatuses","oldStatusCOfferStatuses"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiOfferStatus extends ListOptionItem {

	public static final Long OFFER_STATUS_A = 2L;
	public static final Long OFFER_STATUS_RA = 1L;
	public static final Long OFFER_STATUS_PI = 3L;
	public static final Long OFFER_STATUS_SA = 4L;
	public static final Long OFFER_STATUS_CA = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "status")
	private List<COffer> cOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "oldStatus")
	private List<COfferStatus> oldStatusCOfferStatuses;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "status")
	private List<COfferStatus> cOfferStatuses;

	public LoiOfferStatus() {
		super();
	}

	public LoiOfferStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
