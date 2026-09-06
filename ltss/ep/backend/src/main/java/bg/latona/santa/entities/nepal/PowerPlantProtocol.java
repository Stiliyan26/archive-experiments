package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.mail.SendMailMessage;
import bg.latona.santa.entities.person.LegalPerson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class PowerPlantProtocol extends CompanyRecord {

	private byte[] pdfFile;
	private byte[] xlsxFile;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	@ManyToOne
	private LegalPerson owner;
	@ManyToOne
	private PowerPlant powerPlant;
	@ManyToOne
	private LoiContractQuantity loiContractQuantity;
	@ManyToOne
	private LoiContractPrice loiContractPrice;
	@ManyToOne
	private LoiProtocolLineCount loiProtocolLineCount;
	@ManyToOne
	private LoiProtocolStatus loiProtocolStatus;
	@ManyToOne
	private SendMailMessage sendMailMessage;

}
