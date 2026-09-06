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
@ToString(exclude = {"fCcEbks","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCcEbks","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcEbk extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private String code; //NOT NULL, -- organization unit
	private String name; //NOT NULL, -- unique
	@Column(length = 3000)
	private String descr;
	private String ebkGroup; //NOT NULL, -- unique
	@ManyToOne
	private FCcEbk ebkIdUp; //-- ?
	private String serbraCode; //-- parent id
	private String source; //-- ?
	@Column(length = 3000)
	private String formula; //-- ?
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date,
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ebkIdUp")
	private List<FCcEbk> fCcEbks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccEbkId")
	private List<FInvInvoice> fInvInvoices;

}
/*COMMENT ON TABLE register.cc_ebk
IS '! !
2. ebk
paragrafi ot EBK ( Edinen Byudzheten Klasifikator ) - yerarhichna';
COMMENT ON COLUMN register.cc_ebk.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_ebk.code IS 'organization unit';
COMMENT ON COLUMN register.cc_ebk.name IS 'unique';
COMMENT ON COLUMN register.cc_ebk.ebk_group IS '???';
COMMENT ON COLUMN register.cc_ebk.ebk_id_up IS '???';
COMMENT ON COLUMN register.cc_ebk.serbra_code IS 'parent id';
COMMENT ON COLUMN register.cc_ebk.source IS '???';
COMMENT ON COLUMN register.cc_ebk.formula IS '???'; */