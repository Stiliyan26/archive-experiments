package bg.latona.santa.selfie.service.eInvoice.helpers;

import lombok.*;
import okhttp3.RequestBody;


@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class HttpRequestParameters {

    private final String url;

    private final String contentType;

    private final String httpMethod;

    private final RequestBody body;
}