package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class IbexPrice extends CompanyRecord {

	private LocalDate localDate;
	private BigDecimal hour;
	private BigDecimal priceEUR;
	private BigDecimal priceBGN;
	private BigDecimal volume;

}
