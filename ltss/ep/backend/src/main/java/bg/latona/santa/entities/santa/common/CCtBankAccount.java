package bg.latona.santa.entities.santa.common;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.finance.FInvInvoice;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data //auto-create getters and setters
@ToString(exclude = {"fInvInvoices","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fInvInvoices","cInvoices"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class CCtBankAccount extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String name;
	private String iban;
	@ManyToOne
	private CCtBank bank; //bakId
	private String code;
	@ManyToOne
	private LoiCtBankAccountBatType batType;
	@ManyToOne
	private CCtCurrency currency;
	private LocalDate activeFrom;
	private LocalDate activeTo;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "batId")
	private List<FInvInvoice> fInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bankAccount")
	private List<CInvoice> cInvoices;
}
