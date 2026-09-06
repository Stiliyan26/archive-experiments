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
@ToString(exclude = {"fCcFunctions","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCcFunctions","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcFunction extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private String code; //NOT NULL, -- organization unit
	private String name; //NOT NULL, -- unique
	@Column(length = 3000)
	private String descr; //NOT NULL,
	private String source; // -- ?
	@ManyToOne
	private FCcFunction funIdUp; //-- ?
	private String fType; //-- parent id
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date, -- ?
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "funIdUp")
	private List<FCcFunction> fCcFunctions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFunId")
	private List<FInvInvoice> fInvInvoices;
}
/* COMMENT ON TABLE register.cc_functions
IS '! !
3. fun
funkcii, grpupi, deynosti - yerarhichna';
COMMENT ON COLUMN register.cc_functions.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_functions.code IS 'organization unit';
COMMENT ON COLUMN register.cc_functions.name IS 'unique';
COMMENT ON COLUMN register.cc_functions.source IS '?';
COMMENT ON COLUMN register.cc_functions.fun_id_up IS '???';
COMMENT ON COLUMN register.cc_functions.f_type IS 'parent id';
COMMENT ON COLUMN register.cc_functions.active_from_date IS '???'; */