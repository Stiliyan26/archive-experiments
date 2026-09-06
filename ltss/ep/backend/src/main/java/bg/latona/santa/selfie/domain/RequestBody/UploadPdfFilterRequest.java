package bg.latona.santa.selfie.domain.RequestBody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadPdfFilterRequest {

    private Set<String> accessPoints;
}