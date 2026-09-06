package bg.latona.santa.selfie.domain.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrmRequestMock {

	private String actionPoint;
	private String address;

	private String accountnumber; // EIC_PIN
	private String selfie_egn; // EGN
	private String selfie_sapnumber; // SAP Number
	private String name; // Customer name


	private LocalDateTime createdon; // Date and time when the record was created.
	private LocalDateTime selfie_agreementenddate; // До коя дата е валидно споразумението. Съвпада с крайната дата на договора и не се променя
	private String selfie_agreementselfinvoicingid; //Unique identifier for entity instances
	private LocalDateTime selfie_agreementstartdate; //От коя дата влиза в сила споразумението
	private Long selfie_agreementvattype; // isVatIncluded
//	private Boolean isVatIncluded;
	private Long selfie_clientresponse; // isClientResponseAccept
//	private Boolean isClientResponseAccept;
	private String selfie_customer; //LookUp LegalPerson
	private String selfie_customerrepresentative1;//LookUp LegalPerson
	private String selfie_customerrepresentative2;//LookUp LegalPerson
//	private String selfie_egn; //LookUp LegalPerson
//	private String selfie_name; //The name of the custom entity.

	private Long selfie_reasonfortermination;
	private Long selfie_signatureschema; // LookUpTargets: selfie_signatureschema
	private LocalDateTime selfie_signedon; // Кога споразумението е отбелязано за подписано в Dynamics
	private LocalDateTime selfie_terminatedon; // Кога е извършено прекратяването в Dynamics
	private LocalDateTime selfie_terminationdate; // Дата на която изтича споразумението ако е прекратено предсрочно

	private Long selfie_typesofservices;
	private String selfie_vatnumber;
	private LocalDateTime selfie_vatregistrationdate;
	private LocalDateTime modifiedon; // Date and time when the record was modified.
	private Long statecode; // Status of the Agreement Self Invoicing
	private Long statuscode; // Reason for the status of the Agreement Self Invoicing



//	private String createdon;
	private LocalDateTime selfie_activatedon;
	private String selfie_agreementselfinvoicing; // Lookup Targets:selfie_agreementselfinvoicing
	private String selfie_agreementselfinvoicingname;
	private String selfie_bic;
	private BigDecimal selfie_creditnoticerangefrom;
	private BigDecimal selfie_creditnoticerangeto;
	private LocalDateTime selfie_deactivatedon;
	private BigDecimal selfie_debitnoticerangefrom;
	private BigDecimal selfie_debitnoticerangeto;
	private String selfie_iban;
	private String selfie_invoiceemail1;
	private String selfie_invoiceemail2;
	private BigDecimal selfie_invoicerangefrom;
	private BigDecimal selfie_invoicerangeto;
//	private String selfie_name;
	private String selfie_selfinvoicinglineid;
	private String selfie_veicontract;
	private String selfie_veicontractname;
	private String selfie_veimpn;
	private String selfie_veimpnname;
	private Long statecodeSelfInvoicingLine;
	private String statecodename;
	private Long statuscodeSelfInvoicingLine;
	private String statuscodename;

	private String selfie_externalnumber;
//	private String selfie_gridoperator;
//	private String selfie_gridoperatorname;
	private BigDecimal selfie_installedcapacitymwh;
	private String selfie_name;
	private String selfie_renewablename;
	private Long selfie_type;

	private String new_codpostal; // Postal Code
	private String new_localitate; // City
	private String new_numar; // Number Text
	private String new_strada; //Street
	private String new_streettext; // Street (Text)

	private String veiBgNumber;
	private String veiName;
	private Long veiType;
	private String  veiTypeText;
	private BigDecimal veiInstalledCapacityMwh;
	private String veiInstalledCapacityMwhText;
	private String gridOperatorName;
	private String externalNumber;

	AccountCrm account;





}
