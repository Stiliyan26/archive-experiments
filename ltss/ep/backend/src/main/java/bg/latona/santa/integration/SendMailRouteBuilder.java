package bg.latona.santa.integration;

import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;

import javax.mail.util.ByteArrayDataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.repositories.MailAccountRepository;

public class SendMailRouteBuilder extends RouteBuilder {

	private static Logger logger = LoggerFactory.getLogger(SendMailRouteBuilder.class);
	private static Processor sendMailRetriever = null;
	
	private static MailAccountRepository Mail_Account_Repository;
	
	private Long companyCode;
	
	public SendMailRouteBuilder(Long companyCode, MailAccountRepository mailAccountRepository) {
		this.companyCode = companyCode;
		Mail_Account_Repository = mailAccountRepository;
	}

	public static Processor retrieveSendMail() {
		if(sendMailRetriever == null) {
			sendMailRetriever = new Processor() {

				public void process(Exchange exchange) throws Exception {
					String authToken = (String) exchange.getIn().getHeader("Authorization");
					RestTemplate restTemplate = new RestTemplate();
					HttpHeaders headers = new HttpHeaders();
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
					headers.add(HttpHeaders.AUTHORIZATION, authToken);
					HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

					String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/reports/builder/1?from=SendMailMessage&select=SendMailMessage.fromAccount,SendMailMessage,SendMailMessage.attachments&having=or(isNull(SendMailMessage.sent);isFalse(SendMailMessage.sent))&page=0&size=20&sort=SendMailMessage.id,asc",
							HttpMethod.GET, 
							entity, 
							String.class).getBody();
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode rowsNode = objectMapper.readTree(jsonData).path("_embedded").path("hashMaps");

					JsonNode mailNode = rowsNode.path(0).path("SendMailMessage");
					logger.trace("mailNode: "+mailNode.toString());
					if(!mailNode.isNull() && !mailNode.isMissingNode()) {
						logger.info("Found waiting to be send mail to "+mailNode.path("sendMailToRecipient").asText()+" with subject "+mailNode.path("sendMailSubject").asText());
						
						JsonNode accountNode = rowsNode.path(0).path("SendMailMessage.fromAccount");
						if(!accountNode.isNull() && !accountNode.isMissingNode()) {
							MailAccount account = Mail_Account_Repository.findFirstByIdAndDeleted(Long.parseLong(accountNode.path("id").asText()), false);

							if(account != null) {
								String mailId = mailNode.path("id").asText();

								exchange.getOut().setHeader("subject", mailNode.path("sendMailSubject").asText());
								exchange.getOut().setHeader("to", mailNode.path("sendMailToRecipient").asText());
								exchange.getOut().setHeader("sendMailMessageID", mailId);
								exchange.getOut().setHeader("from", account.getUsername());
								exchange.getOut().setHeader("username", account.getUsername());
								exchange.getOut().setHeader("password", account.getPassword());
								exchange.getOut().setHeader("host", account.getSmtpHost());
								//							exchange.getOut().setHeader("dynamicURI", "smtps://"+accountNode.path("smtpHost").asText()
								//									+"?password=RAW("+accountNode.path("password").asText()
								//									+")&username="+accountNode.path("username").asText());
								exchange.getOut().setBody(mailNode.path("sendMailContent").asText());
								//exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain; charset=UTF-8");
								exchange.getOut().setHeader("contentType", "text/plain; charset=UTF-8");

								Iterator<JsonNode> rowsIt = rowsNode.elements();
								while(rowsIt.hasNext()) {

									JsonNode rowNode = rowsIt.next();
									//only the attachments of this mail
									if (mailId.equals(rowNode.path("SendMailMessage").path("id").asText())) {
										JsonNode attNode = rowNode.path("SendMailMessage.attachments");
										if (!attNode.isNull() && !attNode.isMissingNode()) {
											logger.info("Found attachment for send mail: " + attNode.path("name").asText());
											DefaultAttachment datt = new DefaultAttachment(new ByteArrayDataSource(Base64.getDecoder().decode(attNode.path("content").asText()), "application/xml"/*"application/octet-stream"*/));
											datt.addHeader("Content-Description", attNode.path("name").asText());
											exchange.getOut().addAttachmentObject(attNode.path("name").asText(), datt);

										}
									}

								}
							} else {
								logger.error("SendMailMessage has unavailable MailAccount and can't be sent!");
							}
						} else {
							logger.error("SendMailMessage has no MailAccount and can't be sent!");
						}
					}
				}
			};
		}
		return sendMailRetriever;
	}

	@Override
	public void configure() throws Exception {
		String smtpString = DynamicRouteStarter.isTestMode ? 
			"log:test_send_mail?level=INFO&showAll=true&multiline=true" 
			: "smtps://${header.host}?password=RAW(${header.password})&username=${header.username}";
		//TODO implement mail sending cancellation for the not-send mail
		from("timer:sendMail?period=30000")
			.routeId("sendMail"+companyCode)
//			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
//			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
//			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/sendMailMessages?isSent=0") //get all not sent mail
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.process(retrieveSendMail())
			.removeHeaders("Authorization") //prevent header leak to mail
			.filter().header("to") //only when there is a recipient
			.removeHeaders("*", "host", "username", "password", "subject", "to", "from", "sendMailMessageID", "contentType") //prevent header leak to mail
			.to("log:before_send_mail?level=INFO&showAll=true&multiline=true")
			.recipientList(simple(smtpString) //dynamic URI
				//+ "&debugMode=true"
				//+ "&mail.debug=true"
			) //send the mail http://camel.apache.org/mail.html
			.removeHeaders("*", "sendMailMessageID") //prevent header leak
			.setHeader(Exchange.HTTP_METHOD, constant("PATCH"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.setBody().constant("{\"sent\":true}") //mark as sent
			.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/sendMailMessages/${header.sendMailMessageID}")) //dynamic URI
	//		.to("log:after_send_mail?level=INFO&showAll=true&multiline=true")
			;
	}

}
