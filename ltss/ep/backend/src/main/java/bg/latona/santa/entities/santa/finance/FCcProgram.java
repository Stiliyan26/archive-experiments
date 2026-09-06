package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
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
@ToString(exclude = {"fCcPrograms","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCcPrograms","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcProgram extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private String code; //NOT NULL, -- organization unit
	private String name; //NOT NULL, -- organization unit
	@Column(length = 3000)
	private String descr; // NOT NULL,
	@ManyToOne
	private FCcProgram prmIdUp; // -- parent id
	private String prmType; //-- parent id
	private String prmGroup; //-- ?
	private String sortCode; //-- ?
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date, -- ?
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "prmIdUp")
	private List<FCcProgram> fCcPrograms;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccPrmId")
	private List<FInvInvoice> fInvInvoices;
}
/* COMMENT ON TABLE register.cc_programs
IS '! !
4. prm
programi, proekti, obekti - yerarhichna';
COMMENT ON COLUMN register.cc_programs.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_programs.code IS 'organization unit';
COMMENT ON COLUMN register.cc_programs.name IS 'unique';
COMMENT ON COLUMN register.cc_programs.prm_id_up IS 'parent id';
COMMENT ON COLUMN register.cc_programs.prm_type IS 'parent id';
COMMENT ON COLUMN register.cc_programs.prm_group IS '?';
COMMENT ON COLUMN register.cc_programs.sort_code IS '???';
COMMENT ON COLUMN register.cc_programs.active_from_date IS '???'; */