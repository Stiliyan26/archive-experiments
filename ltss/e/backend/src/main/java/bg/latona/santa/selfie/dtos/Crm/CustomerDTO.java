package bg.latona.santa.selfie.dtos.Crm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private String accountId;

    private String accountNumber;

    private String name;

    private String egn;

    private String sapNo;

    private AddressDTO address;
}
