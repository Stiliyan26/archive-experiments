package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcContract extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- primary key
	private String code; //NOT NULL, -- organization unit
	private String name; //NOT NULL, -- unique
	@Column(length = 3000)
	private String descr; //-- more detailed description
	@ManyToOne
	private CCcPartner parId; // NOT NULL, -- more detailed description
	private String contractNo; // -- FK to cc_partners
	private LocalDate contractStartDate;
	private LocalDate contractEndDate;
	private String status; //-- pending, in progress, finished, abandomed, etc...  in ref_data
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date, -- pending, in progress, finished, abandomed, etc...  in ref_data
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private LocalDate contractDate; //NOT NULL DEFAULT (now())::date,
	@ManyToOne
	FCtContractType cteId;
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccCotId")
	private List<FInvInvoice> fInvInvoices;
}

/* COMMENT ON TABLE register.cc_contracts
IS '! !
8. cot
Dogovori';
COMMENT ON COLUMN register.cc_contracts.id IS 'primary key';
COMMENT ON COLUMN register.cc_contracts.out_code IS 'primary key';
COMMENT ON COLUMN register.cc_contracts.code IS 'organization unit';
COMMENT ON COLUMN register.cc_contracts.name IS 'unique';
COMMENT ON COLUMN register.cc_contracts.descr IS 'more detailed description';
COMMENT ON COLUMN register.cc_contracts.par_id IS 'more detailed description';
COMMENT ON COLUMN register.cc_contracts.contract_no IS 'FK to cc_partners';
COMMENT ON COLUMN register.cc_contracts.status IS 'pending, in progress, finished, abandomed, etc...  in ref_data';
COMMENT ON COLUMN register.cc_contracts.active_from_date IS 'pending, in progress, finished, abandomed, etc...  in ref_data'; */