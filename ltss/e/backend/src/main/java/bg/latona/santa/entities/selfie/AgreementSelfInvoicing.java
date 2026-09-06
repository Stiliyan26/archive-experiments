package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.nepal.PowerPlant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.ZonedDateTime;
import java.util.List;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"agreementsSelfInvoicing", "selfInvoicingLines"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"agreementsSelfInvoicing", "selfInvoicingLines"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class AgreementSelfInvoicing extends CompanyRecord {

	@ManyToOne
	private AgreementSelfInvoicing agreementSelfInvoicingId;
	private String agreementSelfInvoicingIdCode;

	private ZonedDateTime createdOn;

	private ZonedDateTime agreementStartDate;

	private ZonedDateTime agreementEndDate;

	private Boolean isVatIncluded;

	private String vatNumber;

	private ZonedDateTime vatRegistrationDate;

	private Boolean isClientResponseAccept;

	private String customer; //LookUp LegalPerson //Not added

	private String customerRepresentativeFirst;//LookUp LegalPerson //Not added

	private String customerRepresentativeSecond;//LookUp LegalPerson //Not added

	private String egn; //Todo check whether it is for a LegalPerson

	private String ownName; //Todo check whether it is for a PowerPlant

	@ManyToOne
	private LoiReasonForTermination reasonForTermination;

	private ZonedDateTime terminatedOn;

	private ZonedDateTime terminationDate;

	private ZonedDateTime signedOn;

	@ManyToOne
	private LoiTypeOFService typeOFService;

	private ZonedDateTime modifiedOn; // Not added

	private Boolean isStateCodeStatusActive;

	@ManyToOne
	private LoiAgreementStatus agreementStatus;

	@ManyToOne
	private PowerPlant powerPlant;


	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "agreementSelfInvoicingId")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "agreementSelfInvoicing")
	private List<SelfInvoicingLine> selfInvoicingLines;
}
