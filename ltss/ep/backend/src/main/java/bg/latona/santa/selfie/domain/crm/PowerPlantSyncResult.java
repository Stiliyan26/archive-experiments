package bg.latona.santa.selfie.domain.crm;

import lombok.Getter;

@Getter
public class PowerPlantSyncResult extends CommonSyncResult {

    private final String accessPoint;

    public PowerPlantSyncResult(String accessPoint, boolean success, String message) {
        super(success, message);
        this.accessPoint = accessPoint;
    }
}

