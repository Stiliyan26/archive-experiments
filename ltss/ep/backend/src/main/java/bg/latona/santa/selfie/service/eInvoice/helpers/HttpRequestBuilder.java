package bg.latona.santa.selfie.service.eInvoice.helpers;

import bg.latona.santa.reports.ReportException;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Component
public class HttpRequestBuilder {

    private static final Map<String, BiFunction<RequestBody, Request.Builder, Request.Builder>> methodMap = new HashMap<>();

    static {
        methodMap.put("POST", (body, builder) -> builder.post(body));
        methodMap.put("PUT", (body, builder) -> builder.put(body));
        methodMap.put("DELETE", (body, builder) -> {
            if (body != null) {
                return builder.delete(body);
            } else {
                return builder.delete();
            }
        });
        methodMap.put("GET", (body, builder) -> builder.get());
    }

    public Request buildRequest(HttpRequestParameters soapRequestParameters) {
        Request.Builder builder = new Request.Builder()
                .url(soapRequestParameters.getUrl())
                .addHeader("Content-Type", soapRequestParameters.getContentType());

        BiFunction<RequestBody, Request.Builder, Request.Builder> method = methodMap.get(soapRequestParameters.getHttpMethod().toUpperCase());

        if (method == null) {
            throw new ReportException("Unsupported HTTP method: " + soapRequestParameters.getHttpMethod());
        }

        builder = method.apply(soapRequestParameters.getBody(), builder);

        return builder.build();
    }
}
