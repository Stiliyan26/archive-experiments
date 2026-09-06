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

    @Value("${application.base.url:https://127.0.0.1:8443/api}")
    private String baseUrl;

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

    private static final String X_API_KEY = "X-Api-Key";

    private final OkHttpClient httpClient;
    private final MapperUtils mapperUtils;

    @Override
    public CustomerDTO fetchCustomerByEik(String eik) throws ReportException {
        log.info("Fetching customer by EIK: {}", eik);
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
        log.info("Fetching VeiDTO by MPID: {}", mpid);
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
        log.info("Fetching SelfInvoicingLine by MPID: {}", mpid);
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
        log.debug("Constructed URL for resource fetch: {}", url);
        Request request = buildGetRequest(url);
        log.debug("Built GET request for URL: {}", url);
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
        log.debug("Constructed URL for resource list fetch: {}", url);
        Request request = buildGetRequest(url);
        log.debug("Built GET request for URL: {}", url);
        return executionMethod.apply(request, responseType);
    }

    private Request buildGetRequest(String url) {
        log.debug("Building GET request for URL: {}", url);

        return new Request.Builder()
                .url(url)
                .headers(createHeaders())
                .get()
                .build();
    }

    private Headers createHeaders() {
        log.debug("Creating headers for CRM request.");

        String bearerToken = getBearerToken();

        log.debug("Obtained bearer token for headers.");

        return new Headers.Builder()
                .add(X_API_KEY, authKeyValue)
                .add("Accept", "application/json")
                .add("Content-Type", "application/json")
                .add("Authorization", bearerToken)
                .build();

//        return new Headers.Builder()
//                .add(X_API_KEY, authKeyValue)
//                .add("Accept", "application/json")
//                .build();
    }

    private String getBearerToken() {
        log.debug("Attempting to retrieve bearer token via login.");
        FormBody loginFormBody = new FormBody.Builder()
                .add("username", "admin1")
                .add("password", "123")
                .build();

        Request loginRequest = new Request.Builder()
                .url(baseUrl + "/login")
                .post(loginFormBody)
                .build();

        try (Response loginResponse = httpClient.newCall(loginRequest).execute()) {
            log.debug("Login request sent. Waiting for response.");

            if (!loginResponse.isSuccessful() || loginResponse.body() == null) {
                log.error("Login failed with status code: {}", loginResponse.code());
                throw new ReportException("Failed to obtain authentication token");
            }

            String loginResponseBody = loginResponse.body().string();
            log.debug("Login response received: {}", loginResponseBody);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode loginJson = mapper.readTree(loginResponseBody);

            String token = loginJson.get("Authorization").asText();
            log.info("Successfully obtained bearer token: {}", token);

            return token;
        } catch (Exception e) {
            log.error("Error occurred during login request: ", e);
            throw new ReportException(e.getMessage());
        }
    }

    private <T> T executeRequest(Request request, Class<T> dataType) {
        log.debug("Executing request: {} with headers: {}", request.url(), request.headers());
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Unsuccessful response from CRM. URL: {} Status: {} Response: {}", request.url(), response.code(), response);
                throw new ReportException("Unsuccessful response!");
            }

            if (response.body() == null) {
                log.error("No response body received for URL: {}", request.url());
                throw new ReportException("No response body received!");
            }

            String responseBody = response.body().string();
            log.debug("Response Body from {}: {}", request.url(), responseBody);

            return mapperUtils.deserializeJson(responseBody, dataType);
        } catch (Exception e) {
            log.error("Exception during executing request {}: {}", request.url(), e.getMessage(), e);
            throw new ReportException(e.getMessage());
        }
    }

    private <T> List<T> executeRequestList(Request request, Class<T> dataType) {
        log.debug("Executing request for resource list: {} with headers: {}", request.url(), request.headers());
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Unsuccessful response from CRM for list. URL: {} Status: {}", request.url(), response.code());
                throw new ReportException("Unsuccessful response!");
            }

            if (response.body() == null) {
                log.error("No response body received for list URL: {}", request.url());
                throw new ReportException("No response body received!");
            }

            String responseBody = response.body().string();
            log.debug("Response Body for list from {}: {}", request.url(), responseBody);
            JavaType listType = mapperUtils.createListType(dataType);

            return mapperUtils.deserializeJson(responseBody, listType);
        } catch (Exception e) {
            log.error("Exception during executing list request {}: {}", request.url(), e.getMessage(), e);
            throw new ReportException(e.getMessage());
        }
    }
}
