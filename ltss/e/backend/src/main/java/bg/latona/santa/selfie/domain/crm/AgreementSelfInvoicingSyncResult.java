package bg.latona.santa.selfie.domain.crm;

import lombok.Getter;

@Getter
public class AgreementSelfInvoicingSyncResult extends CommonSyncResult {

    private final String accessPoint;

    public AgreementSelfInvoicingSyncResult(String accessPoint, boolean success, String message) {
        super(success, message);
        this.accessPoint = accessPoint;
    }
}