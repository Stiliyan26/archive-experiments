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
@ToString(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtPostings","fBreTransitions","fPtBatchCcDetails","fHpCcBalances","fPtCcBalances","fInvInvoices"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCcReserve2 extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private String name; // NOT NULL, -- organization unit
	private String code; //NOT NULL, -- unique
	private String type1; //-- unique
	private String type2; //--  in ref_data
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date, --  in ref_data
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	private String outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccRe2Id")
	private List<FInvInvoice> fInvInvoices;
}
/* COMMENT ON TABLE register.cc_reserve2
IS '! !
10. re2
Rezerv 2 - svobodna lineyna klasifikaciya - da se izpolzva pri nuzhda ot klienta';
COMMENT ON COLUMN register.cc_reserve2.out_code IS 'organization unit';
COMMENT ON COLUMN register.cc_reserve2.name IS 'organization unit';
COMMENT ON COLUMN register.cc_reserve2.code IS 'unique';
COMMENT ON COLUMN register.cc_reserve2.type1 IS 'unique';
COMMENT ON COLUMN register.cc_reserve2.type2 IS '!RESERVE2_TYPE1! in ref_data';
COMMENT ON COLUMN register.cc_reserve2.active_from_date IS '!RESERVE2_TYPE2! in ref_data'; */