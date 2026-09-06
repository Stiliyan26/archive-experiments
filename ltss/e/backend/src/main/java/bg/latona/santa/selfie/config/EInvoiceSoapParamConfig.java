package bg.latona.santa.selfie.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class EInvoiceSoapParamConfig {

    @Value("${efaktura.url}")
    private String url;

    //TSEE
    @Value("${tsee.efaktura.authorizationId}")
    private String tseeAuthorizationId;

    @Value("${tsee.efaktura.authorizationKey}")
    private String tseeAuthorizationKey;

    @Value("${tsee.identificationNumber}")
    private String tseeIdentificationNumber;

    //TES
    @Value("${tes.efaktura.authorizationId}")
    private String tesAuthorizationId;

    @Value("${tes.efaktura.authorizationKey}")
    private String tesAuthorizationKey;

    @Value("${tes.identificationNumber}")
    private String tesIdentificationNumber;


    public String getAuthorizationId(boolean useTes) {
        return useTes ? tesAuthorizationId : tseeAuthorizationId;
    }

    public String getAuthorizationKey(boolean useTes) {
        return useTes ? tesAuthorizationKey : tseeAuthorizationKey;
    }
}

