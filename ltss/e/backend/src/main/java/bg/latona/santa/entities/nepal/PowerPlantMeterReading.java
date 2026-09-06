package bg.latona.santa.entities.nepal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "ix_PowerPlantMeterReading_identificationStartTS", columnList = "identification ,startts")
	})
public class PowerPlantMeterReading extends CompanyRecord {

	private String identification;
	private LocalDateTime startTS;
	private LocalDateTime endTS;
	private BigDecimal quantityKwh;

}
