package bg.latona.santa.integration;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizationRouteBuilder extends RouteBuilder {

	private static Logger logger = LoggerFactory.getLogger(ReceiveMailRouteBuilder.class);
	public static Map<Long,String> authToken = new HashMap<Long,String>();
	private static Map<Long,Processor> authTokenSetter = new HashMap<Long,Processor>();
	
	private Long companyCode;
    private final String CRM_HOST;
	
	public AuthorizationRouteBuilder(Long companyCode, String CRM_HOST) {
		this.companyCode = companyCode;
        this.CRM_HOST = CRM_HOST;
	}

	public static Processor setAuthToken(Long companyCode) {
		if(authTokenSetter.get(companyCode) == null) {
			authTokenSetter.put(companyCode,new Processor() {
				public void process(Exchange exchange) throws Exception {
					exchange.getIn().setHeader("Authorization", authToken.get(companyCode));
				}
			});
		}
		return authTokenSetter.get(companyCode);
	}
	
	@Override
	public void configure() throws Exception {
		//https://java.globinch.com/enterprise-java/security/fix-java-security-certificate-exception-no-matching-localhost-found/
		//problem was fixed by adding -ext san=dns:localhost to the certificate
		
		
		//alternatively we just generate the token???
		from("timer:authToken?period=3600000")
			.routeId("authToken"+companyCode)
			//.delay(1000*60) //debug mode delay 1 min because the camel user is not created fast enough 
			.setHeader(Exchange.HTTP_METHOD, constant("POST"))
			.setHeader(Exchange.CONTENT_TYPE, constant("multipart/form-data"))
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					StringBody username = new StringBody("camel"+companyCode, org.apache.http.entity.ContentType.MULTIPART_FORM_DATA);
					StringBody password = new StringBody("123", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA);

					MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
					multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					multipartEntityBuilder.addPart("username", username);
					multipartEntityBuilder.addPart("password", password);

					exchange.getIn().setBody(multipartEntityBuilder.build());
				}
			})
			.to("https4://"+CRM_HOST+"/api/login")
			.setHeader("authToken").jsonpath("$.Authorization",true)
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					String authTokenString = exchange.getIn().getHeader("authToken").toString();
					logger.trace("authToken: "+authTokenString);
					authToken.put(companyCode, authTokenString);
				}
			})
			//.to("log:authToken?level=INFO&showAll=true&multiline=true")
			.to("stream:out");
	}

}
