package bg.latona.santa.selfie.service.eInvoice.helpers;

import org.springframework.stereotype.Component;

@Component
public class SoapEnvelopeBuilder {

    public String buildUploadFileEnvelope(String authorizationId, String authorizationKey, String fileName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "    xmlns:ns4=\"https://efaktura.bg/soap/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns4:uploadFile>\n" +
                "            <ns4:authorizationId>" + authorizationId + "</ns4:authorizationId>\n" +
                "            <ns4:authorizationKey>" + authorizationKey + "</ns4:authorizationKey>\n" +
                "            <ns4:fileName>" + fileName + "</ns4:fileName>\n" +
                "            <fileCont href=\"cid:fileContent\"/>\n" +
                "        </ns4:uploadFile>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }

	public byte[] buildUploadFileEnvelopeBase64(String authorizationId, String authorizationKey, String fileName, String boundaryForCId) {
		String newline = "\r\n";
		String config = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				newline +
				"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
				"    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
				"    xmlns:ns4=\"https://efaktura.bg/soap/\">\n" +
				newline +
				"    <SOAP-ENV:Body>\n" +
				newline +
				"<ns4:uploadFile>\n" +
				"<ns4:authorizationId>" + authorizationId + "</ns4:authorizationId>\n" +
				"<ns4:authorizationKey>" + authorizationKey + "</ns4:authorizationKey>\n" +
				"<ns4:fileName>" + fileName + "</ns4:fileName>\n" +
				"<fileCont href=\"cid:" + boundaryForCId + "\"/></ns4:uploadFile>\n" +
				"</SOAP-ENV:Body>\n" +
				"</SOAP-ENV:Envelope>";

		return config.getBytes();
	}

    public String buildGetFileResultEnvelope(String authorizationId, String authorizationKey, String fileTicket) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "    xmlns:ns4=\"https://efaktura.bg/soap/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns4:getFileResult>\n" +
                "            <ns4:authorizationId>" + authorizationId + "</ns4:authorizationId>\n" +
                "            <ns4:authorizationKey>" + authorizationKey + "</ns4:authorizationKey>\n" +
                "            <ns4:fileTicket>" + fileTicket + "</ns4:fileTicket>\n" +
                "        </ns4:getFileResult>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }

    public String buildActivateFileEnvelope(String authorizationId, String authorizationKey, String fileTicket) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                "    xmlns:ns4=\"https://efaktura.bg/soap/\">\n" +
                "    <SOAP-ENV:Body>\n" +
                "        <ns4:activateFile>\n" +
                "            <ns4:authorizationId>" + authorizationId + "</ns4:authorizationId>\n" +
                "            <ns4:authorizationKey>" + authorizationKey + "</ns4:authorizationKey>\n" +
                "            <ns4:fileTicket>" + fileTicket + "</ns4:fileTicket>\n" +
                "        </ns4:activateFile>\n" +
                "    </SOAP-ENV:Body>\n" +
                "</SOAP-ENV:Envelope>";
    }
}
