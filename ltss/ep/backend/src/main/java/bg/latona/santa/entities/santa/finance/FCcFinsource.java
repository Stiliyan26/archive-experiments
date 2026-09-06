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
@ToString(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcFinsource extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private String code; //NOT NULL, -- organization unit
	private String name; //NOT NULL, -- unique
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date,
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccFieId")
	private List<FInvInvoice> fInvInvoices;
}
/* COMMENT ON TABLE register.cc_finsources
IS '! !
5. fie
programi, proekti, obekti - lineyna';
COMMENT ON COLUMN register.cc_finsources.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_finsources.code IS 'organization unit';
COMMENT ON COLUMN register.cc_finsources.name IS 'unique'; */