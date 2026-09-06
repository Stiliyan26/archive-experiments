package bg.latona.santa.selfie.dtos.Crm;

import bg.latona.santa.selfie.util.ZonedDateTimeDeserializer;
import bg.latona.santa.selfie.util.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelfInvoicingLineDTO {

    private String selfInvoicingLineId;

    private String name;

    private String invoiceEmail1;

    private String invoiceEmail2;

    private String iban;

    private String bic;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime activatedOn;

    private String activatedOnText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime deactivatedOn;

    private String deactivatedOnText;

    private int statusCode;

    private String statusCodeText;

    private int stateCode;

    private String stateCodeText;

    private String agreementSelfInvoicingId;

    private String agreementSelfInvoicingText;

    private String veiMpId;

    private String veiMpText;

    private AgreementSelfInvoicingDTO agreementSelfInvoicing;
}
