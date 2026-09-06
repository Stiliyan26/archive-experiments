package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.time.LocalDate;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fCcReserve1s","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCcReserve1s","fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcReserve1 extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	@ManyToOne
	private FCcReserve1 re1IdUp; //-- organization unit
	private String name; //NOT NULL, -- parent id
	private String code; //NOT NULL, -- unique
	private String type1; //-- unique
	private String type2; //--  in ref_data
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date, --  in ref_data
	private LocalDate activeToDate; // NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "re1IdUp")
	private List<FCcReserve1> fCcReserve1s;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe1Id")
	private List<FInvInvoice> fInvInvoices;
}
/* COMMENT ON TABLE register.cc_reserve1
IS '! !
9. re1
Rezerv 1 - svobodna yerarhichna klasifikaciya - da se izpolzva pri nuzhda ot klienta';
COMMENT ON COLUMN register.cc_reserve1.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_reserve1.re1_id_up IS 'organization unit';
COMMENT ON COLUMN register.cc_reserve1.name IS 'parent id';
COMMENT ON COLUMN register.cc_reserve1.code IS 'unique';
COMMENT ON COLUMN register.cc_reserve1.type1 IS 'unique';
COMMENT ON COLUMN register.cc_reserve1.type2 IS '!RESERVE1_TYPE1! in ref_data';
COMMENT ON COLUMN register.cc_reserve1.active_from_date IS '!RESERVE1_TYPE2! in ref_data'; */