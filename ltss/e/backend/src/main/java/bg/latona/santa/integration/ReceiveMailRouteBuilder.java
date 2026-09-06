package bg.latona.santa.integration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.TypeConversionException;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.MailMessage;
import bg.latona.santa.integration.CamelRouteBuilder.MailSplitRecord;
import bg.latona.santa.reports.ReportsRepository;
import bg.latona.santa.repositories.MailMessageRepository;

public class ReceiveMailRouteBuilder extends RouteBuilder {

	private static Logger logger = LoggerFactory.getLogger(ReceiveMailRouteBuilder.class);
	private MailAccount account;
	
	MailMessageRepository mailMessageRepository;
	ReportsRepository reportsRepository;
	
	private Long companyCode;

	public ReceiveMailRouteBuilder(Long companyCode, MailMessageRepository mailMessageRepository, MailAccount account, ReportsRepository reportsRepository) {
		this.companyCode = companyCode;
		this.account = account;
		
		this.mailMessageRepository = mailMessageRepository;
		this.reportsRepository = reportsRepository;
	}
	
	private class MailTransformer implements Processor {
		//TODO Correct attachment handling is needed
		private String getTextFromMessage(javax.mail.Message message) throws IOException, MessagingException {
			String result = "";
			logger.trace("Mail content type: \n"+message.getContentType());
			if (message.isMimeType("text/plain")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("text/html")) {
				result = message.getContent().toString();
			} else if (message.isMimeType("multipart/*")) {
				MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
				result = getTextFromMimeMultipart(mimeMultipart);
			} else {
				logger.warn("Unsupported mail content type: "+message.getContentType());
				result = message.getContent().toString();
			}
			logger.trace("Mail content: \n"+result);
			return result;
		}

		private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {

			int count = mimeMultipart.getCount();
			if (count == 0)
				throw new MessagingException("Multipart with no body parts not supported.");
			boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");
			if (multipartAlt)
				// alternatives appear in an order of increasing 
				// faithfulness to the original content. Customize as req'd.
				return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
			String result = "";
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = mimeMultipart.getBodyPart(i);
				result += getTextFromBodyPart(bodyPart);
			}
			return result;
		}

