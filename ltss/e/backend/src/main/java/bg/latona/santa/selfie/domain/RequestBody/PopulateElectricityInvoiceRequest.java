package bg.latona.santa.selfie.domain.RequestBody;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PopulateElectricityInvoiceRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodFrom;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate periodTo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate taxEventDate;

    private Long documentTypeCode;

    private Set<String> agreementTypeCodes;

    private Set<Long> powerPlantIds;
}
