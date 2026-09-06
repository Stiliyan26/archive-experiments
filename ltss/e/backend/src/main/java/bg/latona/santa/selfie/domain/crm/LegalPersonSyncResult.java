package bg.latona.santa.selfie.domain.crm;

import lombok.Getter;

@Getter
public class LegalPersonSyncResult extends CommonSyncResult {

    private final String eik;

    public LegalPersonSyncResult(String eik, boolean success, String message) {
        super(success, message);
        this.eik = eik;
    }
}


