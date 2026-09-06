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
public class AgreementSelfInvoicingDTO {

    private String agreementSelfInvoicingId;

    private int stateCode;

    private String stateCodeText;

    private int agreementVatType;

    private String agreementVatTypeText;

    private String egn;

    private String name;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime agreementStartDate;

    private String agreementStartDateText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime agreementEndDate;

    private String agreementEndDateText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime signedOn;

    private String signedOnText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime createdOn;

    private String createdOnText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime terminatedOn;

    private String terminatedOnText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime terminationDate;

    private String terminationDateText;

    private Integer reasonForTerminationId;

    private String reasonForTerminationText;

    private Integer clientResponse;

    private String clientResponseText;

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    @JsonDeserialize(using = ZonedDateTimeDeserializer.class)
    private ZonedDateTime vatRegistrationDate;

    private String vatRegistrationDateText;

    private String vatNumber;

    private int typesOfServices;

    private String typesOfServicesText;

    private String customerRepresentative1Id;

    private String customerRepresentative1Text;

    private String customerRepresentative2Id;

    private String customerRepresentative2Text;

    private String customerId;

    private String customerText;

    private int statusCode;

    private String statusCodeText;
}
