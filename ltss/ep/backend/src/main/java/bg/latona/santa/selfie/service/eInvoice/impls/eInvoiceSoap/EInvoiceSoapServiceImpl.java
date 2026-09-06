package bg.latona.santa.selfie.service.eInvoice.impls.eInvoiceSoap;

import bg.latona.santa.selfie.config.EInvoiceSoapParamConfig;
import bg.latona.santa.selfie.domain.UploadZip;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLegalPersonService;
import bg.latona.santa.selfie.service.eInvoice.helpers.SoapEnvelopeBuilder;
import bg.latona.santa.selfie.service.eInvoice.helpers.XmlParser;
import bg.latona.santa.selfie.service.eInvoice.helpers.HttpRequestParameters;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceSoap.EInvoiceSoapService;
import bg.latona.santa.selfie.util.Base64Utils;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.reports.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class EInvoiceSoapServiceImpl implements EInvoiceSoapService {

    private final HttpClientServiceImpl httpClientService;
    private final SoapEnvelopeBuilder envelopeBuilder;
    private final XmlParser xmlParser;
    private final EInvoiceSoapParamConfig eInvoiceSoapParamConfig;
    private final DbLegalPersonService dbLegalPersonService;

    private String authorizationId = null;
    private String authorizationKey = null;

    //TODO: change logic if there is a new company
    private void setCredentials(LegalPerson senderLegalPerson) {
        if (senderLegalPerson == null || !senderLegalPerson.getIsSender()) {
            return;
        }

        boolean isTes = senderLegalPerson.getIdentificationNumber()
                .equals(eInvoiceSoapParamConfig.getTesIdentificationNumber());

        authorizationId = eInvoiceSoapParamConfig.getAuthorizationId(isTes);
        authorizationKey = eInvoiceSoapParamConfig.getAuthorizationKey(isTes);
    }

    public String uploadFile(UploadZip uploadZip) {
        if (uploadZip.getFileName() == null || uploadZip.getFileContent() == null) {
            log.error("File content or file name is null.");
            throw new ReportException("File content and file name must not be null.");
        }

        authorizationId = eInvoiceSoapParamConfig.getTseeAuthorizationId();
        authorizationKey = eInvoiceSoapParamConfig.getTseeAuthorizationKey();

        LegalPerson senderLegalPerson = dbLegalPersonService.getLoggedLegalPerson(true);

        setCredentials(senderLegalPerson);

        String encodedAuthId = Base64Utils.encode(authorizationId);
        String encodedAuthKey = Base64Utils.encode(authorizationKey);
        String encodedFileName = Base64Utils.encode(uploadZip.getFileName());

        String xmlPayload = envelopeBuilder.buildUploadFileEnvelope(
                encodedAuthId,
                encodedAuthKey,
                encodedFileName
        );

        String boundaryFirst = /*"_" +*/ "=_" + String.valueOf(UUID.randomUUID()).replace("-", "");
        String boundary = "uuid:" + "xxxx";
        String boundaryForCId = String.valueOf(UUID.randomUUID()).replace("-", "");;
        String contentType = "multipart/related; type=\"text/xml\"; boundary=\"" + boundaryFirst + "\"; start=\"<root.message@cxf.apache.org>\"; start-info=\"text/xml\"";
        String contentTypeSec = "multipart/related; type=\"text/xml\"; boundary=\"" + boundaryFirst + "\"";

		byte[] xmlPayloadBase = envelopeBuilder.buildUploadFileEnvelopeBase64(
				encodedAuthId,
				encodedAuthKey,
				encodedFileName,
				boundaryForCId
		);


		String multipartBody = buildRawMultipartBody(xmlPayloadBase, uploadZip.getFileContent(), boundaryFirst, boundaryForCId, uploadZip.getFileName());
		log.info("multipartBody uploadFile Request Body- {}", multipartBody);

        MediaType mediaType = MediaType.parse(contentTypeSec);
        RequestBody body = RequestBody.create(mediaType, multipartBody);

        HttpRequestParameters httpRequestParameters = HttpRequestParameters.builder()
                .url(eInvoiceSoapParamConfig.getUrl())
                .contentType(contentTypeSec)
                .httpMethod("POST")
                .body(body)
                .build();

        String uploadFileResponse = httpClientService.sendHttpRequest(httpRequestParameters);
        log.info("UploadFile Response: {}", uploadFileResponse);

        String fileTicket = xmlParser.extractValueByTagName(uploadFileResponse, "fileTicket");
        log.info("Extracted fileTicket: {}", fileTicket);

        String getFileResultResponse = getFileResult(fileTicket);

        return buildCombinedResponse(uploadFileResponse, getFileResultResponse);
    }

    public String getFileResult(String fileTicket) {
        if (fileTicket == null || fileTicket.isEmpty()) {
            log.error("File ticket is null or empty.");
            throw new ReportException("File ticket must not be null or empty.");
        }

        String encodedAuthId = Base64Utils.encode(authorizationId);
        String encodedAuthKey = Base64Utils.encode(authorizationKey);

        String soapEnvelope = envelopeBuilder.buildGetFileResultEnvelope(
                encodedAuthId,
                encodedAuthKey,
                fileTicket
        );

        String contentType = "application/xml";
        RequestBody body = RequestBody.create(MediaType.parse(contentType), soapEnvelope);
        log.info("getFileResult Request Body - {}", soapEnvelope);

        HttpRequestParameters httpRequestParameters = HttpRequestParameters.builder()
                .url(eInvoiceSoapParamConfig.getUrl())
                .contentType(contentType)
                .httpMethod("POST")
                .body(body)
                .build();

        String response = httpClientService.sendHttpRequest(httpRequestParameters);
        log.info("GetFileResult Response: {}", response);

        return response;
    }

    private String buildCombinedResponse(String uploadResponse, String getFileResultResponse) {
        return "{"
                + "\"uploadResponse\": " + uploadResponse + ", "
                + "\"getFileResultResponse\": " + getFileResultResponse
                + "}";
    }



	private String buildRawMultipartBody(byte[] xmlPayload, byte[] fileContent, String boundary, String boundaryForCId, String fileName) {
		String newline = "\r\n";

		// Part 1: SOAP XML Payload
		StringBuilder bodyBuilder = new StringBuilder();
		bodyBuilder.append("--").append(boundary).append(newline)
				.append("Content-Type: text/xml; charset=\"UTF-8\"").append(newline)
				.append("Content-Transfer-Encoding: base64").append(newline)
				.append(newline)
				.append(Base64.getEncoder().encodeToString(xmlPayload))
				.append(newline);

		// Part 2: File Content
		bodyBuilder.append("--").append(boundary).append(newline)
				.append("Content-Disposition: " + fileName).append(newline)
				.append("Content-Type: application/octet-stream").append(newline)
				.append("Content-Transfer-Encoding: base64").append(newline)
				.append("Content-ID: <"+ boundaryForCId +">").append(newline)
				.append(newline)
				.append(Base64.getEncoder().encodeToString(fileContent)).append(newline)
				.append(newline);

		// End Boundary
		bodyBuilder.append("--").append(boundary).append("--");
		return bodyBuilder.toString();
	}
}
