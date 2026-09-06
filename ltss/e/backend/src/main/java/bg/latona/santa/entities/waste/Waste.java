package bg.latona.santa.entities.waste;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import java.math.BigDecimal;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class Waste extends CompanyRecord {

	@ManyToOne
	WasteCostCenter costCenter;
	@ManyToOne
	WasteType wasteType;
	private Date generatedDate;
	private Date processedDate;
	private BigDecimal generatedAmount;
	private BigDecimal processedAmount;


	public Waste() {};

	public Waste(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Date generatedDate, BigDecimal generatedAmount, BigDecimal processedAmount, Date processedDate,
			WasteCostCenter costCenter, WasteType wasteType) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.costCenter = costCenter;
		this.wasteType = wasteType;
		this.generatedDate = generatedDate;
		this.processedDate = processedDate;
		this.generatedAmount = generatedAmount;
		this.processedAmount = processedAmount;
	}
}
