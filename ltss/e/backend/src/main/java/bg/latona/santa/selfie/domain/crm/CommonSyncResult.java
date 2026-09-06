package bg.latona.santa.selfie.domain.crm;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonSyncResult {

    private final boolean success;

    private final String message;
}
