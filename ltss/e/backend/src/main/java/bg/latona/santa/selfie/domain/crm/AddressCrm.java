package bg.latona.santa.selfie.domain.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressCrm {

	private String addressId;
	private String streetText;
	private String number;
	private String streetId;
	private String streetName;
	private String cityId;
	private String cityName;
	private String postalCode;

}
