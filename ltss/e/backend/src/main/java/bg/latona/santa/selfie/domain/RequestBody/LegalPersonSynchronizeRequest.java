package bg.latona.santa.selfie.domain.RequestBody;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LegalPersonSynchronizeRequest {

    @JsonProperty("eik")
    private String eik;
}


