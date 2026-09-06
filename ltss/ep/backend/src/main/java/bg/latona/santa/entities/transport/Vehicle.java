package bg.latona.santa.entities.transport;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.asset.Asset;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"transportsForTractors","transportsForTrailers","vehicleFuelReports"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"transportsForTractors","transportsForTrailers","vehicleFuelReports"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Vehicle extends Asset {

	@ManyToOne
	private LoiVehicleType vehicleType;
	private String licensePlate;
	private String modelName;
	private String ownerName;
	private String euroStandard;
	private Date purchaseDate;
	private Date manufactureDate;
	private BigDecimal purchaseOdometer;
	private String engineNo;
	private String chassisNo;
	private String enginePowerAndCc;
	private BigDecimal trailerHeight;
	private String fuelType;
	private String tankLiters;
	private String oil;
	private String antifreeze;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "tractorUnit")
	private List<Transport> transportsForTractors;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "trailer")
	private List<Transport> transportsForTrailers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "vehicle")
	private List<VehicleFuelReport> vehicleFuelReports;


	
	public Vehicle() {};
	
	public Vehicle(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String assetName,
			LoiVehicleType vehicleType, String licensePlate, String modelName, String ownerName, String euroStandard, Date purchaseDate, Date manufactureDate,
			BigDecimal purchaseOdometer, String engineNo, String chassisNo, String enginePowerAndCc, BigDecimal trailerHeight,
			String fuelType, String tankLiters, String oil, String antifreeze) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, assetName);
		this.vehicleType = vehicleType;
		this.licensePlate = licensePlate;
		this.modelName = modelName;
		this.ownerName = ownerName;
		this.euroStandard = euroStandard;
		this.purchaseDate = purchaseDate;
		this.manufactureDate = manufactureDate;
		this.purchaseOdometer = purchaseOdometer;
		this.engineNo = engineNo;
		this.chassisNo = chassisNo;
		this.enginePowerAndCc = enginePowerAndCc;
		this.trailerHeight = trailerHeight;
		this.fuelType = fuelType;
		this.tankLiters = tankLiters;
		this.oil = oil;
		this.antifreeze = antifreeze;
	}
}
