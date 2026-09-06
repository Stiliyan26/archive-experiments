package bg.latona.santa.reports;

import bg.latona.santa.DroolsRuleException;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.mail.MailAccount;
import bg.latona.santa.entities.mail.MailTemplate;
import bg.latona.santa.entities.mail.SendMailMessage;
import bg.latona.santa.entities.nepal.*;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
public class NEPALProcedure {

	private static final Logger logger = LoggerFactory.getLogger(NEPALProcedure.class);

	public static final String EUROPE_SOFIA = "Europe/Sofia";
	public static final String PORTFOLIO_FOR_APRILCI = "B00218-4";
	@Autowired
	private WebApplicationContext appContext;
	private Repositories repositories = null;
	@PersistenceContext
	private EntityManager entityManager; // USING HQL

	Repositories getRepositories() {
		if (repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}

	// TODO trySave should return errors
	CommonRecord trySave(CommonRecord entity, boolean doReturnNullAtErrors, boolean doFlush) {
		logger.trace("NEPALProcedure.trySave " + entity);
		try {
			CommonRecord result;
			if (entity.getId() == null) {
				RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
				if (doFlush) {
					result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass())
							.get()).saveAndFlush(entity);
				} else {
					result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass())
							.get()).save(entity);
				}
				RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
			} else {
				RepositoryConfiguration.getBeforeSaveValidator().validate(entity, null);
				if (doFlush) {
					result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass())
							.get()).saveAndFlush(entity);
				} else {
					result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass())
							.get()).save(entity);
				}
				RepositoryConfiguration.getAfterSaveValidator().validate(entity, null);
			}
			return result;
		} catch (DroolsRuleException e) {
			if (!doReturnNullAtErrors && e.getResult().getFieldErrors().size() > 0
					&& e.getResult().getFieldErrors().get(0).getCodes()[0].equals("notUnique")) {
				return (CommonRecord) e.getResult().getFieldErrors().get(0).getRejectedValue();
			} else {
				logger.info(/*
							 * e.getStackTrace()[0].getFileName()+" "+e.getStackTrace()[0].getLineNumber()
							 * +" "+
							 */entity.getClass().getSimpleName() + ": " + e.getMessage());
				return null;
			}
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public List<Map<String, Object>> importXML(DBFile dbFile) {

		SecUser user = getSecUser();
		Schedule schedule = new Schedule();

		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
		Map<String, Object> resultRow = new HashMap<String, Object>();

		boolean isTPS = false;
		int counter = 0;
		int j = 0;
		boolean isMatch = true;

		Document document = null;

		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dbFile.getContent())) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true); // Enable secure
																									// processing

			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(new InputSource(byteArrayInputStream));

		} catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error(e.getMessage());
			resultRow.put("Parsing error - ", e);
		}

		if (getNodeValue(document, "SenderIdentification", "v").equals(getNodeValue(document, "InParty", 0, "v"))
				&& !getNodeValue(document, "MessageIdentification", "v").contains("TPS")) {

			PowerPlant powerPlant = ((PowerPlantRepository) getRepositories().getRepositoryFor(PowerPlant.class).get())
					.findFirstByProducerEicAndCompanyAndDeleted(getNodeValue(document, "SenderIdentification", "v"),
							user.getCompany(), false);

			if (powerPlant == null) {
				resultRow.put("Warnings", "DataMismatch");
				result.add(resultRow);
				isMatch = false;
			}
		}

		if (isMatch) {

			if (getNodeValue(document, "MessageIdentification", "v").contains("TPS")) {
				isTPS = true;
			}
			schedule.setMessageIdentification(getNodeValue(document, "MessageIdentification", "v"));
			schedule.setMessageVersion(new BigDecimal(getNodeValue(document, "MessageVersion", "v")));
			schedule.setMessageType(getNodeValue(document, "MessageType", "v"));
			schedule.setProcessType(getNodeValue(document, "ProcessType", "v"));
			schedule.setScheduleClassificationType(getNodeValue(document, "ScheduleClassificationType", "v"));
			schedule.setSenderIdentificationV(getNodeValue(document, "SenderIdentification", "v"));
			schedule.setSenderIdentificationCodingScheme(
					getNodeValue(document, "SenderIdentification", "codingScheme"));
			schedule.setSenderRole(getNodeValue(document, "SenderRole", "v"));
			schedule.setReceiverIdentificationV(getNodeValue(document, "ReceiverIdentification", "v"));
			schedule.setReceiverIdentificationCodingScheme(
					getNodeValue(document, "ReceiverIdentification", "codingScheme"));
			schedule.setReceiverRole(getNodeValue(document, "ReceiverRole", "v"));
			schedule.setMessageDateTime(getNodeValue(document, "MessageDateTime", "v"));
			schedule.setScheduleTimeInterval(getNodeValue(document, "ScheduleTimeInterval", "v"));
			String[] scheduleTimeIntervalParts = schedule.getScheduleTimeInterval().split("/");
			schedule.setScheduleTimeStart(
					LocalDateTime.parse(scheduleTimeIntervalParts[0], DateTimeFormatter.ISO_DATE_TIME));
			schedule.setScheduleTimeEnd(
					LocalDateTime.parse(scheduleTimeIntervalParts[1], DateTimeFormatter.ISO_DATE_TIME));
			schedule.setDeleted(false);
			schedule.setCreatedBy(user);
			schedule.setCompany(user.getCompany());

			trySave(schedule, false, true);
			schedule = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class).get())
					.findById(schedule.getId()).get();

			// ScheduleTimeSeries loop
			NodeList scheduleTimeSeriesList = document.getElementsByTagName("ScheduleTimeSeries");
			NodeList intervalList = document.getElementsByTagName("Interval");

			List<ScheduleTimeSeries> scheduleTimeSeriesListArr = new ArrayList<>();
			for (int i = 0; i < scheduleTimeSeriesList.getLength(); i++) {

				counter++;
				ScheduleTimeSeries scheduleTimeSeries = new ScheduleTimeSeries();

				trySave(scheduleTimeSeries, false, true);
				scheduleTimeSeries = ((ScheduleTimeSeriesRepository) getRepositories()
						.getRepositoryFor(ScheduleTimeSeries.class).get()).findById(scheduleTimeSeries.getId()).get();

				scheduleTimeSeries.setSendersTimeSeriesIdentification(
						getNodeValue(document, "SendersTimeSeriesIdentification", i, "v"));
				scheduleTimeSeries
						.setSendersTimeSeriesVersion(getNodeValue(document, "SendersTimeSeriesVersion", i, "v"));
				scheduleTimeSeries.setBusinessType(getNodeValue(document, "BusinessType", i, "v"));
				scheduleTimeSeries.setProduct(getNodeValue(document, "Product", i, "v"));
				scheduleTimeSeries.setObjectAggregation(getNodeValue(document, "ObjectAggregation", i, "v"));
				scheduleTimeSeries.setInAreaV(getNodeValue(document, "InArea", i, "v"));
				scheduleTimeSeries.setInAreaCodingScheme(getNodeValue(document, "InArea", i, "codingScheme"));
				if (isTPS) {
					schedule.setIsPPS(false);
					scheduleTimeSeries.setOutAreaV(getNodeValue(document, "OutArea", i, "v"));
					scheduleTimeSeries.setOutAreaCodingScheme(getNodeValue(document, "OutArea", i, "codingScheme"));
					scheduleTimeSeries.setInPartyV(getNodeValue(document, "InParty", i, "v"));
					scheduleTimeSeries.setInPartyCodingScheme(getNodeValue(document, "InParty", i, "codingScheme"));
					scheduleTimeSeries.setOutPartyV(getNodeValue(document, "OutParty", i, "v"));
					scheduleTimeSeries.setOutPartyCodingScheme(getNodeValue(document, "OutParty", i, "codingScheme"));
				} else {
					schedule.setIsPPS(true);
					scheduleTimeSeries.setMeteringPointIdentificationV(
							getNodeValue(document, "MeteringPointIdentification", i, "v"));
					scheduleTimeSeries.setMeteringPointIdentificationCodingScheme(
							getNodeValue(document, "MeteringPointIdentification", i, "codingScheme"));
					scheduleTimeSeries.setInPartyV(getNodeValue(document, "InParty", i, "v"));
					scheduleTimeSeries.setInPartyCodingScheme(getNodeValue(document, "InParty", i, "codingScheme"));
				}
				scheduleTimeSeries.setMeasurementUnit(getNodeValue(document, "MeasurementUnit", i, "v"));
				scheduleTimeSeries.setDeleted(false);
				scheduleTimeSeries.setCreatedBy(user);
				scheduleTimeSeries.setCompany(user.getCompany());

				scheduleTimeSeries.setTimeInterval(getNodeValue(document, "TimeInterval", i, "v"));
				scheduleTimeSeries.setResolution(getNodeValue(document, "Resolution", i, "v"));

				List<Interval> intervalListArr = new ArrayList<>();

				int intervalCounter = 0;
				// Interval loop
				while (j < intervalList.getLength()) {

					intervalCounter++;

					Interval interval = new Interval();

					interval.setPos(new BigDecimal(getNodeValue(document, "Pos", j, "v")));
					interval.setQty(new BigDecimal(getNodeValue(document, "Qty", j, "v")));
					interval.setDeleted(false);
					interval.setCreatedBy(user);
					interval.setCompany(user.getCompany());
					interval.setScheduleTimeSeries(scheduleTimeSeries);
					intervalListArr.add(interval);
					trySave(interval, false, true);

					j++;
				}

				resultRow.put("scheduleTimeSeries idtfc -", scheduleTimeSeries.getSendersTimeSeriesIdentification());
				scheduleTimeSeries.setIntervals(intervalListArr);
				scheduleTimeSeries.setSchedule(schedule);
				trySave(scheduleTimeSeries, true, true);
				scheduleTimeSeriesListArr.add(scheduleTimeSeries);
			}

			schedule.setIsSent(false);
			schedule.setDbFile(dbFile);
			schedule.setScheduleTimeSeries(scheduleTimeSeriesListArr);
			if (trySave(schedule, true, true) == null) {
				throw new ReportException("Error at saving Schedule - importXML");
			}
			result.add(resultRow);
		}

		return result;

	}

	private static String getNodeValue(Document document, String elementByTagName, String namedItem) {
		return document.getElementsByTagName(elementByTagName).item(0).getAttributes().getNamedItem(namedItem)
				.getNodeValue();
	}

	private static String getNodeValue(Document document, String elementByTagName, int index, String namedItem) {
		String nodeValue = null;
		try {
			nodeValue = document.getElementsByTagName(elementByTagName).item(index).getAttributes()
					.getNamedItem(namedItem).getNodeValue();
		} catch (Exception e) {
			logger.error("Existing null value of the field - getNodeValue() ");
		}

		return nodeValue;
	}

	private DBFile XMLExport(Schedule scheduleId) throws ParserConfigurationException {

		SecUser secUser = getSecUser();
		Schedule schedule = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class).get())
				.findFirstByIdAndCompanyAndDeleted(scheduleId.getId(), secUser.getCompany(), false);
		// TODO why we get error when we do it the commented way and call
		// energyDistributionCamelProcess
		List<ScheduleTimeSeries> scheduleTimeSeriesList = /* schedule.getScheduleTimeSeries() */((ScheduleTimeSeriesRepository) getRepositories()
				.getRepositoryFor(ScheduleTimeSeries.class).get())
				.findByScheduleAndCompanyAndDeleted(schedule, secUser.getCompany(), false);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
		Document document = docBuilder.newDocument();

		// Create XML stylesheet processing instruction
		String target = "xml-stylesheet";
		String data = "type=\"text/xsl\" href=\"schedule-xsl.xsl\"";
		ProcessingInstruction processingInstruction = document.createProcessingInstruction(target, data);
		document.appendChild(processingInstruction);

		// root element
		Element xmlDtdVersionDtd = document.createElement("ScheduleMessage");// "ScheduleMessage DtdVersion=3
																				// DtdRelease=3"
		xmlDtdVersionDtd.setAttribute("DtdVersion", "3");
		xmlDtdVersionDtd.setAttribute("DtdRelease", "3");
		Element messageIdentification = document.createElement("MessageIdentification");
		messageIdentification.setAttribute("v", schedule.getMessageIdentification());
		Element messageVersion = document.createElement("MessageVersion");
		String vMessageVersion = String.valueOf(schedule.getMessageVersion().intValue());
		messageVersion.setAttribute("v", vMessageVersion);
		Element messageType = document.createElement("MessageType");
		messageType.setAttribute("v", schedule.getMessageType());
		Element processType = document.createElement("ProcessType");
		processType.setAttribute("v", schedule.getProcessType());
		Element scheduleClassificationType = document.createElement("ScheduleClassificationType");
		scheduleClassificationType.setAttribute("v", schedule.getScheduleClassificationType());
		Element senderIdentification = document.createElement("SenderIdentification");
		senderIdentification.setAttribute("v", schedule.getSenderIdentificationV());
		senderIdentification.setAttribute("codingScheme", schedule.getSenderIdentificationCodingScheme());
		Element senderRole = document.createElement("SenderRole");
		senderRole.setAttribute("v", schedule.getSenderRole());
		Element receiverIdentification = document.createElement("ReceiverIdentification");
		receiverIdentification.setAttribute("v", schedule.getReceiverIdentificationV());
		receiverIdentification.setAttribute("codingScheme", schedule.getReceiverIdentificationCodingScheme());
		Element receiverRole = document.createElement("ReceiverRole");
		receiverRole.setAttribute("v", schedule.getReceiverRole());
		Element messageDateTime = document.createElement("MessageDateTime");
		messageDateTime.setAttribute("v", schedule.getMessageDateTime());
		Element scheduleTimeInterval = document.createElement("ScheduleTimeInterval");
		scheduleTimeInterval.setAttribute("v", schedule.getScheduleTimeInterval());

		document.appendChild(xmlDtdVersionDtd);

		xmlDtdVersionDtd.appendChild(messageIdentification);
		xmlDtdVersionDtd.appendChild(messageVersion);
		xmlDtdVersionDtd.appendChild(messageType);
		xmlDtdVersionDtd.appendChild(processType);
		xmlDtdVersionDtd.appendChild(scheduleClassificationType);
		xmlDtdVersionDtd.appendChild(senderIdentification);
		xmlDtdVersionDtd.appendChild(senderRole);
		xmlDtdVersionDtd.appendChild(receiverIdentification);
		xmlDtdVersionDtd.appendChild(receiverRole);
		xmlDtdVersionDtd.appendChild(messageDateTime);
		xmlDtdVersionDtd.appendChild(scheduleTimeInterval);

		for (ScheduleTimeSeries indexOfScheduleTimeSeries : scheduleTimeSeriesList) {

			Element scheduleTimeSeries = document.createElement("ScheduleTimeSeries");

			Element sendersTimeSeriesIdentification = document.createElement("SendersTimeSeriesIdentification");
			sendersTimeSeriesIdentification.setAttribute("v", indexOfScheduleTimeSeries
					.getSendersTimeSeriesIdentification()/*
															 * scheduleTimeSeriesList.get(i).
															 * getSendersTimeSeriesIdentification()
															 */);
			Element sendersTimeSeriesVersion = document.createElement("SendersTimeSeriesVersion");
			sendersTimeSeriesVersion.setAttribute("v",
					/* scheduleTimeSeriesList.get(i) */indexOfScheduleTimeSeries.getSendersTimeSeriesVersion());
			Element businessType = document.createElement("BusinessType");
			businessType.setAttribute("v", indexOfScheduleTimeSeries.getBusinessType());
			Element product = document.createElement("Product");
			product.setAttribute("v", indexOfScheduleTimeSeries.getProduct());
			Element objectAggregation = document.createElement("ObjectAggregation");
			objectAggregation.setAttribute("v", indexOfScheduleTimeSeries.getObjectAggregation());
			Element inArea = document.createElement("InArea");
			inArea.setAttribute("v", indexOfScheduleTimeSeries.getInAreaV());
			inArea.setAttribute("codingScheme", indexOfScheduleTimeSeries.getInAreaCodingScheme());
			Element outArea = document.createElement("OutArea");
			Element inParty = document.createElement("InParty");
			Element outParty = document.createElement("OutParty");
			Element meteringPointIdentification = document.createElement("MeteringPointIdentification");

			if (!schedule.getIsPPS() || schedule.getIsPPS() == null) {
				outArea.setAttribute("v", indexOfScheduleTimeSeries.getOutAreaV());
				outArea.setAttribute("codingScheme", indexOfScheduleTimeSeries.getOutAreaCodingScheme());

				inParty.setAttribute("v", indexOfScheduleTimeSeries.getInPartyV());
				inParty.setAttribute("codingScheme", indexOfScheduleTimeSeries.getInPartyCodingScheme());

				outParty.setAttribute("v", indexOfScheduleTimeSeries.getOutPartyV());
				outParty.setAttribute("codingScheme", indexOfScheduleTimeSeries.getOutPartyCodingScheme());
			} else {

				meteringPointIdentification.setAttribute("v",
						indexOfScheduleTimeSeries.getMeteringPointIdentificationV());
				meteringPointIdentification.setAttribute("codingScheme",
						indexOfScheduleTimeSeries.getMeteringPointIdentificationCodingScheme());

				inParty.setAttribute("v", indexOfScheduleTimeSeries.getInPartyV());
				inParty.setAttribute("codingScheme", indexOfScheduleTimeSeries.getInPartyCodingScheme());

			}

			Element measurementUnit = document.createElement("MeasurementUnit");
			measurementUnit.setAttribute("v", indexOfScheduleTimeSeries.getMeasurementUnit());

			Element period = document.createElement("Period");

			Element timeInterval = document.createElement("TimeInterval");
			timeInterval.setAttribute("v", indexOfScheduleTimeSeries.getTimeInterval());
			Element resolution = document.createElement("Resolution");
			resolution.setAttribute("v", indexOfScheduleTimeSeries.getResolution());

			period.appendChild(timeInterval);
			period.appendChild(resolution);

			// TODO why we get error when we do it the commented way and call
			// energyDistributionCamelProcess
			List<Interval> intervalList = /* indexOfScheduleTimeSeries.getIntervals() */((IntervalRepository) getRepositories()
					.getRepositoryFor(Interval.class).get())
					.findByScheduleTimeSeriesAndCompanyAndDeleted(indexOfScheduleTimeSeries, secUser.getCompany(), false);
			intervalList.sort(Comparator.comparing(Interval::getPos));

			for (Interval indexOfInterval : intervalList) {

				Element interval = document.createElement("Interval");

				Element pos = document.createElement("Pos");
				String posV = String.valueOf(indexOfInterval.getPos().intValue());
				pos.setAttribute("v", posV);
				Element qty = document.createElement("Qty");
				String qtyV = String.valueOf(indexOfInterval.getQty());
				qty.setAttribute("v", qtyV);

				interval.appendChild(pos);
				interval.appendChild(qty);
				period.appendChild(interval);
			}

			scheduleTimeSeries.appendChild(sendersTimeSeriesIdentification);
			scheduleTimeSeries.appendChild(sendersTimeSeriesVersion);
			scheduleTimeSeries.appendChild(businessType);
			scheduleTimeSeries.appendChild(product);
			scheduleTimeSeries.appendChild(objectAggregation);
			scheduleTimeSeries.appendChild(inArea);

			if (!schedule.getIsPPS()) { // isPPS == false
				scheduleTimeSeries.appendChild(outArea);
				scheduleTimeSeries.appendChild(inParty);
				scheduleTimeSeries.appendChild(outParty);
			} else {
				scheduleTimeSeries.appendChild(inParty);
				scheduleTimeSeries.appendChild(meteringPointIdentification);
			}
			scheduleTimeSeries.appendChild(measurementUnit);
			scheduleTimeSeries.appendChild(period);
			xmlDtdVersionDtd.appendChild(scheduleTimeSeries);
		}

		byte[] content = setXmlToByteArray(document);
		DBFile dbFile = null;

		if (schedule.getIsPPS()) {
			dbFile = setContentToDbFile(content,
					schedule.getMessageIdentification() + "_V" + schedule.getMessageVersion().intValue(), "text/xml");
		} else {
			dbFile = setContentToDbFile(content,
					schedule.getMessageIdentification() + "_V" + schedule.getMessageVersion().intValue(), "text/xml");
		}
		return dbFile;

	}

	public SecUser getSecUser() {
		String currentUser = null;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		}

		SecUserRepository secUserRepository = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class)
				.get());
		//entityManager.setFlushMode(FlushModeType.COMMIT); //avoid flushing on next-row query, which slows down this method
		SecUser user = secUserRepository.findFirstByName(currentUser);
		return user;
	}

	private DBFile setContentToDbFile(byte[] content, String fileName, String contentType) {

		DBFile dbFile = new DBFile();

		dbFile.setContent(content);
		dbFile.setContentType(contentType);
		dbFile.setName(fileName);
		trySave(dbFile, true, true);

		return dbFile;
	}

	private byte[] setXmlToByteArray(Document doc) {

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			DOMSource source = new DOMSource(doc);

			// Create a ByteArrayOutputStream to capture the output
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(outputStream);

			transformer.transform(source, result);

			String formattedXml = outputStream.toString();
			formattedXml = formattedXml.replace(" standalone=\"no\"", "");
			formattedXml = formattedXml.replace("?><?xml-stylesheet", "?>\n<?xml-stylesheet");
			formattedXml = formattedXml.replace("?><ScheduleMessage", "?>\n<ScheduleMessage");

			return formattedXml.getBytes();

		} catch (Exception e) {
			logger.error("getXmlBytes -" + e.getMessage());
			return null;
		}
	}

	public List<Map<String, Object>> sendScheduleMail(Collection<Schedule> scheduleList)
			throws ParserConfigurationException {
		SecUser secUser = getSecUser();
		DBFileRepository dbFileRepository = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get());
		ScheduleRepository scheduleRepository = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class)
				.get());

		List<DBFile> dbFileList = new ArrayList<>(scheduleList.size());
		List<Schedule> scheduleArrayList = new ArrayList<>(scheduleList.size());

		for (Schedule schedule : scheduleList) {
			//only schedules that are not sent yet
			if (schedule.getIsSent() == null || !schedule.getIsSent()) {
				//send first schedule separately
				if(schedule.getMessageVersion().compareTo(BigDecimal.ONE) == 0) {
					DBFile dbFile = dbFileRepository.findFirstById(XMLExport(schedule).getId());
					if (dbFile != null) {
						schedule.setDbFile(dbFile);
						List<DBFile> firstScheduleList = new LinkedList<DBFile>();
						firstScheduleList.add(dbFile);
						SendMailMessage sendMailMessage = sendMail(firstScheduleList);

						schedule.setSendMailMessage(sendMailMessage);
						schedule.setIsSent(true);
						if (trySave(schedule, true, true) == null) {
							throw new ReportException("Cannot be submitted same schedule");
						}
					}
				} else {
					Schedule lastSchedule = scheduleRepository
							.findFirstByMessageIdentificationAndCompanyAndDeletedOrderByMessageVersionDesc(
									schedule.getMessageIdentification(), schedule.getCompany(), false);
					//send only last schedule, no intermediate schedules
					if (schedule.getMessageVersion().compareTo(lastSchedule.getMessageVersion()) >= 0) {
						DBFile dbFile = dbFileRepository.findFirstById(XMLExport(schedule).getId());
				if (dbFile != null) {
					schedule.setDbFile(dbFile);
					scheduleArrayList.add(schedule);
					dbFileList.add(dbFile);
				}
			}
		}
			}
		}

		// TODO should be change the parameters for sending mail
		if (!dbFileList.isEmpty()) {
			SendMailMessage sendMailMessage = sendMail(dbFileList);

			for (Schedule scheduleId : scheduleArrayList) {
				Schedule schedule = scheduleRepository.findFirstByIdAndCompanyAndDeleted(scheduleId.getId(),
						secUser.getCompany(), false);

				if (schedule.getIsSent() == null || !schedule.getIsSent()) {

					schedule.setSendMailMessage(sendMailMessage);
					schedule.setIsSent(true);
					if (trySave(schedule, true, true) == null) {
						throw new ReportException("Cannot be submitted same schedule");
					}
				}
			}
		}

		return null;
	}

	public SendMailMessage sendMail(List<DBFile> attachments) {
		SecUser secUser = getSecUser();
		
		MailAccountRepository mailAccountRepository = ((MailAccountRepository) getRepositories()
				.getRepositoryFor(MailAccount.class).get());
		MailAccount mailAccount = mailAccountRepository.findFirstByDefaultAccountAndCompanyAndDeleted(true,
				secUser.getCompany(), false);
		MailTemplate mailTemplate = ((MailTemplateRepository) getRepositories().getRepositoryFor(MailTemplate.class)
				.get()).findFirstByTemplateCodeAndCompanyAndDeleted(MailTemplate.MAIL_TEMPLATE_FOR_ESO,
						secUser.getCompany(), false);

		SendMailMessage sendMailMessage = new SendMailMessage();
		sendMailMessage.setFromAccount(mailAccount);
		sendMailMessage.setName(mailTemplate.getName());
		sendMailMessage.setSendMailSubject(mailTemplate.getTemplateSubject());
		sendMailMessage.setSendMailToRecipient(mailTemplate.getTemplateToRecipient());
		sendMailMessage.setSendMailContent(mailTemplate.getTemplateContent());
		sendMailMessage.setAttachments(attachments);
		sendMailMessage.setSent(false);
		if (trySave(sendMailMessage, true, true) == null) {
			throw new ReportException("Error at saving SendMailMessage - sendMail");
		}
		return sendMailMessage;
	}

	public String getBearerTokenForIbexIntradayApi() throws IOException {
		// TODO put these as confidential parameters
		String clientId = "client_intraday_api";
		String clientSecret = "1xB9Ik1xsEu2nbwVa1BR";
		String username = "API_office@finvest.bg";
		String password = "t7g!uqzvCCb8eML";
		String accessToken = "";

		AsyncHttpClient client = new DefaultAsyncHttpClient();

		String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

		try {
			Response response = client.prepare("POST", "https://sso.ibex.bg/connect/token")
					.setHeader("accept", "application/json")
					.setHeader("content-type", "application/x-www-form-urlencoded")
					.setHeader("Authorization", "Basic " + credentials)
					.setBody("grant_type=password&scope=intraday_api&username=" + username + "&password=" + password)
					.execute()
					.toCompletableFuture()
					.join();

			String responseBody = response.getResponseBody();
			logger.debug("responseBody - " + responseBody);
			accessToken = parseAccessToken(responseBody);
			logger.debug("accessToken - " + accessToken);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		client.close();

		return accessToken;

	}

	private String parseAccessToken(String responseBody) {

		if (responseBody != null) {
			JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
			return jsonObject.get("access_token").getAsString();
		}
		return null;
	}

	public List<Map<String, Object>> getIbexEnergyDealInfo() throws IOException, NullPointerException {

		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();
		List<Long> dealList = new ArrayList<>();
		Calendar createTime = Calendar.getInstance();

		LocalDateTime startZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).minusDays(1)
				.toLocalDateTime();
		LocalDateTime endZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).toLocalDateTime();

		// LocalDateTime midnightDateTimeStart =
		// startZuluTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
		// LocalDateTime midnightDateTimeEnd =
		// endZuluTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String formattedStartZuluTime = startZuluTime.format(formatter);
		String formattedEndZuluTime = endZuluTime.format(formatter);

		List<IbexEnergyDeal> ibexEnergyDealList = new ArrayList<>();
		Type listType = new TypeToken<List<IbexEnergyDeal>>() {
		}.getType();

		// String url =
		// "https://intraday2-api.test.nordpoolgroup.com/api/trades/private?api-version=2.0&endZuluTime=2024-01-06T00:00:00.000Z&startZuluTime=2024-01-05T00:00:00.000Z";
		// String url =
		// "https://intraday2-api.nordpoolgroup.com/api/trades/private?api-version=2.0&endZuluTime=2024-08-01T07:55:55.539Z&startZuluTime=2024-08-01T07:30:55.539Z";
		String url = "https://intraday2-api.nordpoolgroup.com/api/trades/private?api-version=2.0&endZuluTime="
				+ formattedEndZuluTime + "&startZuluTime=" + formattedStartZuluTime;

		String bearerToken = getBearerTokenForIbexIntradayApi();

		if (!bearerToken.equals("")) {
			CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("accept", "application/json");
			httpGet.addHeader("Authorization", "Bearer " + bearerToken);
			CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);

			if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {

				String responseJson = EntityUtils.toString(closeableHttpResponse.getEntity());

				try {

					Gson gson = new GsonBuilder().create();
					ibexEnergyDealList = gson.fromJson(responseJson, listType);

					for (int i = 0; i < ibexEnergyDealList.size(); i++) {

						ibexEnergyDealList.get(i).setIsDistributedToSchedules(false);

						if (trySave(ibexEnergyDealList.get(i), true, false) == null) {
							logger.error("Cannot save existing ibexEnergyDeal - getIbexEnergyDealInfo");
						} else {
							dealList.add(ibexEnergyDealList.get(i).getId());

							for (int k = 0; k < ibexEnergyDealList.get(i).getLegs().size(); k++) {

								IbexEnergyDealLeg ibexEnergyDealLeg = ibexEnergyDealList.get(i).getLegs().get(k);
								ibexEnergyDealLeg.setIbexEnergyDeal(ibexEnergyDealList.get(i));
								ibexEnergyDealLeg.setDeliveryStart(convertToCET(ibexEnergyDealLeg.getDeliveryStart()));
								ibexEnergyDealLeg.setDeliveryEnd(convertToCET(ibexEnergyDealLeg.getDeliveryEnd()));
								BigDecimal UnitPrice = ibexEnergyDealLeg.getUnitPrice().movePointLeft(2);
								ibexEnergyDealLeg.setUnitPrice(UnitPrice.setScale(2, RoundingMode.HALF_UP));

								trySave(ibexEnergyDealLeg, true, false);
							}
						}
					}

					objectMap.put("result", dealList);

				} catch (Exception e) {
					logger.error("Exception from getIbexEnergyDealInfo -" + e.getMessage());
				}

			} else if (closeableHttpResponse.getStatusLine().getStatusCode() == 503) {
				objectMap.put("Warnings", "503");
			} else {
				objectMap.put("Warnings", "NoInternetConnection");
			}

			closeableHttpClient.close();

		} else {
			objectMap.put("Warnings", "NoInternetConnection");
		}

		objectMap.put("result", dealList);
		result.add(objectMap);
		return result;

	}

	private String convertToCET(String input) {

		Calendar createTime = Calendar.getInstance();
		int hour = createTime.toInstant().atZone(ZoneId.of("Europe/Berlin")).getHour()
				- createTime.toInstant().atZone(ZoneId.of("UTC")).getHour();
		LocalDateTime localDateTime = LocalDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME).plusHours(hour);
		String output = localDateTime.atZone(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

		// The result of cetDateTimeString will be in this format
		// 2024-06-27T14:00:00+02:00[Europe/Berlin]
		// ZonedDateTime utcDateTime = ZonedDateTime.parse(input,
		// DateTimeFormatter.ISO_ZONED_DATE_TIME);
		// ZonedDateTime cestDateTime =
		// utcDateTime.withZoneSameInstant(ZoneId.of("Europe/Berlin"));
		// DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
		// String cetDateTimeString = cestDateTime.format(formatter);

		return output;
	}

	private void createIntervalsWithoutQuantity(List<Interval> intervalList, ScheduleTimeSeries tpsScheduleTimeSeries) {

		for (Interval interval : intervalList) {
			Interval newInterval = new Interval();
			newInterval.setPos(interval.getPos());
			newInterval.setQty(BigDecimal.valueOf(0));
			newInterval.setScheduleTimeSeries(tpsScheduleTimeSeries);
			if (trySave(newInterval, true, false) == null) {
				throw new ReportException("Error at saving Interval");
			}
		}
	}

	public List<IbexEnergyDeal> getIbexEnergyDeal(Long minutes) throws IOException, NullPointerException {
		SecUser secUser = getSecUser();
		List<IbexEnergyDeal> dealList = new ArrayList<>();
		Calendar createTime = Calendar.getInstance();
		LocalDateTime startZuluTime = null;
		LocalDateTime endZuluTime = null;

		if (minutes != null) {

			// Finding time difference between UTC and EEST
			int hour = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).getHour()
					- createTime.toInstant().atZone(ZoneId.of("UTC")).getHour();

			endZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).minusHours(hour)
					.toLocalDateTime();
			startZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).minusHours(hour)
					.minusMinutes(minutes).toLocalDateTime();
		} else {

			startZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).minusDays(1)
					.toLocalDateTime();
			endZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).toLocalDateTime();
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		String formattedEndZuluTime = endZuluTime.format(formatter);
		String formattedStartZuluTime = startZuluTime.format(formatter);

		List<IbexEnergyDeal> ibexEnergyDealList = new ArrayList<>();
		Type listType = new TypeToken<List<IbexEnergyDeal>>() {
		}.getType();

		String url = "https://intraday2-api.nordpoolgroup.com/api/trades/private?api-version=2.0&endZuluTime="
				+ formattedEndZuluTime + "&startZuluTime=" + formattedStartZuluTime;

		String bearerToken = getBearerTokenForIbexIntradayApi();

		if (!bearerToken.equals("")) {
			CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("accept", "application/json");
			httpGet.addHeader("Authorization", "Bearer " + bearerToken);
			CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);

			if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {

				String responseJson = EntityUtils.toString(closeableHttpResponse.getEntity());

				if (responseJson.contains("deliveryStart") && responseJson.contains("deliveryEnd")) {

					logger.debug("responseJson - " + responseJson);

					try {

						Gson gson = new GsonBuilder().create();
						ibexEnergyDealList = gson.fromJson(responseJson, listType);
						logger.debug("Importing ibexEnergyDeals count: " + ibexEnergyDealList.size());

						LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
						Date last24HoursDate = Date.from(last24Hours.atZone(ZoneId.systemDefault()).toInstant());
						List<IbexEnergyDeal> ibexEnergyDeals = ((IbexEnergyDealRepository) getRepositories()
								.getRepositoryFor(IbexEnergyDeal.class).get())
								.findDealsFromLast24Hours(last24HoursDate, secUser.getCompany(), false);
						logger.debug("Existing ibexEnergyDeals count: " + ibexEnergyDeals.size());

						Set<String> tradeId = new HashSet<>();
						ibexEnergyDeals.forEach(e -> tradeId.add(e.getTradeId()));

						for (int i = 0; i < ibexEnergyDealList.size(); i++) {

							IbexEnergyDeal ibexEnergyDeal = ibexEnergyDealList.get(i);
							ibexEnergyDeal.setIsDistributedToSchedules(false);

							if (tradeId.contains(ibexEnergyDeal.getTradeId())) {
								logger.trace("Cannot save existing ibexEnergyDeal - getIbexEnergyDeal tradeId: "
										+ ibexEnergyDeal.getTradeId());
							} else {

								if (trySave(ibexEnergyDeal, true, false) == null) {
									logger.error("Cannot save existing ibexEnergyDeal - getIbexEnergyDeal");
								} else {

									dealList.add(ibexEnergyDeal);

									for (int k = 0; k < ibexEnergyDeal.getLegs().size(); k++) {

										IbexEnergyDealLeg ibexEnergyDealLeg = ibexEnergyDeal.getLegs().get(k);
										ibexEnergyDealLeg.setIbexEnergyDeal(ibexEnergyDeal);
										ibexEnergyDealLeg
												.setDeliveryStart(convertToCET(ibexEnergyDealLeg.getDeliveryStart()));
										ibexEnergyDealLeg
												.setDeliveryEnd(convertToCET(ibexEnergyDealLeg.getDeliveryEnd()));
										BigDecimal UnitPrice = ibexEnergyDealLeg.getUnitPrice().movePointLeft(2);
										ibexEnergyDealLeg.setUnitPrice(UnitPrice.setScale(2, RoundingMode.HALF_UP));

										trySave(ibexEnergyDealLeg, true, false);
									}
								}
							}
						}
						logger.info("Saved ibexEnergyDeals count: " + dealList.size());
					} catch (Exception e) {
						logger.error("getIbexEnergyDeal -" + e);
					}
				}
			} else {
				logger.error(String.valueOf(closeableHttpResponse.getStatusLine()));
			}

			closeableHttpClient.close();
		}

		return dealList;
	}

	public synchronized List<Map<String, Object>> energyDistributionCamelProcess(List<IbexEnergyDeal> ibexEnergyDealList)
			throws IOException, ParserConfigurationException {

		logger.info("Starting energyDistributionCamelProcess");
		List<Map<String, Object>> result = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();
		Set<Schedule> scheduleListToSend = new HashSet<>();
		Set<Schedule> scheduleList = new HashSet<>();
	//	List<IbexEnergyDeal> ibexEnergyDealList = getIbexEnergyDeal(30l);
		SecUser secUser = getSecUser();
		ScheduleRepository scheduleRepository = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class)
				.get());
		ScheduleTimeSeriesRepository scheduleTimeSeriesRepository = ((ScheduleTimeSeriesRepository) getRepositories()
				.getRepositoryFor(ScheduleTimeSeries.class).get());
		IntervalRepository intervalRepository = ((IntervalRepository) getRepositories().getRepositoryFor(Interval.class)
				.get());
		int ibexEnergyDealListSize = 0;

		if (ibexEnergyDealList == null || ibexEnergyDealList.isEmpty()) { // TODO Why don't we add them always?
			ibexEnergyDealList = getUndistributedDeals();
		}

		if (ibexEnergyDealList != null && !ibexEnergyDealList.isEmpty()) {
			logger.info("Distributing ibexEnergyDeals count before filter: " + ibexEnergyDealList.size());
			ibexEnergyDealListSize = ibexEnergyDealList.size();
			ibexEnergyDealList = ibexEnergyDealList.stream()
					.filter(ibexEnergyDeal -> ibexEnergyDeal.getLegs() != null
							&& ibexEnergyDeal.getLegs().get(0).getPortfolioId().equals(PORTFOLIO_FOR_APRILCI)
							&& !ibexEnergyDeal.getIsDistributedToSchedules())
					.collect(Collectors.toList());
			logger.info("Distributing ibexEnergyDeals count after filter: " + ibexEnergyDealList.size());
		}

		if (ibexEnergyDealList != null && !ibexEnergyDealList.isEmpty()) {

			logger.debug("ibexEnergyDealList size - after sorting -  " + ibexEnergyDealList.size());

			List<PowerPlant> powerPlantList = ((PowerPlantRepository) getRepositories()
					.getRepositoryFor(PowerPlant.class).get()).findByCompanyAndDeleted(secUser.getCompany(), false);
			if (powerPlantList.size() > 1) {
				powerPlantList.sort((powerPlant, powerPlantSec) -> powerPlantSec.getInstalledPowerMw()
						.compareTo(powerPlant.getInstalledPowerMw()));
			}

			for (int energyDeal = 0; energyDeal < ibexEnergyDealList.size(); energyDeal++) {

				logger.debug("trade id - " + ibexEnergyDealList.get(energyDeal).getTradeId());
				logger.debug("id - " + ibexEnergyDealList.get(energyDeal).getId());

				if (ibexEnergyDealList.get(energyDeal) != null
						&& !ibexEnergyDealList.get(energyDeal).getLegs().isEmpty() /*
																					 * && ibexEnergyDealList.get(
																					 * energyDeal)
																					 * .getLegs().get(0).getPortfolioId(
																					 * ).equals(PORTFOLIO_FOR_APRILCI)
																					 */) {

					logger.debug("energyDistributionCamelProcess Distribute deal");
					List<IbexEnergyDealLeg> ibexEnergyDealLegList = ibexEnergyDealList.get(energyDeal).getLegs();
					BigDecimal purchasedQuantity = ibexEnergyDealLegList.get(0).getQuantity()
							.divide(BigDecimal.valueOf(1000));
					String ibexEnergyDealLegDeliveryStart = extractLocalDate(
							ibexEnergyDealLegList.get(0).getDeliveryStart());
					logger.debug("ibexEnergyDealLegDeliveryStart - " + ibexEnergyDealLegDeliveryStart);

					if (!powerPlantList.isEmpty()) {

						// TODO for now it is made to work with only one PowerPlant
						for (int i = 0; i < 1/* powerPlantList.size() */; i++) {
							logger.debug("energyDistributionCamelProcess Distribute power plant");
							Schedule schedule = scheduleRepository
									.findFirstByIsPPSAndMessageIdentificationStartingWithAndSenderIdentificationVAndCompanyAndDeletedOrderByMessageVersionDesc(
											true, ibexEnergyDealLegDeliveryStart,
											powerPlantList.get(i).getProducerEic(), secUser.getCompany(), false);

							if (schedule != null) {
								logger.debug("energyDistributionCamelProcess Distribute schedule");
								Schedule scheduleTps = scheduleRepository
										.findFirstByIsPPSAndMessageIdentificationStartingWithAndSenderIdentificationVAndCompanyAndDeletedOrderByMessageVersionDesc(
												false, ibexEnergyDealLegDeliveryStart,
												schedule.getSenderIdentificationV(), secUser.getCompany(), false);

								String coordinatorSenderIdentification = "32X001100101785V";
								Schedule coordinatorScheduleTps = scheduleRepository
										.findFirstByIsPPSAndMessageIdentificationStartingWithAndSenderIdentificationVAndCompanyAndDeletedOrderByMessageVersionDesc(
												false, ibexEnergyDealLegDeliveryStart,
												coordinatorSenderIdentification, secUser.getCompany(), false);

								List<ScheduleTimeSeries> scheduleTimeSeriesList = scheduleTimeSeriesRepository
										.findByScheduleAndCompanyAndDeleted(schedule, secUser.getCompany(), false);

								for (int j = 0; j < scheduleTimeSeriesList.size(); j++) {
									logger.debug("energyDistributionCamelProcess Distribute schedule time series");
									ScheduleTimeSeries scheduleTimeSeries = scheduleTimeSeriesList.get(j);

									// TODO Why use find? - The reason is that when we use scheduleTimeSeries.getIntervals() it throws a NullPointerException.
									List<Interval> intervalList = intervalRepository
											.findByScheduleTimeSeriesAndCompanyAndDeleted(scheduleTimeSeries, secUser.getCompany(), false); 

									intervalList.sort(Comparator.comparing(Interval::getPos));

									// find powerPlant
									if (scheduleTimeSeries.getInPartyV()
											.equals(powerPlantList.get(i).getProducerEic())) {

										int indexOfIntervalPosStart = getIntervalIndex(
												ibexEnergyDealLegList.get(0).getDeliveryStart());
										int indexOfIntervalPosEnd = getIntervalIndex(
												ibexEnergyDealLegList.get(0).getDeliveryEnd());
										int counterOfDistributedIntervals = 0;
										boolean generateNewTps = false;
										int counterOfIntervals = 0;
										int counterOfNewIntervals = 0;
										ScheduleTimeSeries newScheduleTimeSeries = new ScheduleTimeSeries();
										ScheduleTimeSeries newScheduleTimeSeriesTpsFirst = new ScheduleTimeSeries();
										ScheduleTimeSeries newScheduleTimeSeriesCoordinatorTpsFirst = new ScheduleTimeSeries();
										ScheduleTimeSeries newScheduleTimeSeriesTpsSecond = new ScheduleTimeSeries();
										ScheduleTimeSeries newScheduleTimeSeriesCoordinatorTpsSecond = new ScheduleTimeSeries();

										for (int intervalPosition = indexOfIntervalPosStart; intervalPosition < indexOfIntervalPosEnd; intervalPosition++) {


											logger.debug("energyDistributionCamelProcess Distribute interval");
											BigDecimal qtyOfInterval = intervalList.get(intervalPosition).getQty();
											List<QuarterOfHour> quarterOfHourList = new ArrayList<>();

											BigDecimal installedPowerMw = powerPlantList.get(i).getInstalledPowerMw();

											if (powerPlantList.get(i).getPowerPlantProfile() != null) {
												quarterOfHourList = powerPlantList.get(i).getPowerPlantProfile()
														.getQuarterOfHours();
											} else {
												objectMap.put("Warnings", "MissingPowerPlantProfile");
												logger.error("MissingPowerPlantProfile");
												break;
											}

											if (installedPowerMw.compareTo(qtyOfInterval) == 0
													&& ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
												objectMap.put("Warnings", "MaximumPowerReached");
												break;
											}

											if (!quarterOfHourList.isEmpty()) {
												quarterOfHourList
														.sort(Comparator.comparing(QuarterOfHour::getQuarterOfHour));
											}

											if (indexOfIntervalPosStart + 1 == indexOfIntervalPosEnd && (quarterOfHourList.get(intervalPosition).getValue() == null || quarterOfHourList.get(intervalPosition).getValue().compareTo(BigDecimal.ZERO) == 0)) {
												objectMap.put("Warnings", "MissingValueForInterval");
												break;
											}

											if (quarterOfHourList.get(intervalPosition).getValue() == null || quarterOfHourList.get(intervalPosition).getValue().compareTo(BigDecimal.ZERO) == 0) {
												objectMap.put("Warnings", "MissingValuesForIntervals");
												break;
											}

											BigDecimal quarterOfHoursValue = quarterOfHourList.get(intervalPosition)
													.getValue().multiply(powerPlantList.get(i).getInstalledPowerMw())
													.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

											// powerPlants InstalledPowerMw is greater than qty
											if (quarterOfHoursValue.compareTo(qtyOfInterval) > 0
													|| ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {

												counterOfDistributedIntervals++;

												BigDecimal powerPlantConsumingCapacity = quarterOfHoursValue
														.subtract(qtyOfInterval);

												// purchasedQuantity is less than powerPlantConsumingCapacity
												if (purchasedQuantity.compareTo(powerPlantConsumingCapacity) < 0
														|| ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {

													if (counterOfNewIntervals == 0) {

														logger.debug(
																"energyDistributionCamelProcess Copy schedule time series");
														trySave(newScheduleTimeSeries, true, false);

														for (Interval interval : intervalList) {

															Interval newInterval = new Interval();
															newInterval.setPos(interval.getPos());
															newInterval.setQty(interval.getQty());
															newInterval.setScheduleTimeSeries(newScheduleTimeSeries);
															if (trySave(newInterval, true, false) == null) {
																logger.error("Error at saving Interval");
															}
														}
														counterOfNewIntervals++;
													}

													List<Interval> newIntervalList = intervalRepository
															.findByScheduleTimeSeriesAndCompanyAndDeleted(newScheduleTimeSeries, secUser.getCompany(), false);
													newIntervalList.sort(Comparator.comparing(Interval::getPos));
													if (ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
														newIntervalList.get(intervalPosition).setQty(newIntervalList
																.get(intervalPosition).getQty().add(purchasedQuantity));
													} else {
														newIntervalList.get(intervalPosition)
																.setQty(newIntervalList.get(intervalPosition).getQty()
																		.subtract(purchasedQuantity));

														if (newIntervalList.get(intervalPosition).getQty()
																.compareTo(BigDecimal.ZERO) < 0) {
															newIntervalList.get(intervalPosition)
																	.setQty(BigDecimal.ZERO);
														}
													}

												//	newIntervalList.get(intervalPosition).setQty(newIntervalList.get(intervalPosition).getQty().add(purchasedQuantity));
													logger.debug("energyDistributionCamelProcess Save interval");
													trySave(newIntervalList.get(intervalPosition), true, true);

													if (scheduleTps == null) {

														generateNewTps = true;

														// Generate TPS
														scheduleTps = new Schedule();
														scheduleTps.setMessageIdentification(
																ibexEnergyDealLegDeliveryStart
																		+ "_TPS_" + schedule.getSenderIdentificationV()
																		+ "_IDM");
														scheduleTps.setMessageVersion(BigDecimal.valueOf(1));
														scheduleTps.setMessageType("A01");
														scheduleTps.setProcessType("A19");
														scheduleTps.setScheduleClassificationType("A01");
														scheduleTps.setSenderIdentificationV(
																schedule.getSenderIdentificationV());
														scheduleTps.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														scheduleTps.setSenderRole("A01");
														scheduleTps.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														scheduleTps.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														scheduleTps.setReceiverRole(schedule.getReceiverRole());
														scheduleTps.setMessageDateTime(schedule.getMessageDateTime());
														scheduleTps.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														String[] scheduleTimeIntervalParts = scheduleTps
																.getScheduleTimeInterval().split("/");
														scheduleTps.setScheduleTimeStart(
																LocalDateTime.parse(scheduleTimeIntervalParts[0],
																		DateTimeFormatter.ISO_DATE_TIME));
														scheduleTps.setScheduleTimeEnd(
																LocalDateTime.parse(scheduleTimeIntervalParts[1],
																		DateTimeFormatter.ISO_DATE_TIME));
														scheduleTps.setIsPPS(false);
														scheduleTps.setIsSent(false);
														logger.debug("energyDistributionCamelProcess Save scheduleTps");
														trySave(scheduleTps, true, true);

														ScheduleTimeSeries tpsScheduleTimeSeries = findOrCreateScheduleTimeSeriesForTPS(
																scheduleTps, ibexEnergyDealLegList, scheduleTimeSeries,
																"TS 001", secUser);
														createIntervalsWithoutQuantity(intervalList,
																tpsScheduleTimeSeries);

														List<Interval> tpsIntervalList = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		tpsScheduleTimeSeries, secUser.getCompany(), false);
														tpsIntervalList.sort(Comparator.comparing(Interval::getPos));

														tpsIntervalList.get(intervalPosition).setQty(tpsIntervalList
																.get(intervalPosition).getQty().add(purchasedQuantity));
														trySave(tpsIntervalList.get(intervalPosition), true, true);

														scheduleList.add(scheduleTps);

													} else {

														List<ScheduleTimeSeries> scheduleTimeSeriesTps = scheduleTimeSeriesRepository
																.findByScheduleAndCompanyAndDeleted(scheduleTps,
																		secUser.getCompany(), false);

														List<Interval> intervalListTps = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		scheduleTimeSeriesTps.get(0), secUser.getCompany(), false);
														intervalListTps.sort(Comparator.comparing(Interval::getPos));

														if (generateNewTps) {
															intervalListTps.get(intervalPosition)
																	.setQty(intervalListTps.get(intervalPosition)
																			.getQty().add(purchasedQuantity));
															logger.debug(
																	"energyDistributionCamelProcess Save interval");

															trySave(intervalListTps.get(intervalPosition), true, true);

															if (!scheduleList.contains(scheduleTps)) {
																scheduleList.add(scheduleTps);
															}
														}

														// Creating ScheduleTimeSeries
														if (counterOfIntervals == 0 && !generateNewTps) {

															if (scheduleTimeSeriesTps.size() == 1) {

																newScheduleTimeSeriesTpsFirst = findOrCreateScheduleTimeSeriesForTPS(
																		scheduleTps, ibexEnergyDealLegList,
																		scheduleTimeSeriesTps.get(0), "TS 001",
																		secUser);

																if (Objects.equals(
																		newScheduleTimeSeriesTpsFirst.getId(),
																		scheduleTimeSeriesTps.get(0).getId())) {

																	newScheduleTimeSeriesTpsFirst = createScheduleTimeSeries(
																			scheduleTimeSeriesTps.get(0), "TS 001");
																	createIntervals(scheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesTpsFirst);

																	newScheduleTimeSeriesTpsSecond = findOrCreateScheduleTimeSeriesForTPS(
																			scheduleTps, ibexEnergyDealLegList,
																			scheduleTimeSeriesTps.get(0), "TS 002",
																			secUser);

																} else if (!newScheduleTimeSeriesTpsFirst.getId()
																		.equals(scheduleTimeSeriesTps.get(0).getId())) {

																	newScheduleTimeSeriesTpsFirst
																			.setSendersTimeSeriesIdentification(
																					"TS 002");
																	createIntervalsWithoutQuantity(intervalListTps,
																			newScheduleTimeSeriesTpsFirst);

																	newScheduleTimeSeriesTpsSecond = createScheduleTimeSeries(
																			scheduleTimeSeriesTps.get(0), "TS 001");
																	createIntervals(scheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesTpsSecond);
																}

															} else {
																newScheduleTimeSeriesTpsFirst = createScheduleTimeSeries(
																		scheduleTimeSeriesTps.get(0), "TS 001");
																createIntervals(scheduleTimeSeriesTps.get(0),
																		newScheduleTimeSeriesTpsFirst);
																newScheduleTimeSeriesTpsSecond = createScheduleTimeSeries(
																		scheduleTimeSeriesTps.get(1), "TS 002");
																createIntervals(scheduleTimeSeriesTps.get(1),
																		newScheduleTimeSeriesTpsSecond);
															}
														}

														// Distributing quantity to interval
														if (!generateNewTps) {
															if (scheduleTimeSeriesTps.size() == 1) {

																List<Interval> newIntervalListTps = intervalRepository
																		.findByScheduleTimeSeriesAndCompanyAndDeleted(
																				newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																newIntervalListTps
																		.sort(Comparator.comparing(Interval::getPos));
																newIntervalListTps.get(intervalPosition)
																		.setQty(newIntervalListTps.get(intervalPosition)
																				.getQty().add(purchasedQuantity));

															} else {

																if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("SELL")) {

																	if (newScheduleTimeSeriesTpsFirst.getInPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	} else if (newScheduleTimeSeriesTpsSecond
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	}

																} else if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("BUY")) {

																	if (newScheduleTimeSeriesTpsFirst.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	} else if (newScheduleTimeSeriesTpsSecond
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	}
																}
															}
														}

														// Create new Schedule TPS with greater messageVersion
														if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
																- indexOfIntervalPosStart) && !generateNewTps) {

															Schedule newSchedule = new Schedule();
															newSchedule.setMessageIdentification(
																	scheduleTps.getMessageIdentification());
															newSchedule.setMessageVersion(
																	BigDecimal.valueOf(scheduleTps.getMessageVersion()
																			.add(BigDecimal.ONE).intValue()));
															newSchedule.setMessageType(scheduleTps.getMessageType());
															newSchedule.setProcessType(scheduleTps.getProcessType());
															newSchedule.setScheduleClassificationType(
																	scheduleTps.getScheduleClassificationType());
															newSchedule.setSenderIdentificationV(
																	scheduleTps.getSenderIdentificationV());
															newSchedule.setSenderIdentificationCodingScheme(
																	scheduleTps.getSenderIdentificationCodingScheme());
															newSchedule.setSenderRole(scheduleTps.getSenderRole());
															newSchedule.setReceiverIdentificationV(
																	scheduleTps.getReceiverIdentificationV());
															newSchedule
																	.setReceiverIdentificationCodingScheme(scheduleTps
																			.getReceiverIdentificationCodingScheme());
															newSchedule.setReceiverRole(scheduleTps.getReceiverRole());
															newSchedule.setMessageDateTime(
																	scheduleTps.getMessageDateTime());
															newSchedule.setScheduleTimeInterval(
																	scheduleTps.getScheduleTimeInterval());
															newSchedule.setScheduleTimeStart(
																	scheduleTps.getScheduleTimeStart());
															newSchedule.setScheduleTimeEnd(
																	scheduleTps.getScheduleTimeEnd());
															newSchedule.setIsPPS(false);
															newSchedule.setIsSent(false);
															logger.debug(
																	"energyDistributionCamelProcess Save newSchedule");
															trySave(newSchedule, true, true);

															newScheduleTimeSeriesTpsFirst.setSendersTimeSeriesVersion(
																	String.valueOf(newSchedule.getMessageVersion()));
															newScheduleTimeSeriesTpsFirst.setSchedule(newSchedule);
															trySave(newScheduleTimeSeriesTpsFirst, true, false);

															if (newScheduleTimeSeriesTpsSecond != null && !Objects
																	.equals(newScheduleTimeSeriesTpsSecond.getId(),
																			scheduleTimeSeriesTps.get(0).getId())) {
																newScheduleTimeSeriesTpsSecond
																		.setSendersTimeSeriesVersion(String.valueOf(
																				newSchedule.getMessageVersion()));
																newScheduleTimeSeriesTpsSecond.setSchedule(newSchedule);
																trySave(newScheduleTimeSeriesTpsSecond, true, true);
															}

															if (!scheduleList.contains(newSchedule)) {
																scheduleList.add(newSchedule);
															}
															scheduleTps = newSchedule;
														}
													}

													if (coordinatorScheduleTps == null) {

														generateNewTps = true;

														// Generate TPS for Coordinator
														coordinatorScheduleTps = new Schedule();
														coordinatorScheduleTps.setMessageIdentification(
																ibexEnergyDealLegDeliveryStart + "_TPS_"
																		+ "32X001100101785V"
																		+ "_IDM");
														coordinatorScheduleTps.setMessageVersion(BigDecimal.valueOf(1));
														coordinatorScheduleTps.setMessageType("A01");
														coordinatorScheduleTps.setProcessType("A19");
														coordinatorScheduleTps.setScheduleClassificationType("A01");
														coordinatorScheduleTps.setSenderIdentificationV(
																coordinatorSenderIdentification);
														coordinatorScheduleTps.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														coordinatorScheduleTps.setSenderRole("A01");
														coordinatorScheduleTps.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														coordinatorScheduleTps.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														coordinatorScheduleTps
																.setReceiverRole(schedule.getReceiverRole());
														coordinatorScheduleTps
																.setMessageDateTime(schedule.getMessageDateTime());
														coordinatorScheduleTps.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														String[] scheduleTimeIntervalParts = coordinatorScheduleTps
																.getScheduleTimeInterval().split("/");
														coordinatorScheduleTps.setScheduleTimeStart(
																LocalDateTime.parse(scheduleTimeIntervalParts[0],
																		DateTimeFormatter.ISO_DATE_TIME));
														coordinatorScheduleTps.setScheduleTimeEnd(
																LocalDateTime.parse(scheduleTimeIntervalParts[1],
																		DateTimeFormatter.ISO_DATE_TIME));
														coordinatorScheduleTps.setIsPPS(false);
														coordinatorScheduleTps.setIsSent(false);
														logger.debug(
																"energyDistributionCamelProcess Save coordinatorScheduleTps");
														trySave(coordinatorScheduleTps, true, true);

														ScheduleTimeSeries tpsScheduleTimeSeriesCoordinator = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																coordinatorScheduleTps, scheduleTps,
																ibexEnergyDealLegList, scheduleTimeSeries, "TS 001",
																secUser);
														createIntervalsWithoutQuantity(intervalList,
																tpsScheduleTimeSeriesCoordinator);

														List<Interval> tpsIntervalListCoordinator = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		tpsScheduleTimeSeriesCoordinator, secUser.getCompany(), false);
														tpsIntervalListCoordinator
																.sort(Comparator.comparing(Interval::getPos));

														tpsIntervalListCoordinator.get(intervalPosition)
																.setQty(tpsIntervalListCoordinator.get(intervalPosition)
																		.getQty().add(purchasedQuantity));
														trySave(tpsIntervalListCoordinator.get(intervalPosition), true,
																true);

														scheduleList.add(coordinatorScheduleTps);

													} else {

														List<ScheduleTimeSeries> coordinatorScheduleTimeSeriesTps = scheduleTimeSeriesRepository
																.findByScheduleAndCompanyAndDeleted(
																		coordinatorScheduleTps, secUser.getCompany(),
																		false);

														List<Interval> intervalListTpsCoordinator = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		coordinatorScheduleTimeSeriesTps.get(0), secUser.getCompany(), false);
														intervalListTpsCoordinator
																.sort(Comparator.comparing(Interval::getPos));

														if (generateNewTps) {
															intervalListTpsCoordinator.get(intervalPosition)
																	.setQty(intervalListTpsCoordinator
																			.get(intervalPosition).getQty()
																			.add(purchasedQuantity));
															trySave(intervalListTpsCoordinator.get(intervalPosition),
																	false, true);

															if (!scheduleList.contains(coordinatorScheduleTps)) {
																scheduleList.add(coordinatorScheduleTps);
															}
														}

														// Creating ScheduleTimeSeries
														if (counterOfIntervals == 0 && !generateNewTps) {

															if (coordinatorScheduleTimeSeriesTps.size() == 1) {

																newScheduleTimeSeriesCoordinatorTpsFirst = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																		coordinatorScheduleTps, scheduleTps,
																		ibexEnergyDealLegList,
																		coordinatorScheduleTimeSeriesTps.get(0),
																		"TS 001", secUser);

																if (Objects.equals(
																		newScheduleTimeSeriesCoordinatorTpsFirst
																				.getId(),
																		coordinatorScheduleTimeSeriesTps.get(0)
																				.getId())) {

																	newScheduleTimeSeriesCoordinatorTpsFirst = createScheduleTimeSeries(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 001");
																	createIntervals(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesCoordinatorTpsFirst);

																	newScheduleTimeSeriesCoordinatorTpsSecond = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																			coordinatorScheduleTps, scheduleTps,
																			ibexEnergyDealLegList,
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 002", secUser);
																} else if (!newScheduleTimeSeriesCoordinatorTpsFirst
																		.getId().equals(coordinatorScheduleTimeSeriesTps
																				.get(0).getId())) {

																	newScheduleTimeSeriesCoordinatorTpsFirst
																			.setSendersTimeSeriesIdentification(
																					"TS 002");
																	createIntervalsWithoutQuantity(
																			intervalListTpsCoordinator,
																			newScheduleTimeSeriesCoordinatorTpsFirst);

																	newScheduleTimeSeriesCoordinatorTpsSecond = createScheduleTimeSeries(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 001");
																	createIntervals(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesCoordinatorTpsSecond);
																}
															} else {

																newScheduleTimeSeriesCoordinatorTpsFirst = createScheduleTimeSeries(
																		coordinatorScheduleTimeSeriesTps.get(0),
																		"TS 001");
																createIntervals(coordinatorScheduleTimeSeriesTps.get(0),
																		newScheduleTimeSeriesCoordinatorTpsFirst);
																newScheduleTimeSeriesCoordinatorTpsSecond = createScheduleTimeSeries(
																		coordinatorScheduleTimeSeriesTps.get(1),
																		"TS 002");
																createIntervals(coordinatorScheduleTimeSeriesTps.get(1),
																		newScheduleTimeSeriesCoordinatorTpsSecond);

															}
														}

														// Distributing quantity to interval
														if (!generateNewTps) {
															if (coordinatorScheduleTimeSeriesTps.size() == 1) {

																List<Interval> newCoordinatorIntervalListTps = intervalRepository
																		.findByScheduleTimeSeriesAndCompanyAndDeleted(
																				newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																newCoordinatorIntervalListTps
																		.sort(Comparator.comparing(Interval::getPos));
																newCoordinatorIntervalListTps.get(intervalPosition)
																		.setQty(newCoordinatorIntervalListTps
																				.get(intervalPosition).getQty()
																				.add(purchasedQuantity));

															} else {

																if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("SELL")) {

																	if (newScheduleTimeSeriesCoordinatorTpsFirst
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	} else if (newScheduleTimeSeriesCoordinatorTpsSecond
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	}

																} else if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("BUY")) {

																	if (newScheduleTimeSeriesCoordinatorTpsFirst
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	} else if (newScheduleTimeSeriesCoordinatorTpsSecond
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(purchasedQuantity));
																	}
																}
															}
														}

														// Create new Schedule Coordinator TPS with greater
														// messageVersion
														if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
																- indexOfIntervalPosStart) && !generateNewTps) {

															Schedule newSchedule = new Schedule();
															newSchedule.setMessageIdentification(
																	coordinatorScheduleTps.getMessageIdentification());
															newSchedule.setMessageVersion(BigDecimal
																	.valueOf(coordinatorScheduleTps.getMessageVersion()
																			.add(BigDecimal.ONE).intValue()));
															newSchedule.setMessageType(
																	coordinatorScheduleTps.getMessageType());
															newSchedule.setProcessType(
																	coordinatorScheduleTps.getProcessType());
															newSchedule.setScheduleClassificationType(
																	coordinatorScheduleTps
																			.getScheduleClassificationType());
															newSchedule.setSenderIdentificationV(
																	coordinatorScheduleTps.getSenderIdentificationV());
															newSchedule.setSenderIdentificationCodingScheme(
																	coordinatorScheduleTps
																			.getSenderIdentificationCodingScheme());
															newSchedule.setSenderRole(
																	coordinatorScheduleTps.getSenderRole());
															newSchedule
																	.setReceiverIdentificationV(coordinatorScheduleTps
																			.getReceiverIdentificationV());
															newSchedule.setReceiverIdentificationCodingScheme(
																	coordinatorScheduleTps
																			.getReceiverIdentificationCodingScheme());
															newSchedule.setReceiverRole(
																	coordinatorScheduleTps.getReceiverRole());
															newSchedule.setMessageDateTime(
																	coordinatorScheduleTps.getMessageDateTime());
															newSchedule.setScheduleTimeInterval(
																	coordinatorScheduleTps.getScheduleTimeInterval());
															newSchedule.setScheduleTimeStart(
																	coordinatorScheduleTps.getScheduleTimeStart());
															newSchedule.setScheduleTimeEnd(
																	coordinatorScheduleTps.getScheduleTimeEnd());
															newSchedule.setIsPPS(false);
															newSchedule.setIsSent(false);
															logger.debug(
																	"energyDistributionCamelProcess Save newSchedule");
															trySave(newSchedule, true, false);

															newScheduleTimeSeriesCoordinatorTpsFirst
																	.setSchedule(newSchedule);
															newScheduleTimeSeriesCoordinatorTpsFirst
																	.setSendersTimeSeriesVersion(String
																			.valueOf(newSchedule.getMessageVersion()));
															trySave(newScheduleTimeSeriesCoordinatorTpsFirst, true,
																	false);

															if (newScheduleTimeSeriesCoordinatorTpsSecond != null
																	&& !Objects.equals(
																			newScheduleTimeSeriesCoordinatorTpsSecond
																					.getId(),
																			coordinatorScheduleTimeSeriesTps.get(0)
																					.getId())) {

																newScheduleTimeSeriesCoordinatorTpsSecond
																		.setSchedule(newSchedule);
																newScheduleTimeSeriesCoordinatorTpsSecond
																		.setSendersTimeSeriesVersion(String.valueOf(
																				newSchedule.getMessageVersion()));
																trySave(newScheduleTimeSeriesCoordinatorTpsSecond, true,
																		true);
															}

															if (!scheduleList.contains(newSchedule)) {
																scheduleList.add(newSchedule);
															}
															coordinatorScheduleTps = newSchedule;
														}
													}

													if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
															- indexOfIntervalPosStart)) {

														Schedule newSchedule = new Schedule();
														newSchedule.setMessageIdentification(
																schedule.getMessageIdentification());
														newSchedule.setMessageVersion(BigDecimal.valueOf(schedule
																.getMessageVersion().add(BigDecimal.ONE).intValue()));
														newSchedule.setMessageType(schedule.getMessageType());
														newSchedule.setProcessType(schedule.getProcessType());
														newSchedule.setScheduleClassificationType(
																schedule.getScheduleClassificationType());
														newSchedule.setSenderIdentificationV(
																schedule.getSenderIdentificationV());
														newSchedule.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														newSchedule.setSenderRole(schedule.getSenderRole());
														newSchedule.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														newSchedule.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														newSchedule.setReceiverRole(schedule.getReceiverRole());
														newSchedule.setMessageDateTime(schedule.getMessageDateTime());
														newSchedule.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														newSchedule
																.setScheduleTimeStart(schedule.getScheduleTimeStart());
														newSchedule.setScheduleTimeEnd(schedule.getScheduleTimeEnd());
														newSchedule.setIsPPS(true);
														newSchedule.setIsSent(false);
														logger.debug("energyDistributionCamelProcess Save newSchedule");
														trySave(newSchedule, true, false);

														newScheduleTimeSeries
																.setSendersTimeSeriesIdentification(scheduleTimeSeries
																		.getSendersTimeSeriesIdentification());
														newScheduleTimeSeries.setSendersTimeSeriesVersion(
																String.valueOf(newSchedule.getMessageVersion()));
														newScheduleTimeSeries
																.setBusinessType(scheduleTimeSeries.getBusinessType());
														newScheduleTimeSeries
																.setProduct(scheduleTimeSeries.getProduct());
														newScheduleTimeSeries.setObjectAggregation(
																scheduleTimeSeries.getObjectAggregation());
														newScheduleTimeSeries
																.setInAreaV(scheduleTimeSeries.getInAreaV());
														newScheduleTimeSeries.setInAreaCodingScheme(
																scheduleTimeSeries.getInAreaCodingScheme());
														newScheduleTimeSeries
																.setOutAreaV(scheduleTimeSeries.getOutAreaV());
														newScheduleTimeSeries.setOutAreaCodingScheme(
																scheduleTimeSeries.getOutAreaCodingScheme());
														newScheduleTimeSeries
																.setInPartyV(scheduleTimeSeries.getInPartyV());
														newScheduleTimeSeries.setInPartyCodingScheme(
																scheduleTimeSeries.getInPartyCodingScheme());
														newScheduleTimeSeries
																.setOutPartyV(scheduleTimeSeries.getOutPartyV());
														newScheduleTimeSeries.setOutPartyCodingScheme(
																scheduleTimeSeries.getOutPartyCodingScheme());
														newScheduleTimeSeries.setMeasurementUnit(
																scheduleTimeSeries.getMeasurementUnit());
														newScheduleTimeSeries
																.setTimeInterval(scheduleTimeSeries.getTimeInterval());
														newScheduleTimeSeries
																.setResolution(scheduleTimeSeries.getResolution());
														newScheduleTimeSeries.setSchedule(newSchedule);
														trySave(newScheduleTimeSeries, true, false);

														if (!scheduleList.contains(newSchedule)) {
															scheduleList.add(newSchedule);
														}
														schedule = newSchedule;

														purchasedQuantity = BigDecimal.valueOf(0);

													}
													counterOfIntervals++;

													// purchasedQuantity is greater than powerPlantConsumingCapacity
												} else if (purchasedQuantity.compareTo(powerPlantConsumingCapacity) > 0
														&& !powerPlantConsumingCapacity.equals(BigDecimal.ZERO)) {

													counterOfDistributedIntervals++;

													if (counterOfNewIntervals == 0) {
														logger.debug(
																"energyDistributionCamelProcess Save newScheduleTimeSeries");
														trySave(newScheduleTimeSeries, true, false);

														for (Interval interval : intervalList) {

															Interval newInterval = new Interval();
															newInterval.setPos(interval.getPos());
															newInterval.setQty(interval.getQty());
															newInterval.setScheduleTimeSeries(newScheduleTimeSeries);
															if (trySave(newInterval, true, false) == null) {
																logger.error("Error at saving Interval");
															}
														}
														counterOfNewIntervals++;
													}

													List<Interval> newIntervalList = intervalRepository
															.findByScheduleTimeSeriesAndCompanyAndDeleted(newScheduleTimeSeries, secUser.getCompany(), false);
													newIntervalList.sort(Comparator.comparing(Interval::getPos));
													if (!ibexEnergyDealLegList.isEmpty()
															&& ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
														newIntervalList.get(intervalPosition)
																.setQty(newIntervalList.get(intervalPosition).getQty()
																		.add(powerPlantConsumingCapacity));
													} else {
														newIntervalList.get(intervalPosition)
																.setQty(newIntervalList.get(intervalPosition).getQty()
																		.subtract(powerPlantConsumingCapacity));
														if (newIntervalList.get(intervalPosition).getQty()
																.compareTo(BigDecimal.ZERO) < 0) {
															newIntervalList.get(intervalPosition)
																	.setQty(BigDecimal.ZERO);
														}
													}
													logger.debug("energyDistributionCamelProcess Save interval");
													trySave(newIntervalList.get(intervalPosition), true, true);

													// Checking for existing TPS
													if (scheduleTps == null) {

														generateNewTps = true;

														// Generate TPS
														scheduleTps = new Schedule();
														scheduleTps.setMessageIdentification(
																ibexEnergyDealLegDeliveryStart
																		+ "_TPS_" + schedule.getSenderIdentificationV()
																		+ "_IDM");
														scheduleTps.setMessageVersion(BigDecimal.valueOf(1));
														scheduleTps.setMessageType("A01");
														scheduleTps.setProcessType("A19");
														scheduleTps.setScheduleClassificationType("A01");
														scheduleTps.setSenderIdentificationV(
																schedule.getSenderIdentificationV());
														scheduleTps.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														scheduleTps.setSenderRole("A01");
														scheduleTps.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														scheduleTps.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														scheduleTps.setReceiverRole(schedule.getReceiverRole());
														scheduleTps.setMessageDateTime(schedule.getMessageDateTime());
														scheduleTps.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														String[] scheduleTimeIntervalParts = scheduleTps
																.getScheduleTimeInterval().split("/");
														scheduleTps.setScheduleTimeStart(
																LocalDateTime.parse(scheduleTimeIntervalParts[0],
																		DateTimeFormatter.ISO_DATE_TIME));
														scheduleTps.setScheduleTimeEnd(
																LocalDateTime.parse(scheduleTimeIntervalParts[1],
																		DateTimeFormatter.ISO_DATE_TIME));
														scheduleTps.setIsPPS(false);
														scheduleTps.setIsSent(false);
														logger.debug("energyDistributionCamelProcess Save scheduleTps");
														trySave(scheduleTps, true, true);

														ScheduleTimeSeries tpsScheduleTimeSeries = findOrCreateScheduleTimeSeriesForTPS(
																scheduleTps, ibexEnergyDealLegList, scheduleTimeSeries,
																"TS 001", secUser);
														createIntervalsWithoutQuantity(intervalList,
																tpsScheduleTimeSeries);

														List<Interval> tpsIntervalList = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		tpsScheduleTimeSeries, secUser.getCompany(), false);
														tpsIntervalList.sort(Comparator.comparing(Interval::getPos));

														tpsIntervalList.get(intervalPosition)
																.setQty(powerPlantConsumingCapacity);
														trySave(tpsIntervalList.get(intervalPosition), true, true);

														logger.debug("purchasedQuantity - " + purchasedQuantity);
														scheduleList.add(scheduleTps);

													} else {

														List<ScheduleTimeSeries> scheduleTimeSeriesTps = scheduleTimeSeriesRepository
																.findByScheduleAndCompanyAndDeleted(scheduleTps,
																		secUser.getCompany(), false);

														List<Interval> intervalListTps = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		scheduleTimeSeriesTps.get(0), secUser.getCompany(), false);
														intervalListTps.sort(Comparator.comparing(Interval::getPos));

														if (generateNewTps) {
															intervalListTps.get(intervalPosition)
																	.setQty(intervalListTps.get(intervalPosition)
																			.getQty().add(powerPlantConsumingCapacity));
															logger.debug(
																	"energyDistributionCamelProcess Save interval");
															trySave(intervalListTps.get(intervalPosition), true, true);

															if (!scheduleList.contains(scheduleTps)) {
																scheduleList.add(scheduleTps);
															}
														}

														if (counterOfIntervals == 0 && !generateNewTps) {

															if (scheduleTimeSeriesTps.size() == 1) {

																newScheduleTimeSeriesTpsFirst = findOrCreateScheduleTimeSeriesForTPS(
																		scheduleTps, ibexEnergyDealLegList,
																		scheduleTimeSeriesTps.get(0), "TS 001",
																		secUser);

																if (Objects.equals(
																		newScheduleTimeSeriesTpsFirst.getId(),
																		scheduleTimeSeriesTps.get(0).getId())) {

																	newScheduleTimeSeriesTpsFirst = createScheduleTimeSeries(
																			scheduleTimeSeriesTps.get(0), "TS 001");
																	createIntervals(scheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesTpsFirst);

																	newScheduleTimeSeriesTpsSecond = findOrCreateScheduleTimeSeriesForTPS(
																			scheduleTps, ibexEnergyDealLegList,
																			scheduleTimeSeriesTps.get(0), "TS 002",
																			secUser);

																} else if (!newScheduleTimeSeriesTpsFirst.getId()
																		.equals(scheduleTimeSeriesTps.get(0).getId())) {

																	newScheduleTimeSeriesTpsFirst
																			.setSendersTimeSeriesIdentification(
																					"TS 002");
																	createIntervalsWithoutQuantity(intervalListTps,
																			newScheduleTimeSeriesTpsFirst);

																	newScheduleTimeSeriesTpsSecond = createScheduleTimeSeries(
																			scheduleTimeSeriesTps.get(0), "TS 001");
																	createIntervals(scheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesTpsSecond);
																}

															} else {
																newScheduleTimeSeriesTpsFirst = createScheduleTimeSeries(
																		scheduleTimeSeriesTps.get(0), "TS 001");
																createIntervals(scheduleTimeSeriesTps.get(0),
																		newScheduleTimeSeriesTpsFirst);
																newScheduleTimeSeriesTpsSecond = createScheduleTimeSeries(
																		scheduleTimeSeriesTps.get(1), "TS 002");
																createIntervals(scheduleTimeSeriesTps.get(1),
																		newScheduleTimeSeriesTpsSecond);
															}
														}

														// Distributing quantity to interval
														if (!generateNewTps) {
															if (scheduleTimeSeriesTps.size() == 1) {

																List<Interval> newIntervalListTps = intervalRepository
																		.findByScheduleTimeSeriesAndCompanyAndDeleted(
																				newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																newIntervalListTps
																		.sort(Comparator.comparing(Interval::getPos));
																newIntervalListTps.get(intervalPosition)
																		.setQty(newIntervalListTps.get(intervalPosition)
																				.getQty()
																				.add(powerPlantConsumingCapacity));

															} else {

																if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("SELL")) {

																	if (newScheduleTimeSeriesTpsFirst.getInPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	} else if (newScheduleTimeSeriesTpsSecond
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	}

																} else if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("BUY")) {

																	if (newScheduleTimeSeriesTpsFirst.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	} else if (newScheduleTimeSeriesTpsSecond
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	}
																}
															}
														}

														// Create new Schedule TPS with greater messageVersion
														if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
																- indexOfIntervalPosStart) && !generateNewTps) {

															Schedule newSchedule = new Schedule();
															newSchedule.setMessageIdentification(
																	scheduleTps.getMessageIdentification());
															newSchedule.setMessageVersion(
																	BigDecimal.valueOf(scheduleTps.getMessageVersion()
																			.add(BigDecimal.ONE).intValue()));
															newSchedule.setMessageType(scheduleTps.getMessageType());
															newSchedule.setProcessType(scheduleTps.getProcessType());
															newSchedule.setScheduleClassificationType(
																	scheduleTps.getScheduleClassificationType());
															newSchedule.setSenderIdentificationV(
																	scheduleTps.getSenderIdentificationV());
															newSchedule.setSenderIdentificationCodingScheme(
																	scheduleTps.getSenderIdentificationCodingScheme());
															newSchedule.setSenderRole(scheduleTps.getSenderRole());
															newSchedule.setReceiverIdentificationV(
																	scheduleTps.getReceiverIdentificationV());
															newSchedule
																	.setReceiverIdentificationCodingScheme(scheduleTps
																			.getReceiverIdentificationCodingScheme());
															newSchedule.setReceiverRole(scheduleTps.getReceiverRole());
															newSchedule.setMessageDateTime(
																	scheduleTps.getMessageDateTime());
															newSchedule.setScheduleTimeInterval(
																	scheduleTps.getScheduleTimeInterval());
															newSchedule.setScheduleTimeStart(
																	scheduleTps.getScheduleTimeStart());
															newSchedule.setScheduleTimeEnd(
																	scheduleTps.getScheduleTimeEnd());
															newSchedule.setIsPPS(false);
															newSchedule.setIsSent(false);
															logger.debug(
																	"energyDistributionCamelProcess Save newSchedule");
															trySave(newSchedule, true, true);

															newScheduleTimeSeriesTpsFirst.setSendersTimeSeriesVersion(
																	String.valueOf(newSchedule.getMessageVersion()));
															newScheduleTimeSeriesTpsFirst.setSchedule(newSchedule);
															trySave(newScheduleTimeSeriesTpsFirst, true, false);

															if (newScheduleTimeSeriesTpsSecond != null && !Objects
																	.equals(newScheduleTimeSeriesTpsSecond.getId(),
																			scheduleTimeSeriesTps.get(0).getId())) {
																newScheduleTimeSeriesTpsSecond
																		.setSendersTimeSeriesVersion(String.valueOf(
																				newSchedule.getMessageVersion()));
																newScheduleTimeSeriesTpsSecond.setSchedule(newSchedule);
																trySave(newScheduleTimeSeriesTpsSecond, true, true);
															}

															if (!scheduleList.contains(newSchedule)) {
																scheduleList.add(newSchedule);
															}
															scheduleTps = newSchedule;
														}
													}

													// Checking for existing TPS
													if (coordinatorScheduleTps == null) {

														generateNewTps = true;

														// Generate TPS for Coordinator
														coordinatorScheduleTps = new Schedule();
														coordinatorScheduleTps.setMessageIdentification(
																ibexEnergyDealLegDeliveryStart + "_TPS_"
																		+ "32X001100101785V"
																		+ "_IDM");
														coordinatorScheduleTps.setMessageVersion(BigDecimal.valueOf(1));
														coordinatorScheduleTps.setMessageType("A01");
														coordinatorScheduleTps.setProcessType("A19");
														coordinatorScheduleTps.setScheduleClassificationType("A01");
														coordinatorScheduleTps.setSenderIdentificationV(
																coordinatorSenderIdentification);
														coordinatorScheduleTps.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														coordinatorScheduleTps.setSenderRole("A01");
														coordinatorScheduleTps.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														coordinatorScheduleTps.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														coordinatorScheduleTps
																.setReceiverRole(schedule.getReceiverRole());
														coordinatorScheduleTps
																.setMessageDateTime(schedule.getMessageDateTime());
														coordinatorScheduleTps.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														String[] scheduleTimeIntervalParts = coordinatorScheduleTps
																.getScheduleTimeInterval().split("/");
														coordinatorScheduleTps.setScheduleTimeStart(
																LocalDateTime.parse(scheduleTimeIntervalParts[0],
																		DateTimeFormatter.ISO_DATE_TIME));
														coordinatorScheduleTps.setScheduleTimeEnd(
																LocalDateTime.parse(scheduleTimeIntervalParts[1],
																		DateTimeFormatter.ISO_DATE_TIME));
														coordinatorScheduleTps.setIsPPS(false);
														coordinatorScheduleTps.setIsSent(false);
														logger.debug(
																"energyDistributionCamelProcess Save coordinatorScheduleTps");
														trySave(coordinatorScheduleTps, true, true);

														ScheduleTimeSeries tpsScheduleTimeSeriesCoordinator = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																coordinatorScheduleTps, scheduleTps,
																ibexEnergyDealLegList, scheduleTimeSeries, "TS 001",
																secUser);
														createIntervalsWithoutQuantity(intervalList,
																tpsScheduleTimeSeriesCoordinator);
														//
														List<Interval> tpsIntervalListCoordinator = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		tpsScheduleTimeSeriesCoordinator, secUser.getCompany(), false);
														tpsIntervalListCoordinator
																.sort(Comparator.comparing(Interval::getPos));	

														tpsIntervalListCoordinator.get(intervalPosition)
																.setQty(powerPlantConsumingCapacity);
														trySave(tpsIntervalListCoordinator.get(intervalPosition), true,
																true);

														scheduleList.add(coordinatorScheduleTps);

													} else {

														List<ScheduleTimeSeries> coordinatorScheduleTimeSeriesTps = scheduleTimeSeriesRepository
																.findByScheduleAndCompanyAndDeleted(
																		coordinatorScheduleTps, secUser.getCompany(),
																		false);

														List<Interval> intervalListTpsCoordinator = intervalRepository
																.findByScheduleTimeSeriesAndCompanyAndDeleted(
																		coordinatorScheduleTimeSeriesTps.get(0), secUser.getCompany(), false);
														intervalListTpsCoordinator
																.sort(Comparator.comparing(Interval::getPos));

														if (generateNewTps) {
															intervalListTpsCoordinator.get(intervalPosition)
																	.setQty(intervalListTpsCoordinator
																			.get(intervalPosition).getQty()
																			.add(powerPlantConsumingCapacity));
															logger.debug(
																	"energyDistributionCamelProcess Save interval");
															trySave(intervalListTpsCoordinator.get(intervalPosition),
																	true, true);

															if (!scheduleList.contains(coordinatorScheduleTps)) {
																scheduleList.add(coordinatorScheduleTps);
															}
														}

														// Creating ScheduleTimeSeries
														if (counterOfIntervals == 0 && !generateNewTps) {

															if (coordinatorScheduleTimeSeriesTps.size() == 1) {

																newScheduleTimeSeriesCoordinatorTpsFirst = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																		coordinatorScheduleTps, scheduleTps,
																		ibexEnergyDealLegList,
																		coordinatorScheduleTimeSeriesTps.get(0),
																		"TS 001", secUser);

																if (Objects.equals(
																		newScheduleTimeSeriesCoordinatorTpsFirst
																				.getId(),
																		coordinatorScheduleTimeSeriesTps.get(0)
																				.getId())) {

																	newScheduleTimeSeriesCoordinatorTpsFirst = createScheduleTimeSeries(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 001");
																	createIntervals(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesCoordinatorTpsFirst);

																	newScheduleTimeSeriesCoordinatorTpsSecond = findOrCreateScheduleTimeSeriesForTPSCoordinator(
																			coordinatorScheduleTps, scheduleTps,
																			ibexEnergyDealLegList,
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 002", secUser);
																} else if (!newScheduleTimeSeriesCoordinatorTpsFirst
																		.getId().equals(coordinatorScheduleTimeSeriesTps
																				.get(0).getId())) {

																	newScheduleTimeSeriesCoordinatorTpsFirst
																			.setSendersTimeSeriesIdentification(
																					"TS 002");
																	createIntervalsWithoutQuantity(
																			intervalListTpsCoordinator,
																			newScheduleTimeSeriesCoordinatorTpsFirst);

																	newScheduleTimeSeriesCoordinatorTpsSecond = createScheduleTimeSeries(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			"TS 001");
																	createIntervals(
																			coordinatorScheduleTimeSeriesTps.get(0),
																			newScheduleTimeSeriesCoordinatorTpsSecond);
																}
															} else {

																newScheduleTimeSeriesCoordinatorTpsFirst = createScheduleTimeSeries(
																		coordinatorScheduleTimeSeriesTps.get(0),
																		"TS 001");
																createIntervals(coordinatorScheduleTimeSeriesTps.get(0),
																		newScheduleTimeSeriesCoordinatorTpsFirst);
																newScheduleTimeSeriesCoordinatorTpsSecond = createScheduleTimeSeries(
																		coordinatorScheduleTimeSeriesTps.get(1),
																		"TS 002");
																createIntervals(coordinatorScheduleTimeSeriesTps.get(1),
																		newScheduleTimeSeriesCoordinatorTpsSecond);

															}
														}

														// Distributing quantity to interval
														if (!generateNewTps) {
															if (coordinatorScheduleTimeSeriesTps.size() == 1) {

																List<Interval> newCoordinatorIntervalListTps = intervalRepository
																		.findByScheduleTimeSeriesAndCompanyAndDeleted(
																				newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																newCoordinatorIntervalListTps
																		.sort(Comparator.comparing(Interval::getPos));
																newCoordinatorIntervalListTps.get(intervalPosition)
																		.setQty(newCoordinatorIntervalListTps
																				.get(intervalPosition).getQty()
																				.add(powerPlantConsumingCapacity));

															} else {

																if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("SELL")) {

																	if (newScheduleTimeSeriesCoordinatorTpsFirst
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	} else if (newScheduleTimeSeriesCoordinatorTpsSecond
																			.getInPartyV().equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	}

																} else if (ibexEnergyDealLegList.get(0).getSide()
																		.equals("BUY")) {

																	if (newScheduleTimeSeriesCoordinatorTpsFirst
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsFirst, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	} else if (newScheduleTimeSeriesCoordinatorTpsSecond
																			.getOutPartyV()
																			.equals("32X001100101785V")) {
																		List<Interval> newCoordinatorIntervalListTps = intervalRepository
																				.findByScheduleTimeSeriesAndCompanyAndDeleted(
																						newScheduleTimeSeriesCoordinatorTpsSecond, secUser.getCompany(), false);
																		newCoordinatorIntervalListTps.sort(
																				Comparator.comparing(Interval::getPos));
																		newCoordinatorIntervalListTps
																				.get(intervalPosition)
																				.setQty(newCoordinatorIntervalListTps
																						.get(intervalPosition).getQty()
																						.add(powerPlantConsumingCapacity));
																	}
																}
															}
														}

														// Create new Schedule Coordinator TPS with greater
														// messageVersion
														if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
																- indexOfIntervalPosStart) && !generateNewTps) {

															Schedule newSchedule = new Schedule();
															newSchedule.setMessageIdentification(
																	coordinatorScheduleTps.getMessageIdentification());
															newSchedule.setMessageVersion(BigDecimal
																	.valueOf(coordinatorScheduleTps.getMessageVersion()
																			.add(BigDecimal.ONE).intValue()));
															newSchedule.setMessageType(
																	coordinatorScheduleTps.getMessageType());
															newSchedule.setProcessType(
																	coordinatorScheduleTps.getProcessType());
															newSchedule.setScheduleClassificationType(
																	coordinatorScheduleTps
																			.getScheduleClassificationType());
															newSchedule.setSenderIdentificationV(
																	coordinatorScheduleTps.getSenderIdentificationV());
															newSchedule.setSenderIdentificationCodingScheme(
																	coordinatorScheduleTps
																			.getSenderIdentificationCodingScheme());
															newSchedule.setSenderRole(
																	coordinatorScheduleTps.getSenderRole());
															newSchedule
																	.setReceiverIdentificationV(coordinatorScheduleTps
																			.getReceiverIdentificationV());
															newSchedule.setReceiverIdentificationCodingScheme(
																	coordinatorScheduleTps
																			.getReceiverIdentificationCodingScheme());
															newSchedule.setReceiverRole(
																	coordinatorScheduleTps.getReceiverRole());
															newSchedule.setMessageDateTime(
																	coordinatorScheduleTps.getMessageDateTime());
															newSchedule.setScheduleTimeInterval(
																	coordinatorScheduleTps.getScheduleTimeInterval());
															newSchedule.setScheduleTimeStart(
																	coordinatorScheduleTps.getScheduleTimeStart());
															newSchedule.setScheduleTimeEnd(
																	coordinatorScheduleTps.getScheduleTimeEnd());
															newSchedule.setIsPPS(false);
															newSchedule.setIsSent(false);
															logger.debug(
																	"energyDistributionCamelProcess Save newSchedule");
															trySave(newSchedule, true, false);

															newScheduleTimeSeriesCoordinatorTpsFirst
																	.setSchedule(newSchedule);
															newScheduleTimeSeriesCoordinatorTpsFirst
																	.setSendersTimeSeriesVersion(String
																			.valueOf(newSchedule.getMessageVersion()));
															trySave(newScheduleTimeSeriesCoordinatorTpsFirst, true,
																	false);

															if (newScheduleTimeSeriesCoordinatorTpsSecond != null
																	&& !Objects.equals(
																			newScheduleTimeSeriesCoordinatorTpsSecond
																					.getId(),
																			coordinatorScheduleTimeSeriesTps.get(0)
																					.getId())) {

																newScheduleTimeSeriesCoordinatorTpsSecond
																		.setSchedule(newSchedule);
																newScheduleTimeSeriesCoordinatorTpsSecond
																		.setSendersTimeSeriesVersion(String.valueOf(
																				newSchedule.getMessageVersion()));
																trySave(newScheduleTimeSeriesCoordinatorTpsSecond, true,
																		true);
															}

															if (!scheduleList.contains(newSchedule)) {
																scheduleList.add(newSchedule);
															}
															coordinatorScheduleTps = newSchedule;
														}
													}

													if (counterOfDistributedIntervals == (indexOfIntervalPosEnd
															- indexOfIntervalPosStart)) {

														Schedule newSchedule = new Schedule();
														newSchedule.setMessageIdentification(
																schedule.getMessageIdentification());
														newSchedule.setMessageVersion(BigDecimal.valueOf(schedule
																.getMessageVersion().add(BigDecimal.ONE).intValue()));
														newSchedule.setMessageType(schedule.getMessageType());
														newSchedule.setProcessType(schedule.getProcessType());
														newSchedule.setScheduleClassificationType(
																schedule.getScheduleClassificationType());
														newSchedule.setSenderIdentificationV(
																schedule.getSenderIdentificationV());
														newSchedule.setSenderIdentificationCodingScheme(
																schedule.getSenderIdentificationCodingScheme());
														newSchedule.setSenderRole(schedule.getSenderRole());
														newSchedule.setReceiverIdentificationV(
																schedule.getReceiverIdentificationV());
														newSchedule.setReceiverIdentificationCodingScheme(
																schedule.getReceiverIdentificationCodingScheme());
														newSchedule.setReceiverRole(schedule.getReceiverRole());
														newSchedule.setMessageDateTime(schedule.getMessageDateTime());
														newSchedule.setScheduleTimeInterval(
																schedule.getScheduleTimeInterval());
														newSchedule
																.setScheduleTimeStart(schedule.getScheduleTimeStart());
														newSchedule.setScheduleTimeEnd(schedule.getScheduleTimeEnd());
														newSchedule.setIsPPS(true);
														newSchedule.setIsSent(false);
														logger.debug("energyDistributionCamelProcess Save newSchedule");
														trySave(newSchedule, true, false);

														newScheduleTimeSeries
																.setSendersTimeSeriesIdentification(scheduleTimeSeries
																		.getSendersTimeSeriesIdentification());
														newScheduleTimeSeries.setSendersTimeSeriesVersion(
																String.valueOf(newSchedule.getMessageVersion()));
														newScheduleTimeSeries
																.setBusinessType(scheduleTimeSeries.getBusinessType());
														newScheduleTimeSeries
																.setProduct(scheduleTimeSeries.getProduct());
														newScheduleTimeSeries.setObjectAggregation(
																scheduleTimeSeries.getObjectAggregation());
														newScheduleTimeSeries
																.setInAreaV(scheduleTimeSeries.getInAreaV());
														newScheduleTimeSeries.setInAreaCodingScheme(
																scheduleTimeSeries.getInAreaCodingScheme());
														newScheduleTimeSeries
																.setOutAreaV(scheduleTimeSeries.getOutAreaV());
														newScheduleTimeSeries.setOutAreaCodingScheme(
																scheduleTimeSeries.getOutAreaCodingScheme());
														newScheduleTimeSeries
																.setInPartyV(scheduleTimeSeries.getInPartyV());
														newScheduleTimeSeries.setInPartyCodingScheme(
																scheduleTimeSeries.getInPartyCodingScheme());
														newScheduleTimeSeries
																.setOutPartyV(scheduleTimeSeries.getOutPartyV());
														newScheduleTimeSeries.setOutPartyCodingScheme(
																scheduleTimeSeries.getOutPartyCodingScheme());
														newScheduleTimeSeries.setMeasurementUnit(
																scheduleTimeSeries.getMeasurementUnit());
														newScheduleTimeSeries
																.setTimeInterval(scheduleTimeSeries.getTimeInterval());
														newScheduleTimeSeries
																.setResolution(scheduleTimeSeries.getResolution());
														newScheduleTimeSeries.setSchedule(newSchedule);
														trySave(newScheduleTimeSeries, true, false);

														if (!scheduleList.contains(newSchedule)) {
															scheduleList.add(newSchedule);
														}
														schedule = newSchedule;
													}
													counterOfIntervals++;
												}
											}
										}
									}
								}
							}
						}
					}
					logger.debug("energyDistributionCamelProcess Save distribution");
					if (purchasedQuantity.compareTo(BigDecimal.ZERO) == 0) {

						ibexEnergyDealList.get(energyDeal).setIsDistributedToSchedules(true);
						if (trySave(ibexEnergyDealList.get(energyDeal), true, true) == null) {
							logger.error("Error at saving ibexEnergyDeal - energyDistribution");
						}
					} else if ((powerPlantList.isEmpty() || scheduleList.isEmpty()) && objectMap.isEmpty()) {
						 objectMap.put("Warnings", "MissingPowerPlantOrSchedule");
						 logger.error("MissingPowerPlantOrSchedule");
					} else if (objectMap.isEmpty()) {
						objectMap.put("Warnings", "UndistributedQuantities");
						logger.error("UndistributedQuantities!");
					}

					// TODO create ibexEnergyDealTradeTime as parameter
					// ibexEnergyDealTradeTime = ibexEnergyDeal.getTradeTime();
				}
				objectMap.put("Result", scheduleList);
				result.add(objectMap);
				scheduleListToSend.addAll(scheduleList);
					scheduleList.clear();
				}
		} else if (ibexEnergyDealListSize != 0) {
			objectMap.put("Warnings", "WrongPortfolioId");
		}

		result.add(objectMap);

		//	logger.debug("energyDistributionCamelProcess sendMail");
		logger.info("Schedules updated by deal distribution: " + scheduleListToSend.size());
		if (!scheduleListToSend.isEmpty()) {
			sendScheduleMail(scheduleListToSend);
		}
		logger.debug("End energyDistributionCamelProcess");

		return result;
	}

	private ScheduleTimeSeries findOrCreateScheduleTimeSeriesForTPS(Schedule tpsSchedule,
			List<IbexEnergyDealLeg> ibexEnergyDealLegList, ScheduleTimeSeries scheduleTimeSeries,
			String sendersTimeSeriesIdentification, SecUser secUser) {

		ScheduleTimeSeries tpsScheduleTimeSeries = null;

		if (ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
			tpsScheduleTimeSeries = ((ScheduleTimeSeriesRepository) getRepositories()
					.getRepositoryFor(ScheduleTimeSeries.class).get())
					.findFirstByScheduleAndInPartyVAndOutPartyVAndCompanyAndDeleted(tpsSchedule, "32X001100101785V",
							tpsSchedule.getSenderIdentificationV(), secUser.getCompany(), false);
		} else if (ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {

			tpsScheduleTimeSeries = ((ScheduleTimeSeriesRepository) getRepositories()
					.getRepositoryFor(ScheduleTimeSeries.class).get())
					.findFirstByScheduleAndInPartyVAndOutPartyVAndCompanyAndDeleted(tpsSchedule,
							tpsSchedule.getSenderIdentificationV(), "32X001100101785V", secUser.getCompany(),
							false);
		}

		if (tpsScheduleTimeSeries == null) {

			// Generate new TPS
			tpsScheduleTimeSeries = new ScheduleTimeSeries();

			tpsScheduleTimeSeries.setSendersTimeSeriesIdentification(sendersTimeSeriesIdentification);
			tpsScheduleTimeSeries.setSendersTimeSeriesVersion("1");
			tpsScheduleTimeSeries.setBusinessType("A02");
			tpsScheduleTimeSeries.setProduct(scheduleTimeSeries.getProduct());
			tpsScheduleTimeSeries.setObjectAggregation("A03");
			tpsScheduleTimeSeries.setInAreaV("10YCA-BULGARIA-R");
			tpsScheduleTimeSeries.setInAreaCodingScheme("A01");
			tpsScheduleTimeSeries.setOutAreaV("10YCA-BULGARIA-R");
			tpsScheduleTimeSeries.setOutAreaCodingScheme("A01");

			if (ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
				tpsScheduleTimeSeries.setInPartyV("32X001100101785V");
				tpsScheduleTimeSeries.setOutPartyV(tpsSchedule.getSenderIdentificationV());
			} else if (ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {
				tpsScheduleTimeSeries.setInPartyV(tpsSchedule.getSenderIdentificationV());
				tpsScheduleTimeSeries.setOutPartyV("32X001100101785V");

			}

			tpsScheduleTimeSeries.setInPartyCodingScheme("A01");
			tpsScheduleTimeSeries.setOutPartyCodingScheme("A01");
			tpsScheduleTimeSeries.setMeasurementUnit(scheduleTimeSeries.getMeasurementUnit());
			tpsScheduleTimeSeries.setTimeInterval(scheduleTimeSeries.getTimeInterval());
			tpsScheduleTimeSeries.setResolution(scheduleTimeSeries.getResolution());
			tpsScheduleTimeSeries.setSchedule(tpsSchedule);
			trySave(tpsScheduleTimeSeries, true, false);

		}

		return tpsScheduleTimeSeries;
	}

	private ScheduleTimeSeries createScheduleTimeSeries(ScheduleTimeSeries scheduleTimeSeries,
			String sendersTimeSeriesIdentification) {

		ScheduleTimeSeries tpsScheduleTimeSeries = new ScheduleTimeSeries();

		tpsScheduleTimeSeries.setSendersTimeSeriesIdentification(sendersTimeSeriesIdentification);
		tpsScheduleTimeSeries.setSendersTimeSeriesVersion(scheduleTimeSeries.getSendersTimeSeriesVersion());
		tpsScheduleTimeSeries.setBusinessType(scheduleTimeSeries.getBusinessType());
		tpsScheduleTimeSeries.setProduct(scheduleTimeSeries.getProduct());
		tpsScheduleTimeSeries.setObjectAggregation(scheduleTimeSeries.getObjectAggregation());
		tpsScheduleTimeSeries.setInAreaV(scheduleTimeSeries.getInAreaV());
		tpsScheduleTimeSeries.setInAreaCodingScheme(scheduleTimeSeries.getInAreaCodingScheme());
		tpsScheduleTimeSeries.setOutAreaV(scheduleTimeSeries.getOutAreaV());
		tpsScheduleTimeSeries.setOutAreaCodingScheme(scheduleTimeSeries.getOutAreaCodingScheme());
		tpsScheduleTimeSeries.setInPartyV(scheduleTimeSeries.getInPartyV());
		tpsScheduleTimeSeries.setOutPartyV(scheduleTimeSeries.getOutPartyV());
		tpsScheduleTimeSeries.setInPartyCodingScheme(scheduleTimeSeries.getInPartyCodingScheme());
		tpsScheduleTimeSeries.setOutPartyCodingScheme(scheduleTimeSeries.getOutPartyCodingScheme());
		tpsScheduleTimeSeries.setMeasurementUnit(scheduleTimeSeries.getMeasurementUnit());
		tpsScheduleTimeSeries.setTimeInterval(scheduleTimeSeries.getTimeInterval());
		tpsScheduleTimeSeries.setResolution(scheduleTimeSeries.getResolution());

		trySave(tpsScheduleTimeSeries, true, false);

		return tpsScheduleTimeSeries;
	}

	private void createIntervals(ScheduleTimeSeries scheduleTimeSeriesFrom, ScheduleTimeSeries scheduleTimeSeriesTo) {

		List<Interval> intervalListTps = ((IntervalRepository) getRepositories().getRepositoryFor(Interval.class).get())
				.findByScheduleTimeSeriesAndCompanyAndDeleted(scheduleTimeSeriesFrom, scheduleTimeSeriesFrom.getCompany(), false);

		for (Interval interval : intervalListTps) {
			Interval newInterval = new Interval();
			newInterval.setPos(interval.getPos());
			newInterval.setQty(interval.getQty());
			newInterval.setScheduleTimeSeries(scheduleTimeSeriesTo);
			if (trySave(newInterval, true, false) == null) {
				logger.error("Error at saving Interval");
			}
		}

	}

	private ScheduleTimeSeries findOrCreateScheduleTimeSeriesForTPSCoordinator(Schedule tpsScheduleCoordinator,
			Schedule scheduleTps, List<IbexEnergyDealLeg> ibexEnergyDealLegList, ScheduleTimeSeries scheduleTimeSeries,
			String sendersTimeSeriesIdentification, SecUser secUser) {

		ScheduleTimeSeries tpsScheduleTimeSeriesCoordinator = null;

		if (ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
			tpsScheduleTimeSeriesCoordinator = ((ScheduleTimeSeriesRepository) getRepositories()
					.getRepositoryFor(ScheduleTimeSeries.class).get())
					.findFirstByScheduleAndInPartyVAndOutPartyVAndCompanyAndDeleted(tpsScheduleCoordinator,
							tpsScheduleCoordinator.getSenderIdentificationV(), scheduleTps.getSenderIdentificationV(),
							secUser.getCompany(), false);
		} else if (ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {
			tpsScheduleTimeSeriesCoordinator = ((ScheduleTimeSeriesRepository) getRepositories()
					.getRepositoryFor(ScheduleTimeSeries.class).get())
					.findFirstByScheduleAndInPartyVAndOutPartyVAndCompanyAndDeleted(tpsScheduleCoordinator,
							scheduleTps.getSenderIdentificationV(), tpsScheduleCoordinator.getSenderIdentificationV(),
							secUser.getCompany(), false);
		}

		if (tpsScheduleTimeSeriesCoordinator == null) {

			// Generate new TPS
			tpsScheduleTimeSeriesCoordinator = new ScheduleTimeSeries();

			tpsScheduleTimeSeriesCoordinator.setSendersTimeSeriesIdentification(sendersTimeSeriesIdentification);
			tpsScheduleTimeSeriesCoordinator.setSendersTimeSeriesVersion("1");
			tpsScheduleTimeSeriesCoordinator.setBusinessType("A02");
			tpsScheduleTimeSeriesCoordinator.setProduct(scheduleTimeSeries.getProduct());
			tpsScheduleTimeSeriesCoordinator.setObjectAggregation("A03");
			tpsScheduleTimeSeriesCoordinator.setInAreaV("10YCA-BULGARIA-R");
			tpsScheduleTimeSeriesCoordinator.setInAreaCodingScheme("A01");
			tpsScheduleTimeSeriesCoordinator.setOutAreaV("10YCA-BULGARIA-R");
			tpsScheduleTimeSeriesCoordinator.setOutAreaCodingScheme("A01");

			if (ibexEnergyDealLegList.get(0).getSide().equals("SELL")) {
				tpsScheduleTimeSeriesCoordinator.setInPartyV(tpsScheduleCoordinator.getSenderIdentificationV());
				tpsScheduleTimeSeriesCoordinator.setOutPartyV(scheduleTps.getSenderIdentificationV());
			} else if (ibexEnergyDealLegList.get(0).getSide().equals("BUY")) {
				tpsScheduleTimeSeriesCoordinator.setInPartyV(scheduleTps.getSenderIdentificationV());
				tpsScheduleTimeSeriesCoordinator.setOutPartyV(tpsScheduleCoordinator.getSenderIdentificationV());
			}

			tpsScheduleTimeSeriesCoordinator.setInPartyCodingScheme("A01");
			tpsScheduleTimeSeriesCoordinator.setOutPartyCodingScheme("A01");
			tpsScheduleTimeSeriesCoordinator.setMeasurementUnit(scheduleTimeSeries.getMeasurementUnit());
			tpsScheduleTimeSeriesCoordinator.setTimeInterval(scheduleTimeSeries.getTimeInterval());
			tpsScheduleTimeSeriesCoordinator.setResolution(scheduleTimeSeries.getResolution());
			tpsScheduleTimeSeriesCoordinator.setSchedule(tpsScheduleCoordinator);
			trySave(tpsScheduleTimeSeriesCoordinator, true, false);

		}

		return tpsScheduleTimeSeriesCoordinator;
	}

	private static String extractLocalDate(String input) {

		String outputDateString = "";
		try {

			OffsetDateTime offsetDateTime = OffsetDateTime.parse(input, DateTimeFormatter.ISO_DATE_TIME);
			LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
			LocalDate dateString = localDateTime.toLocalDate();
			DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			// Parse the input string into a LocalDate object
			LocalDate date = LocalDate.parse(String.valueOf(dateString), inputFormatter);

			// Define the output pattern for the date
			DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
			outputDateString = date.format(outputFormatter);
		} catch (Exception e) {
			logger.error("Error at extractLocalDate - " + e.getMessage());
		}
		return outputDateString;
	}

	private Integer getIntervalIndex(String interval) {

		Calendar createTime = Calendar.getInstance();
		int localInterval = 4;
		if (createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).getHour() == createTime.toInstant()
				.atZone(ZoneId.of("Europe/Berlin")).getHour()) {
			localInterval = 0;
		}
		LocalDateTime dateTime = LocalDateTime.parse(interval, DateTimeFormatter.ISO_DATE_TIME);
		LocalTime startOfDay = LocalTime.of(0, 0);
		Duration duration = Duration.between(startOfDay, dateTime);

		int totalIntervals = 96;
		int intervalLengthInMinutes = 24 * 60 / totalIntervals;
		return Math.toIntExact((duration.toMinutes() / intervalLengthInMinutes) + localInterval);
	}

	public List<Map<String, Object>> importXLSXProducedSchedule(DBFile dbFile) {

		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();

		if (dbFile != null) {

			SecUser secUser = getSecUser();
			PowerPlantRepository powerPlantRepository = ((PowerPlantRepository) getRepositories()
					.getRepositoryFor(PowerPlant.class).get());
			List<PowerPlant> powerPlantList = powerPlantRepository.findByCompanyAndDeleted(secUser.getCompany(), false);
			NotificationRepository notificationRepository = ((NotificationRepository) getRepositories()
					.getRepositoryFor(Notification.class).get());
			LoiNotificationTypeRepository loiNotificationTypeRepository = ((LoiNotificationTypeRepository) getRepositories()
					.getRepositoryFor(LoiNotificationType.class).get());
			LoiNotificationType loiNotificationType = loiNotificationTypeRepository
					.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiNotificationType.NOTIFICATION_TYPE_IMPORT,
							secUser.getCompany(), false);
			Set<String> identificationListOfPowerPlant = new HashSet<>();

			if (!powerPlantList.isEmpty()) {
				powerPlantList.forEach(e -> identificationListOfPowerPlant.add(e.getIdentificationNumber()));

				try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dbFile.getContent()))) {
					Sheet sheet = workbook.getSheetAt(0);

					List<String> identifications = new ArrayList<>();
					int counter = 0;
					int counterOfNotSavedIdNum = 0;
					boolean isSaved = false;

					for (Row row : sheet) {

						int counterForIdentification = 0;

						if (counter == 0) {
							for (int i = 2; i < row.getLastCellNum(); i++) {
								identifications.add(String.valueOf(row.getCell(i)));
							}

							for (String identification : identifications) {
								if (!identificationListOfPowerPlant.contains(identification)) {

									Notification notification = new Notification();
									notification.setMessage("powerPlantMissingInTheSystem");
									notification.setMessageDateTime(LocalDateTime.now());
									notification.setIsActive(true);
									notification.setUserName(secUser.getName());
									// notification.setRoleName();
									notification.setIdentificationFirst(identification);
									notification.setIdentificationSecond("producedExcel");
									notification.setLoiNotificationType(loiNotificationType);

									if (trySave(notification, true, false) == null) {
										// throw new ReportException("Error at saving notification");
									}

									counterOfNotSavedIdNum++;
									objectMap.put(identification, counterOfNotSavedIdNum);
									identificationListOfPowerPlant.remove(identification);
								} else {
									Notification notification = notificationRepository
											.findFirstByIsActiveAndIdentificationFirstAndIdentificationSecond(true,
													identification, "producedExcel");

									if (notification != null) {
										notification.setIsActive(false);
										if (trySave(notification, true, false) == null) {
											// throw new ReportException("Error at saving notification");
										}
									}
								}
							}

						} else {

							String rowValue = String.valueOf(row.getCell(0)).trim();

							if (!rowValue.equals("")) {

								LocalDateTime localDateTimeCellValue = row.getCell(0).getLocalDateTimeCellValue();
								long hour = Math.round(row.getCell(1).getNumericCellValue());
								LocalDateTime resultForEndTS = localDateTimeCellValue.plusHours(hour);
								int rowSiZe = row.getLastCellNum();

								for (int i = 2; i < rowSiZe; i++) {

									if (identificationListOfPowerPlant
											.contains(identifications.get(counterForIdentification)) == true) {

										BigDecimal quantity = BigDecimal.valueOf(row.getCell(i).getNumericCellValue());

										PowerPlantProducedSchedule powerPlantProducedSchedule = new PowerPlantProducedSchedule();
										powerPlantProducedSchedule.setStartTS(resultForEndTS.minusHours(1L));
										powerPlantProducedSchedule.setEndTS(resultForEndTS);
										powerPlantProducedSchedule.setQuantityKwh(quantity);
										powerPlantProducedSchedule
												.setIdentification(identifications.get(counterForIdentification));
										if (trySave(powerPlantProducedSchedule, false, false) == null) {
											// throw new ReportException("Error at saving powerPlantProducedSchedule -
											// importXLSXProducedSchedule()");
											trySave(powerPlantProducedSchedule, false, false);
										}
										isSaved = true;
									}
									counterForIdentification++;
								}
							}
						}
						counter++;
					}

					objectMap.put("NotSavedIdNum", "" + counterOfNotSavedIdNum);
					objectMap.put("Processed rows - ", "" + counter);
					if (!isSaved) {
						throw new ReportException("No data saved!");
					}
				} catch (Exception e) {
					throw new ReportException("importXLSXProducedSchedule error message - " + e.getMessage());
				}
			} else {
				throw new ReportException("PowerPlant is missing!");
			}
		} else {
			throw new ReportException("DbFile is missing");
		}

		mapList.add(objectMap);
		return mapList;
	}

	public List<Map<String, Object>> importXLSXMeterReading(DBFile dbFile) {

		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();

		if (dbFile != null) {

			SecUser secUser = getSecUser();
			PowerPlantRepository powerPlantRepository = ((PowerPlantRepository) getRepositories()
					.getRepositoryFor(PowerPlant.class).get());
			List<PowerPlant> powerPlantList = powerPlantRepository.findByCompanyAndDeleted(secUser.getCompany(), false);
			NotificationRepository notificationRepository = ((NotificationRepository) getRepositories()
					.getRepositoryFor(Notification.class).get());
			LoiNotificationTypeRepository loiNotificationTypeRepository = ((LoiNotificationTypeRepository) getRepositories()
					.getRepositoryFor(LoiNotificationType.class).get());
			LoiNotificationType loiNotificationType = loiNotificationTypeRepository
					.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiNotificationType.NOTIFICATION_TYPE_IMPORT,
							secUser.getCompany(), false);
			Set<String> identificationListOfPowerPlant = new HashSet<>();

			if (!powerPlantList.isEmpty()) {
				powerPlantList.forEach(e -> identificationListOfPowerPlant.add(e.getIdentificationNumber()));

				try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dbFile.getContent()))) {
					Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

					List<String> periodList = new ArrayList<>();
					int counter = 0;
					int counterOfNotSavedIdNum = 0;
					boolean isSaved = false;
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

					for (Row row : sheet) {

						int counterForPeriodList = 0;
						if (counter != 0) {

							int rowSiZe = row.getLastCellNum();
							String rowValue = String.valueOf(row.getCell(0)).trim();

							if (!rowValue.equals("")) {

								if (identificationListOfPowerPlant.contains(String.valueOf(row.getCell(0)))) {
									logger.debug("identifications - " + row.getCell(0));
									// Deleting the used identification
									identificationListOfPowerPlant.remove(String.valueOf(row.getCell(0)));

									String identification = String.valueOf(row.getCell(0));
									Notification notification = notificationRepository
											.findFirstByIsActiveAndIdentificationFirstAndIdentificationSecond(true,
													identification, "measuredExcel");

									if (notification != null) {
										notification.setIsActive(false);
										if (trySave(notification, true, false) == null) {
											// throw new ReportException("Error at saving notification");
										}
									}

									for (int i = 3; i < rowSiZe; i++) {

										LocalDateTime endTs = LocalDateTime.parse(periodList.get(counterForPeriodList),
												formatter);
										LocalDateTime startTS = endTs.minusMinutes(15);

										PowerPlantMeterReading powerPlantMeterReading = new PowerPlantMeterReading();
										powerPlantMeterReading.setIdentification(identification);
										powerPlantMeterReading.setStartTS(startTS);
										powerPlantMeterReading.setEndTS(endTs);
										powerPlantMeterReading.setQuantityKwh(row.getCell(i) != null
												? BigDecimal.valueOf(row.getCell(i).getNumericCellValue())
												: BigDecimal.ZERO);
										if (trySave(powerPlantMeterReading, true, false) == null) {
											// throw new ReportException("Error at saving powerPlantMeterReading -
											// importXLSXMeterReading()");
											trySave(powerPlantMeterReading, false, false);
										}
										isSaved = true;
										counterForPeriodList++;
									}
								} else {

									Notification notification = new Notification();
									notification.setMessage("powerPlantMissingInTheSystem");
									notification.setMessageDateTime(LocalDateTime.now());
									notification.setIsActive(true);
									notification.setUserName(secUser.getName());
									// notification.setRoleName();
									notification.setIdentificationFirst(String.valueOf(row.getCell(0)));
									notification.setIdentificationSecond("measuredExcel");
									notification.setLoiNotificationType(loiNotificationType);

									if (trySave(notification, true, false) == null) {
										// throw new ReportException("Error at saving notification");
									}

									counterOfNotSavedIdNum++;
									objectMap.put(String.valueOf(row.getCell(0)), counterOfNotSavedIdNum);
								}
							}
						} else {
							for (int i = 3; i < row.getLastCellNum(); i++) {
								periodList.add(String.valueOf(row.getCell(i)));
							}
						}
						counter++;
					}
					objectMap.put("NotSavedIdNum", "" + counterOfNotSavedIdNum);
					objectMap.put("Processed rows - ", "" + counter);
					if (!isSaved) {
						throw new ReportException("No data saved!");
					}
				} catch (Exception e) {
					throw new ReportException("importXLSMMeterReading error message - " + e.getMessage());
				}
			} else {
				throw new ReportException("PowerPlant is missing!");
			}
		} else {
			throw new ReportException("DbFile is missing");
		}

		mapList.add(objectMap);
		return mapList;
	}

	public List<Map<String, Object>> importIbexPriceDAM(DBFile dbFile) {

		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();

		if (dbFile != null) {

			try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dbFile.getContent()))) {
				Sheet sheet = workbook.getSheetAt(0);

				int counter = 0;
				boolean isSaved = false;

				for (Row row : sheet) {

					String rowValue = String.valueOf(row.getCell(0)).trim();

					if (counter != 0 && !rowValue.equals("")) {

						IbexPrice ibexPrice = new IbexPrice();
						ibexPrice.setLocalDate(row.getCell(0).getDateCellValue().toInstant()
								.atZone(ZoneId.of(EUROPE_SOFIA)).toLocalDate());
						ibexPrice.setHour(BigDecimal.valueOf(row.getCell(1).getNumericCellValue()));
						ibexPrice.setPriceEUR(
								row.getCell(2) != null ? new BigDecimal(row.getCell(2).toString().replace(",", "."))
										: BigDecimal.ZERO);
						ibexPrice.setPriceBGN(
								row.getCell(3) != null ? new BigDecimal(row.getCell(3).toString().replace(",", "."))
										: BigDecimal.ZERO);
						ibexPrice.setVolume(
								row.getCell(4) != null ? new BigDecimal(row.getCell(4).toString().replace(",", "."))
										: BigDecimal.ZERO);

						if (trySave(ibexPrice, false, false) == null) {
							// throw new ReportException("Error at saving ibexPrice -
							// importIbexPriceDAM()");
						}
						isSaved = true;
					}
					counter++;
				}

				objectMap.put("Processed rows - ", "" + counter);
				if (!isSaved) {
					throw new ReportException("No data saved!");
				}

			} catch (Exception e) {
				throw new ReportException("importIbexPriceDAM error message - " + e.getMessage());
			}
		} else {
			throw new ReportException("DbFile is missing");
		}

		mapList.add(objectMap);
		return mapList;
	}

	public List<Map<String, Object>> savePowerPlantProtocols(List<PowerPlantProtocol> powerPlantProtocols) {

		List<Map<String, Object>> mapList = new ArrayList<>();
		Map<String, Object> objectMap = new HashMap<>();
		int counter = 0;

		for (PowerPlantProtocol powerPlantProtocol : powerPlantProtocols) {
			if (trySave(powerPlantProtocol, true, true) == null) {
				throw new ReportException("Error at saving PowerPlantProtocol - savePowerPlantProtocols()");
			} else {
				counter++;
			}
		}

		objectMap.put("Saved powerPlantProtocols - ", counter);
		mapList.add(objectMap);
		return mapList;
	}

	public String getBearerTokenForAuctionApi() throws IOException {

		String clientId = "client_auction_api";
		String clientSecret = "client_auction_api";
		String username = "API_office@finvest.bg";
		String password = "t7g!uqzvCCb8eML";
		String accessToken = "";

		AsyncHttpClient client = new DefaultAsyncHttpClient();

		String credentials = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());

		try {
			Response response = client.prepare("POST", "https://sso.ibex.bg/connect/token")
					.setHeader("accept", "application/json")
					.setHeader("content-type", "application/x-www-form-urlencoded")
					.setHeader("Authorization", "Basic " + credentials)
					.setBody("grant_type=password&scope=auction_api&username=" + username + "&password=" + password)
					.execute()
					.toCompletableFuture()
					.join();

			String responseBody = response.getResponseBody();
		//	logger.debug("responseBody - " + responseBody);
			accessToken = parseAccessToken(responseBody);
			logger.debug("accessToken - " + accessToken);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		client.close();
		return accessToken;
	}

	public List<String> getAuctionId() throws IOException, NullPointerException {

		Calendar createTime = Calendar.getInstance();

		LocalDateTime endZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).toLocalDateTime();
		LocalDateTime startZuluTime = createTime.toInstant().atZone(ZoneId.of(EUROPE_SOFIA)).minusDays(1)
				.toLocalDateTime();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String formattedStartZuluTime = formatter.format(startZuluTime);
		String formattedEndZuluTime = formatter.format(endZuluTime);

		List<String> auctionIdList = new ArrayList<>();

		String url = "https://auctions-api.ibex.bg/api/v1/auctions?closeBiddingFrom=" + formattedStartZuluTime
				+ "&closeBiddingTo=" + formattedEndZuluTime;

		String bearerToken = getBearerTokenForAuctionApi();

		if (!bearerToken.equals("")) {
			CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader("accept", "application/json");
			httpGet.addHeader("Authorization", "Bearer " + bearerToken);
			CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);

			if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {

				String responseJson = EntityUtils.toString(closeableHttpResponse.getEntity());

				try {

					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readTree(responseJson);

					for (JsonNode node : jsonNode) {
						String idValue = node.get("id").asText();

						if (idValue.contains("IBEX_H_DA")) {
							auctionIdList.add(idValue);
						}
					}

				} catch (Exception e) {
					logger.error("Exception from getAuctionId -" + e.getMessage());
				}
			}
		}
		return auctionIdList;
	}

	public List<Map<String, Object>> getAuctionPrices() throws IOException, NullPointerException {

		List<Map<String, Object>> mapList = new ArrayList<>();
		List<String> auctionIdList = getAuctionId();
		int hourCounter = 1;
		int counter = 0;

		for (String auctionId : auctionIdList) {

			String url = "https://auctions-api.ibex.bg/api/v1/auctions/" + auctionId + "/prices";

			String bearerToken = getBearerTokenForAuctionApi();

			if (!bearerToken.equals("")) {
				CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet(url);
				httpGet.addHeader("accept", "application/json");
				httpGet.addHeader("Authorization", "Bearer " + bearerToken);
				CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);

				if (closeableHttpResponse.getStatusLine().getStatusCode() == 200) {

					String responseJson = EntityUtils.toString(closeableHttpResponse.getEntity());

					try {

						ObjectMapper objectMapper = new ObjectMapper();
						JsonNode jsonNode = objectMapper.readTree(responseJson);

						for (JsonNode contractNode : jsonNode.get("contracts")) {

							Instant instant = Instant.parse(contractNode.get("deliveryEnd").asText());
							ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));

							IbexPrice ibexPrice = new IbexPrice();
							ibexPrice.setLocalDate(zonedDateTime.plusHours(1).toLocalDate());

							for (JsonNode areaNode : contractNode.get("areas")) {

								for (JsonNode priceNode : areaNode.get("prices")) {

									if (priceNode.get("currencyCode").asText().equals("BGN")
											&& !priceNode.get("marketPrice").isNull()) {
										counter++;
										ibexPrice.setPriceBGN(
												BigDecimal.valueOf(priceNode.get("marketPrice").asDouble()));
									} else if (priceNode.get("currencyCode").asText().equals("BGN")
											&& priceNode.get("marketPrice").isNull()) {
										ibexPrice.setPriceBGN(BigDecimal.ZERO);
									}

									if (priceNode.get("currencyCode").asText().equals("EUR")
											&& !priceNode.get("marketPrice").isNull()) {
										counter++;
										ibexPrice.setPriceEUR(
												BigDecimal.valueOf(priceNode.get("marketPrice").asDouble()));
									} else {
										ibexPrice.setPriceEUR(BigDecimal.ZERO);
									}
								}

								ibexPrice.setHour(BigDecimal.valueOf(zonedDateTime.plusHours(1).getHour())
										.add(BigDecimal.valueOf(1)));

								if (trySave(ibexPrice, true, false) == null) {
									logger.error("Error at saving ibexPrice - getAuctionPrices");
								}

								hourCounter++;
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
					}

					break;
				} else {
					logger.error(String.valueOf(closeableHttpResponse.getStatusLine()));
				}

				closeableHttpClient.close();
			}
		}
		return mapList;
	}

	public List<Map<String, Object>> sumKwh(String pSDateFrom, String pSDateTo) {

		// List<Object[]> queryResult = entityManager.createNativeQuery("select
		// pp.identification_number identification_number, prod.startts at time zone
		// 'UTC' at time zone 'EET' as startts, prod.endts at time zone 'UTC' at time
		// zone 'EET' as endts, prod.quantity_kwh prod_quantity_kwh," +
		// " sum(met.quantity_kwh) met_quantity_kwh, pr.pricebgn pricebgn" +
		// " from power_plant pp" +
		// " left join power_plant_meter_reading met on met.deleted = false" +
		// " and met.identification = pp.identification_number and met.company_id =
		// pp.company_id" +
		// " left join power_plant_produced_schedule prod on prod.deleted = false" +
		// " and prod.identification = pp.identification_number and date_trunc('hour',
		// met.startts) = date_trunc('hour', prod.startts) and prod.company_id =
		// pp.company_id" +
		// " left join ibex_price pr on pr.deleted = false" +
		// " and pr.local_date + make_interval(0,0,0,0,cast(pr.hour as integer)) =
		// prod.endts and pr.company_id = pp.company_id" +
		// " where pp.deleted = false and met.endts > to_date(:pSDateFrom,'yyyy-mm-dd')
		// and met.endts <= to_date(:pSDateTo,'yyyy-mm-dd') " +
		// " and prod.endts > to_date(:pSDateFrom,'yyyy-mm-dd') and prod.endts <=
		// to_date(:pSDateTo,'yyyy-mm-dd') group by pp.identification_number,
		// prod.startts, prod.endts, prod.quantity_kwh, pr.pricebgn;")
		// .setParameter("pSDateFrom", pSDateFrom)
		// .setParameter("pSDateTo", pSDateTo)
		// .getResultList();

		List<Object[]> queryResult = entityManager.createNativeQuery(
				"select pp.identification_number identification_number, prod.startts startts, prod.endts endts, prod.quantity_kwh prod_quantity_kwh,"
						+
						" sum(met.quantity_kwh) met_quantity_kwh, pr.pricebgn pricebgn" +
						" from power_plant pp" +
						" left join power_plant_meter_reading met on met.deleted = false" +
						" and met.identification = pp.identification_number and met.company_id = pp.company_id" +
						" left join power_plant_produced_schedule prod on prod.deleted = false" +
						" and prod.identification = pp.identification_number and date_trunc('hour',met.startts) = date_trunc('hour',prod.startts) and prod.company_id = pp.company_id"
						+
						" left join ibex_price pr on pr.deleted = false" +
						" and pr.local_date + make_interval(0,0,0,0,cast(pr.hour as integer)) = prod.endts and pr.company_id = pp.company_id"
						+
						" where pp.deleted = false and met.endts > to_date(:pSDateFrom,'yyyy-mm-dd') and met.endts <= to_date(:pSDateTo,'yyyy-mm-dd') "
						+
						" and prod.endts > to_date(:pSDateFrom,'yyyy-mm-dd') and prod.endts <= to_date(:pSDateTo,'yyyy-mm-dd') group by pp.identification_number, prod.startts, prod.endts, prod.quantity_kwh, pr.pricebgn;")
				.setParameter("pSDateFrom", pSDateFrom)
				.setParameter("pSDateTo", pSDateTo)
				.getResultList();

		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();
		for (Object[] queryRow : queryResult) {

			Map<String, Object> resultRow = new HashMap<String, Object>();
			resultRow.put("identification_number", queryRow[0]);

			// resultRow.put("startts", queryRow[1].toString());
			// resultRow.put("endts", queryRow[2].toString());
			resultRow.put("startts", convertToUTC(queryRow[1].toString(), "UTC"));
			resultRow.put("endts", convertToUTC(queryRow[2].toString(), "UTC"));
			resultRow.put("prod_quantity_kwh", queryRow[3]);
			resultRow.put("met_quantity_kwh", queryRow[4]);
			resultRow.put("pricebgn", queryRow[5]);
			result.add(resultRow);
		}

		return result;
	}

	private String convertToUTC(String dateString, String inputTimeZone) {

		// Define the formatter for the input string
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

		try {
			// Parse the string to LocalDateTime
			LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);

			// Convert LocalDateTime to ZonedDateTime with the specified input timezone
			ZonedDateTime localZonedDateTime = localDateTime.atZone(ZoneId.of(inputTimeZone));

			// Convert ZonedDateTime to UTC
			ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

			// Format the UTC ZonedDateTime to a string
			return utcZonedDateTime.format(outputFormatter);
		} catch (Exception e) {
			// Log the exception (using your preferred logging framework)
			logger.error("Error parsing date: " + e.getMessage());
			return "";
		}
	}

	public List<IbexEnergyDeal> getUndistributedDeals() {

		SecUser secUser = getSecUser();
		List<IbexEnergyDeal> ibexEnergyDeals = ((IbexEnergyDealRepository) getRepositories()
				.getRepositoryFor(IbexEnergyDeal.class).get())
				.findFirst200ByIsDistributedToSchedulesAndCompanyAndDeletedOrderByCreatedDateDesc(false, secUser.getCompany(), false);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -1);
		Date yesterday = calendar.getTime();
		ibexEnergyDeals = ibexEnergyDeals.stream()
				.filter(ibexEnergyDeal -> (ibexEnergyDeal.getLegs().get(0).getPortfolioId()
						.equals(PORTFOLIO_FOR_APRILCI) && ibexEnergyDeal.getCreatedDate().after(yesterday)))
				.collect(Collectors.toList());

		return ibexEnergyDeals;
	}

}
