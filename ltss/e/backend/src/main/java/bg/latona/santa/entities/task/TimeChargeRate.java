package bg.latona.santa.entities.task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class TimeChargeRate extends CompanyRecord {

	@ManyToOne
	private SecUser provider;
	@ManyToOne
	private SecUser resource; //maybe should be new class ServiceType and/or change to usedResource
	private BigDecimal chargeRatePerHour;
	@ManyToOne
	private Currency currency;
	private LocalDate validFromDate;
	private LocalDate validToDate;
	
	public TimeChargeRate() {};
	
	public TimeChargeRate(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			SecUser provider, SecUser resource,
			BigDecimal chargeRatePerHour, Currency currency, LocalDate validFromDate, LocalDate validToDate) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.provider = provider;
		this.resource = resource;
		this.chargeRatePerHour = chargeRatePerHour;
		this.currency = currency;
		this.validFromDate = validFromDate;
		this.validToDate = validToDate;
	}
}
