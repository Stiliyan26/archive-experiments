package bg.latona.santa.entities.nepal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.Entity;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class PowerPlantProducedSchedule extends CompanyRecord {

	private String identification;
	private LocalDateTime startTS;
	private LocalDateTime endTS;
	private BigDecimal quantityKwh;

}
