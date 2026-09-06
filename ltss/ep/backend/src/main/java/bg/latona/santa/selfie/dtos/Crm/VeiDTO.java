package bg.latona.santa.selfie.dtos.Crm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiDTO {

    private String veiId;

    private String veiBgNumber;

    private String veiName;

    private Long veiType;

    private String veiTypeText;

    private BigDecimal veiInstalledCapacityMwh;

    private String veiInstalledCapacityMwhText;

    private String gridOperatorId;

    private String gridOperatorName;

    private String externalNumber;

    private CustomerDTO account;
}
