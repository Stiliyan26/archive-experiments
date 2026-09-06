package bg.latona.santa.selfie.domain.RequestBody;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class SynchronizeRequest {

    @JsonProperty("mpid")
    private String mpid;
}