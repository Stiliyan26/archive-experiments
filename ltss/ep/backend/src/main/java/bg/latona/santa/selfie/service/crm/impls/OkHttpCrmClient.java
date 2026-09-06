package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;
import bg.latona.santa.selfie.service.crm.interfaces.CrmClient;
import bg.latona.santa.selfie.util.MapperUtils;
import bg.latona.santa.reports.ReportException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Slf4j
@RequiredArgsConstructor
@Component
public class OkHttpCrmClient implements CrmClient {

    @Value("${crm.url.base:https://127.0.0.1:8443/api/reports}")
    private String crmBaseUrl;

    @Value("${crm.endpoint.url.findCustomerByEik:/reports/findCustomerByEik}")
    private String endpointFindCustomerByEik;

    @Value("${crm.endpoint.url.findMeteringPointByNumber:/reports/findMeteringPointByNumber}")
    private String endpointFindMeteringPointNumber;

    @Value("${crm.endpoint.url.getSelfInvoicingLine:/reports/getSelfInvoicingLine}")
    private String endpointGetSelfInvoicingLine;

    @Value("${crm.x-auth-key}")
    private String authKeyValue;

    private static final String X_AUTH_KEY = "X-Auth-Key";

    private final OkHttpClient httpClient;
    private final MapperUtils mapperUtils;

    @Override
    public CustomerDTO fetchCustomerByEik(String eik) throws ReportException {
        return fetchResource(
                endpointFindCustomerByEik,
                "eik",
                eik,
                this::executeRequest,
                CustomerDTO.class
        );
    }

    @Override
    public VeiDTO fetchVeiDtoByMpid(String mpid) throws ReportException {
        return fetchResource(
                endpointFindMeteringPointNumber,
                "mpid",
                mpid,
                this::executeRequest,
                VeiDTO.class
        );
    }

    @Override
    public List<SelfInvoicingLineDTO> fetchSelfInvoicingLineByMpid(String mpid) throws ReportException {
        return fetchResourceList(
                endpointGetSelfInvoicingLine,
                "mpid",
                mpid,
                this::executeRequestList,
                SelfInvoicingLineDTO.class
        );
    }

    // -----------------------------------------------
    // Internal Helpers
    // -----------------------------------------------
    private <T> T fetchResource(
            String endpoint,
            String queryParamKey,
            String queryParamValue,
            BiFunction<Request, Class<T>, T> executionMethod,
            Class<T> responseType
    ) throws ReportException {
        String url = crmBaseUrl + endpoint + "?" + queryParamKey + "=" + queryParamValue;

        Request request = buildGetRequest(url);

        return executionMethod.apply(request, responseType);
    }

    private <T> List<T> fetchResourceList(
            String endpoint,
            String queryParamKey,
            String queryParamValue,
            BiFunction<Request, Class<T>, List<T>> executionMethod,
            Class<T> responseType
    ) throws ReportException {
        String url = crmBaseUrl + endpoint + "?" + queryParamKey + "=" + queryParamValue;

        Request request = buildGetRequest(url);

        return executionMethod.apply(request, responseType);
    }

    private Request buildGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .headers(createHeaders())
                .get()
                .build();
    }

    private Headers createHeaders() {
        String bearerToken = getBearerToken();

        return new Headers.Builder()
                .add(X_AUTH_KEY, authKeyValue)
                .add("Accept", "application/json")
                .add("Content-Type", "application/json")
                .add("Authorization", bearerToken)
                .build();
    }

    private String getBearerToken() {

        FormBody loginFormBody = new FormBody.Builder()
                .add("username", "admin1")
                .add("password", "123")
                .build();

        Request loginRequest = new Request.Builder()
                .url("https://127.0.0.1:8443/api/login")
                .post(loginFormBody)
                .build();

        try (Response loginResponse = httpClient.newCall(loginRequest).execute()) {

            if (!loginResponse.isSuccessful() || loginResponse.body() == null) {
                log.error("Login failed with status code: {}", loginResponse.code());
                throw new ReportException("Failed to obtain authentication token");
            }

            String loginResponseBody = loginResponse.body().string();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode loginJson = mapper.readTree(loginResponseBody);

            return loginJson.get("Authorization").asText();

        } catch (Exception e) {
            log.error("Error occurred during login request: ", e);
            e.printStackTrace();

            throw new ReportException(e.getMessage());
        }
    }

    private <T> T executeRequest(Request request, Class<T> dataType) {

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ReportException("Unsuccessful response!");
            }

            if (response.body() == null) {
                throw new ReportException("No response body received!");
            }

            String responseBody = response.body().string();
            log.info("Response Body: {}", responseBody);

            return mapperUtils.deserializeJson(responseBody, dataType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReportException(e.getMessage());
        }
    }

    private <T> List<T> executeRequestList(Request request, Class<T> dataType) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new ReportException("Unsuccessful response!");
            }

            if (response.body() == null) {
                throw new ReportException("No response body received!");
            }

            String responseBody = response.body().string();
            log.info("Response Body: {}", responseBody);

            JavaType listType = mapperUtils.createListType(dataType);

            return mapperUtils.deserializeJson(responseBody, listType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ReportException(e.getMessage());
        }
    }
}