		private String getTextFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
			String result = "";
			logger.trace("BodyPart content type: \n"+bodyPart.getContentType()+"\nBodyPart disposition: \n"+bodyPart.getDisposition());
			if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && bodyPart.getFileName()==null) {
				if (bodyPart.isMimeType("text/plain")) {
					result = (String) bodyPart.getContent();
				} else if (bodyPart.isMimeType("text/html")) {
					String html = (String) bodyPart.getContent();
//					result = org.jsoup.Jsoup.parse(html).text();
					result = html;
				} else if (bodyPart.getContent() instanceof MimeMultipart){
					result = getTextFromMimeMultipart((MimeMultipart)bodyPart.getContent());
				}
			}
			logger.trace("BodyPart content: \n"+result);
			return result;
		}
		
		public void process(Exchange exchange) throws Exception {
			javax.mail.internet.MimeMessage msg = exchange.getIn().getBody(javax.mail.internet.MimeMessage.class);

			try {
				StringBuilder builder = new StringBuilder();
				boolean isRecognizedContact = false;
				for(Address s : msg.getFrom()) {
					String fromAddress = ((InternetAddress) s).getAddress().toString();
					builder.append(fromAddress).append(",");
					if(!isRecognizedContact && account.getRecognizedContact()) {
						BigInteger recognizedContact = reportsRepository.isRecognizedContact(fromAddress, companyCode);
						if(recognizedContact != null) {
							logger.trace("MailMessage from recognized contact: "+fromAddress+" contains contact with ID "+recognizedContact+"; "+msg.getSubject()+"; "+msg.getSentDate());
							isRecognizedContact = true;
							break;
						}
					}
				}
				String from = builder.toString();
				from = from.substring(0, from.length() < 3000 ? from.length() : 3000);
				if(!isRecognizedContact && account.getRecognizedContact()) {
					logger.debug("MailMessage from unrecognized contact is ignored: "+from+"; "+msg.getSubject()+"; "+msg.getSentDate());
					return;
				}
				
				builder = new StringBuilder();
				Address[] recipients = msg.getAllRecipients();
				if(recipients != null) {
					for(Address s : recipients) {
						builder.append(((InternetAddress) s).getAddress().toString()).append(",");
					}
				}
				String to = builder.toString();
				to = to.substring(0, to.length() < 3000 ? to.length() : 3000);
				
				builder = new StringBuilder();
				for(Address s : msg.getReplyTo()) {
					builder.append(((InternetAddress) s).getAddress().toString()).append(",");
				}
				String replyTo = builder.toString();
				replyTo = replyTo.substring(0, replyTo.length() < 3000 ? replyTo.length() : 3000);
				
//				for(String s : exchange.getIn().getHeaders().keySet()) {
//					logger.trace("Header "+s);
//				}
				
				String content = getTextFromMessage(msg);
				
				List<MailMessage> existingMessages = mailMessageRepository.findByFromAddressAndMailSubjectAndReceivedDateAndSentDateAndMailContent(from,msg.getSubject(),msg.getReceivedDate(),msg.getSentDate(),content);
				
				if(existingMessages.size() > 0) {
					//TODO compare attachments too?
					logger.debug("MailMessage already existing: "+from+to+msg.getSubject());
					return;
				}
				
				logger.info("MailMessage receiving: "+from+to+msg.getSubject()+msg.getSentDate());

				String mailName = "Писмо \""+msg.getSubject()+"\" от "+msg.getFrom()[0].toString();
				mailName = mailName.substring(0, mailName.length() < 3000 ? mailName.length() : 3000);
				String mailSubject = msg.getSubject();
				mailSubject = mailSubject != null ? mailSubject.substring(0, mailSubject.length() < 3000 ? mailSubject.length() : 3000) : null;
				MailMessage mail = new MailMessage(null, null, null, null, false, null, //managedCompany will be put by the default rules using the Camel SecUser's company
						mailName,
						null,
						from,
						to,
						replyTo,
						msg.getReceivedDate(),
						msg.getSentDate(),
						mailSubject,
						exchange.getIn().getHeaders().toString(),
						content
					);
				
				MailSplitRecord result = new MailSplitRecord();
				result.mail = mail;
				//return the attachments
				Map<String, DataHandler> attachments = exchange.getIn().getAttachments(); 
				if (attachments.size() > 0) { 
					for (String name : attachments.keySet()) { 
						DataHandler dh = attachments.get(name);
						String filename = dh.getName(); // get the file name 
						filename = filename.substring(0, filename.length() < 3000 ? filename.length() : 3000);
						String contentType = dh.getContentType();
						contentType = contentType.substring(0, contentType.length() < 255 ? contentType.length() : 255);
						byte[] data;
						try {
							data = exchange.getContext().getTypeConverter() 
									.convertTo(byte[].class, dh.getInputStream()); // get the content and convert it to byte[] 
						} catch (TypeConversionException e) {
							e.printStackTrace();
							continue;
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
						DBFile dBFile = new DBFile(null,null,null,null,false,null,
								filename,null,data,contentType);
						result.attachments.add(dBFile);
					} 
				}
				exchange.getOut().setBody(result);
				exchange.getOut().setHeader("NotExisting", true);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void configure() throws Exception {
		logger.info("ReceiveMailRouteBuilder configure");
		//TODO make instead of daily to be started once per event (startup, new mail in contacts)
		//start daily check of all messages in the inbox folder
		from("imaps://"+this.account.getImapHost()+"?username="+this.account.getUsername()+"&password="+this.account.getPassword()
				+ ((this.account.getInboxFolder() != null && this.account.getInboxFolder().length() > 0) 
						? "&folderName="+this.account.getInboxFolder() 
						: "")
				//choose either delete=false and unseen=true or delete=true and unseen=false and copyTo=INBOX.Archive
				+ ((this.account.getDeleteMail()) 
						? "&delete=true"
						: "&delete=false")
				+ ((this.account.getUnseenMail()) 
						? "&unseen=true"
						: "&unseen=false")
				+ ((this.account.getArchiveFolder() != null && this.account.getArchiveFolder().length() > 0) 
						? "&copyTo="+this.account.getArchiveFolder()
						: "")
				+ "&consumer.delay="+1000*60*60*24 //one day
				+ "&disconnect=true" //once per day, so disconnect
				+ "&skipFailedMessage=true"
				//+ "&fetchSize=10"
				+ "&mapMailMessage=false"
//				+ "&debugMode=true"
//				+ "&mail.debug=true"
			).routeId("InboxIMAPMail_Daily_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode)
			//.to("log:"+"InboxIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+"?level=INFO&showAll=true&multiline=true")
			.process(new MailTransformer())
			.filter().header("NotExisting") //only when message doesn't exist in DB
			.removeHeaders("NotExisting") //prevent header leak to mail
			.to("seda:email"+companyCode);

		//start regular check of last messages in the inbox folder
		from("imaps://"+this.account.getImapHost()+"?username="+this.account.getUsername()+"&password="+this.account.getPassword()
				+ ((this.account.getInboxFolder() != null && this.account.getInboxFolder().length() > 0) 
						? "&folderName="+this.account.getInboxFolder() 
						: "")
				//choose either delete=false and unseen=true or delete=true and unseen=false and copyTo=INBOX.Archive
				+ ((this.account.getDeleteMail()) 
						? "&delete=true"
						: "&delete=false")
				+ ((this.account.getUnseenMail()) 
						? "&unseen=true"
						: "&unseen=false")
				+ ((this.account.getArchiveFolder() != null && this.account.getArchiveFolder().length() > 0) 
						? "&copyTo="+this.account.getArchiveFolder()
						: "")
				+ "&consumer.initialDelay="+1000*60*5 //to give the daily poller some time
				+ "&consumer.delay="+this.account.getDelay()
				//+ "&fetchSize=10"
				+ "&mapMailMessage=false"
				+ "&searchTerm.fromSentDate=now-"+(this.account.getDelay()/1000*2)+"s" //check for received messages during the last 2 intervals (should be at least 1 interval)
//				+ "&debugMode=true"
//				+ "&mail.debug=true"
			).routeId("InboxIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode)
			//.to("log:"+"InboxIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+"?level=INFO&showAll=true&multiline=true")
			.process(new MailTransformer())
			.filter().header("NotExisting") //only when message doesn't exist in DB
			.removeHeaders("NotExisting") //prevent header leak to mail
			.to("seda:email"+companyCode);

		if(this.account.getSentFolder() != null && this.account.getSentFolder().length() > 0) {
			//start daily check of all messages in the sent mail folder
			from("imaps://"+this.account.getImapHost()+"?username="+this.account.getUsername()+"&password="+this.account.getPassword()
					+ "&folderName="+this.account.getSentFolder()
					//choose either delete=false and unseen=true or delete=true and unseen=false and copyTo=INBOX.Archive
					+ ((this.account.getDeleteMail()) 
							? "&delete=true"
							: "&delete=false")
					+ ((this.account.getUnseenMail()) 
							? "&unseen=true"
							: "&unseen=false")
					+ ((this.account.getArchiveFolder() != null && this.account.getArchiveFolder().length() > 0) 
							? "&copyTo="+this.account.getArchiveFolder()
							: "")
					+ "&consumer.delay="+1000*60*60*24 //one day
					+ "&disconnect=true" //once per day, so disconnect
					+ "&skipFailedMessage=true"
					//+ "&fetchSize=10"
					+ "&mapMailMessage=false"
	//				+ "&debugMode=true"
	//				+ "&mail.debug=true"
				).routeId("SentIMAPMail_Daily_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode)
				//.to("log:"+"SentIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+"?level=INFO&showAll=true&multiline=true")
				.process(new MailTransformer())
				.filter().header("NotExisting") //only when message doesn't exist in DB
				.removeHeaders("NotExisting") //prevent header leak to mail
				.to("seda:email"+companyCode);

			//start regular check of last messages in the sent mail folder
			from("imaps://"+this.account.getImapHost()+"?username="+this.account.getUsername()+"&password="+this.account.getPassword()
					+ "&folderName="+this.account.getSentFolder()
					//choose either delete=false and unseen=true or delete=true and unseen=false and copyTo=INBOX.Archive
					+ ((this.account.getDeleteMail()) 
							? "&delete=true"
							: "&delete=false")
					+ ((this.account.getUnseenMail()) 
							? "&unseen=true"
							: "&unseen=false")
					+ ((this.account.getArchiveFolder() != null && this.account.getArchiveFolder().length() > 0) 
							? "&copyTo="+this.account.getArchiveFolder()
							: "")
					+ "&consumer.initialDelay="+1000*60*5 //to give the daily poller some time
					+ "&consumer.delay="+this.account.getDelay()
					//+ "&fetchSize=10"
					+ "&mapMailMessage=false"
					+ "&searchTerm.fromSentDate=now-"+(this.account.getDelay()/1000*2)+"s" //check for received messages during the last 2 intervals (should be at least 1 interval)
	//				+ "&debugMode=true"
	//				+ "&mail.debug=true"
				).routeId("SentIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode)
				//.to("log:"+"SentIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+"?level=INFO&showAll=true&multiline=true")
				.process(new MailTransformer())
				.filter().header("NotExisting") //only when message doesn't exist in DB
				.removeHeaders("NotExisting") //prevent header leak to mail
				.to("seda:email"+companyCode);
		}
	}

	public void stopRoutes(CamelContext camelContext) {
		try {
			camelContext.stopRoute("InboxIMAPMail_Daily_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode);
			camelContext.stopRoute("InboxIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode);
			camelContext.stopRoute("SentIMAPMail_Daily_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode);
			camelContext.stopRoute("SentIMAPMail_"+this.account.getImapHost()+"_"+this.account.getUsername()+companyCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
