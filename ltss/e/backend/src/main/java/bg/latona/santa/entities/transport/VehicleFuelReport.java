package bg.latona.santa.entities.transport;

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
public class VehicleFuelReport extends CompanyRecord {

	private Date issueDate;
	private BigDecimal odometer;
	private BigDecimal fuelInTank;
	@ManyToOne
	private Vehicle vehicle;

	
	public VehicleFuelReport() {};
	
	public VehicleFuelReport(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Date issueDate, BigDecimal odometer, BigDecimal fuelInTank, Vehicle vehicle) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.issueDate = issueDate;
		this.odometer = odometer;
		this.fuelInTank = fuelInTank;
		this.vehicle = vehicle;
	}
}
