package bg.latona.santa.selfie.domain.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountCrm {

	private String accountId;
	private String accountNumber;
	private String name;
	private String egn;
	private String sapNo;
	private AddressCrm address;
}
