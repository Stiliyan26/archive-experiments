package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class SelfInvoicingLine extends CompanyRecord {

	private ZonedDateTime createdOn; // Not added

	private ZonedDateTime activatedOn;

	private ZonedDateTime deActivatedOn;

	private String bic;

	private String iban;

	@ManyToOne
	private AgreementSelfInvoicing agreementSelfInvoicing;

//	private BigDecimal creditNoticeRangeFrom;

//	private BigDecimal creditNoticeRangeTo;

//	private BigDecimal debitNoticeRangeFrom;

//	private BigDecimal debitNoticeRangeTo;

//	private BigDecimal invoiceRangeFrom;

//	private BigDecimal invoiceRangeTo;

	private String invoiceEmail;

	private String invoiceEmailSec;

	private String ownName;

	private Boolean isActiveStateCode;

	@ManyToOne
	private LoiStatusCode statusCode;
}
