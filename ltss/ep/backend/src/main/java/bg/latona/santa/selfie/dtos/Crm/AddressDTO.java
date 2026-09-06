package bg.latona.santa.selfie.dtos.Crm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private String addressId;

    private String streetText;

    private String number;

    private String streetId;

    private String streetName;

    private String cityId;

    private String cityName;

    private String postalCode;
}
