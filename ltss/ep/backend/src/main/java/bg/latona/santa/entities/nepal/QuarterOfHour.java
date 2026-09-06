package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class QuarterOfHour extends CompanyRecord {

	private BigDecimal quarterOfHour;
	@Column(precision = 19, scale = 5)
	private BigDecimal value;
	@ManyToOne
	private PowerPlantProfile powerPlantProfile;
}
