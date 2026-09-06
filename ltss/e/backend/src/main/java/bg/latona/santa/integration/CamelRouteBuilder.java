package bg.latona.santa.integration;

import java.util.LinkedList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.support.ExpressionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.mail.MailMessage;

public class CamelRouteBuilder extends RouteBuilder {
	
//	public static Timestamp last_imported_mail_date;
//	public static String last_imported_mail_date_formatted;
	private static Logger logger = LoggerFactory.getLogger(CamelRouteBuilder.class);
	
	private Long companyCode;
	
	public CamelRouteBuilder(Long companyCode) {
		this.companyCode = companyCode;
	}
	
	public static class MailSplitRecord {
		public MailMessage mail;
		public String mailLocation;
		public List<DBFile> attachments = new LinkedList<DBFile>();
		public List<String> attachmentLocations = new LinkedList<String>();
	}
	
	//https://github.com/apache/camel/blob/master/camel-core/src/test/java/org/apache/camel/processor/SplitterPojoTest.java
	//https://svn.apache.org/repos/asf/camel/trunk/components/camel-mail/src/main/java/org/apache/camel/component/mail/SplitAttachmentsExpression.java
	private class MailSplitExpression extends ExpressionAdapter { //TODO don't get the same mail twice, check Message-ID header 
		@Override
		public Object evaluate(Exchange exchange) {
			MailSplitRecord mailSplitRecord = exchange.getIn().getBody(MailSplitRecord.class);

			try {
				ObjectMapper mapper = new ObjectMapper();
				String json = mapper.writeValueAsString(mailSplitRecord.mail);
				//return the mail
				List<org.apache.camel.Message> answer = new LinkedList<org.apache.camel.Message>();
				org.apache.camel.Message copy = exchange.getIn().copy();
				copy.getAttachments().clear();
				copy.setBody(json);
				copy.setHeader("SANTA_TYPE", "SANTA_EMAIL_MESSAGE");
				answer.add(copy);
				
				//return the attachments
				if (mailSplitRecord.attachments.size() > 0) { 
					for (DBFile dBFile : mailSplitRecord.attachments) {
						json = mapper.writeValueAsString(dBFile);
						copy = exchange.getIn().copy();
						copy.getAttachments().clear();
						copy.setBody(json);
						copy.setHeader("SANTA_TYPE", "SANTA_EMAIL_ATTACHMENT");
						answer.add(copy);
					}
				}
				
				return answer;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static class MailSplitAggregationStrategy implements AggregationStrategy {
		 
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
			// put together in old exchange by adding from new exchange
			Exchange result;
			// get "location" header, which is set by the https4 POST
			String location = newExchange.getIn().getHeader("location").toString();
			if (oldExchange == null) {
				// the first time we aggregate we only have the new exchange,
				// so we just return it
				newExchange.getIn().setBody(new MailSplitRecord());
				result = newExchange;
			} else {
				result = oldExchange;
			}
			MailSplitRecord rec = result.getIn().getBody(MailSplitRecord.class);
			if(location.contains("mailMessage")) {
				rec.mailLocation = location;
			} else {
				rec.attachmentLocations.add(location);
			}
			result.getIn().setBody(rec);
			return result;
		}
	}
	
	private class MailSplitRecordExpression extends ExpressionAdapter {
		@Override
		public Object evaluate(Exchange exchange) {
			MailSplitRecord rec = exchange.getIn().getBody(MailSplitRecord.class);
			List<org.apache.camel.Message> answer = new LinkedList<org.apache.camel.Message>();
			if(rec!=null) {
				for(String att : rec.attachmentLocations) {
					String json = "{\"mail\":\""+rec.mailLocation+"\",\"attachment\":\""+att+"\"}";
					org.apache.camel.Message copy = exchange.getIn().copy();
					copy.getAttachments().clear();
					copy.setBody(json);
					answer.add(copy);
				}
			} else {
				logger.error("MailSplitRecord is null!");
			}
			return answer;
		}
	}
	
	@Override
	public void configure() throws Exception {
		
		from("seda:email"+companyCode)
			.routeId("emailStore"+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak to mail
			//.to("log:before_split?level=INFO&showAll=true&multiline=true")
			//WARNING: split aggregation did not work with 'imaps' before it in the same route, so they are separated! 
			.split(new MailSplitExpression(),new MailSplitAggregationStrategy()) //http://camel.apache.org/composed-message-processor.html
				.choice()
					.when(header("SANTA_TYPE").isEqualTo(constant("SANTA_EMAIL_MESSAGE"))) //http://camel.apache.org/predicate.html
						.setHeader(Exchange.HTTP_METHOD, constant("POST"))
						.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
						.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
						//.to("log:mailMessages_split?level=INFO&showAll=true&multiline=true")
						.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/mailMessages")
					.when(header("SANTA_TYPE").isEqualTo(constant("SANTA_EMAIL_ATTACHMENT")))
						.setHeader(Exchange.HTTP_METHOD, constant("POST"))
						.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
						.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
						.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/dBFiles")
					.otherwise()
						.to("log:receivedMail_unknown_case?level=INFO&showAll=true&multiline=true")
						.to("stream:out")
				.end()
			.end()
			//.to("log:after_split?level=INFO&showAll=true&multiline=true")
			.split(new MailSplitRecordExpression())
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
				.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/mailAttachments")
			.end();//TODO then aggregate them - link attachments to mail
		
	}
}
