package bg.latona.santa.entities.santa.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import java.time.LocalDate;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"cCtBankAccounts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCtBankAccounts"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class CCtBank extends CompanyRecord {

	private String name;
	private String nameEn;
	private String bic;
	private LocalDate activeFrom;
	private LocalDate activeTo;
	private String format; // is it used?

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bank")
	private List<CCtBankAccount> cCtBankAccounts;
}
