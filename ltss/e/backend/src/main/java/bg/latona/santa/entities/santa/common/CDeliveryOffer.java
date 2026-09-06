package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@ToString(exclude = {"cDeliveryOfferItems"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cDeliveryOfferItems"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CDeliveryOffer extends CompanyRecord{
	@ManyToOne
	private CCcPartner parId;
	private String offerNo;
	private Date dateOffer;
	private LocalDate startDate;
	private LocalDate endDate;
	private BigDecimal discount;
	private String status;
	@Column(length= 3000)
	private String remarks;
	@ManyToOne
	private CCcOrganizationUnit outCode;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dorId")
	private List<CDeliveryOfferItem> cDeliveryOfferItems;

	public CDeliveryOffer() {
		super();
	}
	
	public CDeliveryOffer(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CCcPartner parId, String offerNo, Date dateOffer,
			LocalDate startDate, LocalDate endDate, BigDecimal discount, String status, String remarks,
			CCcOrganizationUnit outCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.parId = parId;
		this.offerNo = offerNo;
		this.dateOffer = dateOffer;
		this.startDate = startDate;
		this.endDate = endDate;
		this.discount = discount;
		this.status = status;
		this.remarks = remarks;
		this.outCode = outCode;
	}	 
	
}
