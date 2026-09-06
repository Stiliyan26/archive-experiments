package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fPtJournals","fBreTransitions","fPtBatchCcDetails","fPtClosingAccounts","fPtBatchLinks","bahId2FPtBatchLinks","vodBahIdFInvInvoices"
,"fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtJournals","fBreTransitions","fPtBatchCcDetails","fPtClosingAccounts","fPtBatchLinks","bahId2FPtBatchLinks","vodBahIdFInvInvoices"
,"fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtBatch extends CompanyRecord {

	private LocalDate postDate; //NOT NULL,
	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit code
	private String module; //NOT NULL, -- source module, than created the batch
	private String refNo; //NOT NULL, -- source module, than created the batch
	private LocalDate refDate; //-- - source document date
	private BigDecimal amount; //NOT NULL, -- total amount to be posted
	private BigDecimal amountOutstanding; // NOT NULL, -- outstaning amount - amount to be payed off
	private BigDecimal amount2;
	private BigDecimal amount3;
	private LocalDate dueDate; //-- end of a payment term
	@Column(length = 3000)
	private String descr;
	private String corrRefNo;
	@ManyToOne
	private FInvDdsFile dfeId;
	private String salePeriod;
	private String purchasePeriod;
	@ManyToOne
	private FInvDdsFile dfeId2; //-- DDS file id in purchase for only protocol invoice when sale_period <> purchase_period
	@ManyToOne
	private FCtBatchType bteId; //NOT NULL,
	@ManyToOne
	private CCcPartner parId;
	@ManyToOne
	private FCtInvDealType ideId;
	@ManyToOne
	private CCtCurrency cuyCode; //NOT NULL DEFAULT 'BGN'::character varying,
	private BigDecimal cuyRate; //NOT NULL DEFAULT 1,
	private Integer cuyUnit; //NOT NULL DEFAULT 1,
	private BigDecimal amountCurrency;
	private BigDecimal amountOutstandingCurrency;
	private BigDecimal amount2Currency;
	private BigDecimal amount3Currency;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId")
	private List<FPtJournal> fPtJournals;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId")
	private List<FPtClosingAccount> fPtClosingAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId1")
	private List<FPtBatchLink> fPtBatchLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId2")
	private List<FPtBatchLink> bahId2FPtBatchLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "vodBahId")
	private List<FInvInvoice> vodBahIdFInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bahId")
	private List<FInvInvoice> fInvInvoices;
}

/*COMMENT ON TABLE accounting.pt_batches
IS '! bah !
Batch - higest level in posting - parvichen dokument';
COMMENT ON COLUMN accounting.pt_batches.out_code IS 'organization unit code';
COMMENT ON COLUMN accounting.pt_batches.module IS 'source module, than created the batch';
COMMENT ON COLUMN accounting.pt_batches.ref_no IS 'source reference number, i.e. invoice no, declaration no, etc.';
COMMENT ON COLUMN accounting.pt_batches.ref_date IS '- source document date';
COMMENT ON COLUMN accounting.pt_batches.amount IS 'total amount to be posted';
COMMENT ON COLUMN accounting.pt_batches.amount_outstanding IS 'outstaning amount - amount to be payed off';
COMMENT ON COLUMN accounting.pt_batches.due_date IS 'end of a payment term';
COMMENT ON COLUMN accounting.pt_batches.dfe_id2 IS 'DDS file id in purchase for only protocol invoice when sale_period <> purchase_period'; */
