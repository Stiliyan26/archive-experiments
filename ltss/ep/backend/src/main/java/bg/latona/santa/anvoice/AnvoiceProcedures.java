package bg.latona.santa.anvoice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.Repositories;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.WebApplicationContext;

import bg.latona.santa.DroolsRuleException;
import bg.latona.santa.RepositoryConfiguration;
import bg.latona.santa.anvoice.entities.ImportXenergieInvoice;
import bg.latona.santa.anvoice.repositories.ImportXenergieInvoiceRepository;
import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.UserActionEvent;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.DocumentRange;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.reports.ServiceUnavailableException;
import bg.latona.santa.repositories.CommonRepository;
import bg.latona.santa.repositories.DBFileRepository;
import bg.latona.santa.repositories.ElectricityInvoiceRepository;
import bg.latona.santa.repositories.LegalPersonRepository;
import bg.latona.santa.repositories.LoiDocumentTypeRepository;
import bg.latona.santa.repositories.SecUserRepository;
import bg.latona.santa.security.AppUserDetailsService;
import bg.latona.santa.selfie.util.DateFormatUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.*;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.*;
import static bg.latona.santa.selfie.util.CurrencyFormatUtils.convertNumbersToBgnCurrency;
import static bg.latona.santa.selfie.util.ElectricityInvoiceUtils.formatPopulateElectricityInvoiceErrorMessageKey;
import static bg.latona.santa.selfie.util.HtmlUtils.fillHtmlTemplate;
import static bg.latona.santa.selfie.util.PdfUtils.convertHtmlFileToPdfByteArray;

@Repository
@Transactional
public class AnvoiceProcedures {

	private static final Logger logger = LoggerFactory.getLogger(AnvoiceProcedures.class);

    public static final String ERROR_CREATING_PDF_FILE = "ErrorGeneratingTheFile";
	
	@Autowired
	Environment env;
	@Autowired
	private WebApplicationContext appContext;
	private Repositories repositories = null;
	@Autowired
	private SessionFactory sessionFactory;

	Repositories getRepositories() {
		if (repositories == null) {
			repositories = new Repositories(appContext);
		}
		return repositories;
	}

	public SecUser getSecUser() {
		String currentUser = null;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			currentUser = authentication.getPrincipal().toString();
		} else {
			//make Camel load the security context
			currentUser = "camel1";
			UserDetailsService userDetailsService = appContext.getBean(AppUserDetailsService.class);
			UserDetails userDetails = userDetailsService.loadUserByUsername(currentUser);
			authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		SecUserRepository secUserRepository = ((SecUserRepository) getRepositories().getRepositoryFor(SecUser.class)
				.get());
		//entityManager.setFlushMode(FlushModeType.COMMIT); //avoid flushing on next-row query, which slows down this method
		SecUser user = secUserRepository.findFirstByName(currentUser);
		return user;
	}

	public void validateBeforeSave(CommonRecord entity) {
		if (entity.getId() == null) {
			RepositoryConfiguration.getBeforeCreateValidator().validate(entity, null);
		} else {
			RepositoryConfiguration.getBeforeSaveValidator().validate(entity, null);
		}
	}

	public void validateAfterSave(CommonRecord entity) {
		if (entity.getId() == null) {
			RepositoryConfiguration.getAfterCreateValidator().validate(entity, null);
		} else {
			RepositoryConfiguration.getAfterSaveValidator().validate(entity, null);
		}
	}

	public CommonRecord trySave(CommonRecord entity, boolean doReturnNullAtErrors, boolean doFlush) {
		logger.trace("AnvoiceController.trySave " + entity);
		try {
			CommonRecord result;
			validateBeforeSave(entity);
			if (doFlush) {
				result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get())
						.saveAndFlush(entity);
			} else {
				result = (CommonRecord) ((CommonRepository) getRepositories().getRepositoryFor(entity.getClass()).get())
						.save(entity);
			}
			validateAfterSave(entity);
			return result;
		} catch (DroolsRuleException e) {
			if (!doReturnNullAtErrors && !e.getResult().getFieldErrors().isEmpty()
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

	public List<Map<String, Object>> getXenergieInvoices() {

		List<Map<String, Object>> result = new ArrayList<>();

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;

		try {
			// Establish the connection
			connection = DriverManager.getConnection(
					env.getProperty("xenergie.datasource.jdbc-url"),
					env.getProperty("xenergie.datasource.username"),
					env.getProperty("xenergie.datasource.password"));

			// Define the SQL query
			String sql = "SELECT BILLING_DATE,CONTRACT,Z_NUMBER,CUSTOMER_EIK,CUSTOMER_NAME,ADDRESS,VAT_NUMBER,QUANTITY,PRICE,"
				+"TOTAL_COST,TOTAL_COST_INCL_VAT,VAT,REASON_NO_VAT,EMAIL,PRODUCT,SELF_BILLING,BILLING_MONTH,BANK_CONNECTION,"
				+"DOCUMENT_TYPE_ID,DEAL_ID,REASON_CORRECTION_DOC,EPRS_EPRES,EPRS_EPRES_EIK,EPRS_EPRES_ADDRESS FROM EPRO.V_SELF_BILLING";

			// Create the PreparedStatement
			preparedStatement = connection.prepareStatement(sql);

			// Execute the query
			resultSet = preparedStatement.executeQuery();

			// Process the result set
			while (resultSet.next()) {
				logger.trace(
					resultSet.getDate("BILLING_DATE")+";"+resultSet.getString("CONTRACT")+";"+resultSet.getString("Z_NUMBER")+";"+resultSet.getString("CUSTOMER_EIK")+";"+resultSet.getString("CUSTOMER_NAME")+";"+resultSet.getString("ADDRESS")+";"+resultSet.getString("VAT_NUMBER")+";"+resultSet.getBigDecimal("QUANTITY")+";"+resultSet.getBigDecimal("PRICE")+";"+resultSet.getBigDecimal("TOTAL_COST")+";"+resultSet.getBigDecimal("TOTAL_COST_INCL_VAT")+";"+resultSet.getBigDecimal("VAT")+";"+resultSet.getString("REASON_NO_VAT")+";"+resultSet.getString("EMAIL")+";"+resultSet.getString("PRODUCT"));

				ImportXenergieInvoice xeInvoice = new ImportXenergieInvoice(
					null, null, null, null, false, getSecUser().getCompany(),
					resultSet.getDate("BILLING_DATE").toLocalDate(),
					resultSet.getDate("BILLING_MONTH").toLocalDate(),
					resultSet.getString("CONTRACT"),
					resultSet.getString("Z_NUMBER"),
					resultSet.getString("CUSTOMER_EIK"),
					resultSet.getString("CUSTOMER_NAME"),
					resultSet.getString("ADDRESS"),
					resultSet.getString("VAT_NUMBER"),
					resultSet.getBigDecimal("QUANTITY"),
					resultSet.getBigDecimal("PRICE"),
					resultSet.getBigDecimal("TOTAL_COST"),
					resultSet.getBigDecimal("TOTAL_COST_INCL_VAT"),
					resultSet.getBigDecimal("VAT"),
					resultSet.getString("REASON_NO_VAT"),
					resultSet.getString("EMAIL"),
					resultSet.getString("PRODUCT"),
					resultSet.getString("SELF_BILLING"),
					resultSet.getString("BANK_CONNECTION"),
					resultSet.getLong("DOCUMENT_TYPE_ID"),
					resultSet.getString("DEAL_ID"),
					resultSet.getString("REASON_CORRECTION_DOC"),
					resultSet.getString("EPRS_EPRES"),
					resultSet.getBigDecimal("EPRS_EPRES_EIK"),
					resultSet.getString("EPRS_EPRES_ADDRESS"),
					true,
					null,
					null
				);
				trySave(xeInvoice, false, false);
			}
		} catch (SQLException e) {
			throw new ServiceUnavailableException(e.getMessage());
		} finally {
			// Close the resources
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				throw new ServiceUnavailableException(e.getMessage());
			}
		}

		return result;
	}

	public LocalDate getLocalDateFromString(String date) {
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
			.appendOptional(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
			.appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
			.appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
			.appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
			.toFormatter();
		return LocalDate.parse(date, formatter);
	}

	public LocalDate getCellAsLocalDate(Row row, int colNum) {
		Cell cell = row.getCell(colNum);
		if(cell == null) return null;
		if(cell.getCellType().equals(CellType.STRING)) {
			return getLocalDateFromString(cell.toString());
		}
		Date date = cell.getDateCellValue();
		if(date == null) return null;
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public BigDecimal getCellAsBigDecimal(Row row, int colNum) {
		Cell cell = row.getCell(colNum);
		if(cell == null) return null;
		if(cell.getCellType().equals(CellType.NUMERIC)) {
			return new BigDecimal(cell.getNumericCellValue());
		}
		return new BigDecimal(cell.toString());
	}

	public Long getCellAsLong(Row row, int colNum) {
		Cell cell = row.getCell(colNum);
		if(cell == null) return null;
		BigDecimal num = null;
		if(cell.getCellType().equals(CellType.NUMERIC)) {
			num = new BigDecimal(cell.getNumericCellValue());
		} else {
			num = new BigDecimal(cell.toString());
		}
		return num.longValue();
	}
    
    public Map<Integer, List<String>> parseXenergieInvoicesFile(Long dbFileId) {
        DBFile dbFile = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get()).findFirstById(dbFileId);

        if (dbFile == null) // if the File is null
            throw new ReportException(DB_FILE_MISSING_MESSAGE);

        Map<Integer, List<String>> invalidFileRowEntries = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dbFile.getContent()))) {
            Sheet sheet = workbook.getSheetAt(0);
			int rowNum = 0;
        	for (Row row : sheet) {
				try {
					rowNum++;
					if(rowNum == 1 || "BILLING_DATE".equals(row.getCell(0).toString())) 
						continue;

					ImportXenergieInvoice xeInvoice = new ImportXenergieInvoice(
						null, null, null, null, false, getSecUser().getCompany(),
						getCellAsLocalDate(row,0),
						getCellAsLocalDate(row,1),
						row.getCell(2,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(3,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(4,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(5,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(6,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(7,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						getCellAsBigDecimal(row,8),
						getCellAsBigDecimal(row,9),
						getCellAsBigDecimal(row,10),
						getCellAsBigDecimal(row,11),
						getCellAsBigDecimal(row,12),
						row.getCell(13,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(14,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(15,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(16,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(17,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						getCellAsLong(row,18),
						row.getCell(19,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(20,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						row.getCell(21,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						getCellAsBigDecimal(row,22),
						row.getCell(23,MissingCellPolicy.CREATE_NULL_AS_BLANK).toString(),
						true,
						null,
						null
					);
					trySave(xeInvoice, false, false);
				} catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					throw new ReportException(e.getMessage());
				}
			}
        } catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new ReportException(e.getMessage());
        }

        return invalidFileRowEntries; // If all rows are with valid data: an empty Map will be returned
    }

    public void populateElectricityInvoice(LocalDate periodFrom, LocalDate periodTo, LocalDate taxEventDate, Long documentType) {
        
        ManagedCompany company = getSecUser().getCompany();

		LoiDocumentTypeRepository loiDocumentTypeRepository = ((LoiDocumentTypeRepository) getRepositories().getRepositoryFor(LoiDocumentType.class).get());
		// Prefetching the LoiDocumentTypes
		LoiDocumentType loiDocumentTypeInvoice = loiDocumentTypeRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.INVOICE, company, false);
		LoiDocumentType loiDocumentTypeDebitNote = loiDocumentTypeRepository
				.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.DEBIT_NOTE, company, false);
		LoiDocumentType loiDocumentTypeCreditNote = loiDocumentTypeRepository
				.findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.CREDIT_NOTE, company, false);

        List<ImportXenergieInvoice> importXenergieInvoices = ((ImportXenergieInvoiceRepository) getRepositories().getRepositoryFor(ImportXenergieInvoice.class).get())
                .findAllByElectricityInvoiceIsNullAndIsValidIsTrueAndBillingDateBetweenAndDocumentTypeIdAndCompanyAndDeleted(
					periodFrom, 
					periodTo, 
					documentType,
					company,
					false
				);
		logger.info("Found import rows: "+String.valueOf(importXenergieInvoices.size()));
		
		ElectricityInvoiceRepository electricityInvoiceRepository = ((ElectricityInvoiceRepository) getRepositories().getRepositoryFor(ElectricityInvoice.class).get());

        for(ImportXenergieInvoice importXenergieInvoice: importXenergieInvoices) {
			createElectricityInvoiceFromImport(importXenergieInvoice,
				loiDocumentTypeInvoice,loiDocumentTypeDebitNote,loiDocumentTypeCreditNote,
				electricityInvoiceRepository, company, taxEventDate);
		}

        //importFilesService.fetchAndPopulateImportQuantityEntries(periodFrom, periodTo, taxEventDate, loiDocumentType, loiDocumentTypeMap);

        //importFilesService.fetchAndPopulateImportValueAndQuantityEntries(periodFrom, periodTo, taxEventDate, loiDocumentType, loiDocumentTypeMap);

    }

	public void createElectricityInvoiceFromImport(ImportXenergieInvoice importXenergieInvoice,
			LoiDocumentType loiDocumentTypeInvoice, LoiDocumentType loiDocumentTypeDebitNote, LoiDocumentType loiDocumentTypeCreditNote,
			ElectricityInvoiceRepository electricityInvoiceRepository, ManagedCompany company,
			LocalDate taxEventDate) {
		ElectricityInvoice electricityInvoice;

		LoiDocumentType importXenergieInvoiceLoiDocumentType = importXenergieInvoice.getDocumentTypeId() == 1L ? loiDocumentTypeInvoice : (importXenergieInvoice.getDocumentTypeId() == 2L ? loiDocumentTypeCreditNote : loiDocumentTypeDebitNote);

		ElectricityInvoice previousElectricityInvoice = null;
		if ( // in case of loiDocumentType == debit note || credit note -> check for an existing invoice
			importXenergieInvoice.getDocumentTypeId() != 1L
		) {
			previousElectricityInvoice = electricityInvoiceRepository
				.findFirstByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndHasDbFileIsTrueAndDeletedAndCompany(
					importXenergieInvoice.getZnumber(),
					importXenergieInvoice.getBillingDate().with(TemporalAdjusters.firstDayOfMonth()),
					importXenergieInvoice.getBillingDate().with(TemporalAdjusters.lastDayOfMonth()),
					loiDocumentTypeInvoice,
					false,
					company
				).get();
	
			if (previousElectricityInvoice != null) {
				logger.error(
					"No such previous invoice with generated PDF invoice: Reporting Point: " + importXenergieInvoice.getZnumber() +
					", Period From: " + importXenergieInvoice.getBillingDate().with(TemporalAdjusters.firstDayOfMonth()) +
					", Period To: " + importXenergieInvoice.getBillingDate().with(TemporalAdjusters.lastDayOfMonth())
				);
				return;
			}
		}

		Optional<ElectricityInvoice> existingElectricityInvoice = electricityInvoiceRepository
			.findByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
				importXenergieInvoice.getZnumber(),
				importXenergieInvoice.getBillingDate().with(TemporalAdjusters.firstDayOfMonth()),
				importXenergieInvoice.getBillingDate().with(TemporalAdjusters.lastDayOfMonth()),
				importXenergieInvoiceLoiDocumentType,
				false,
				company
			);

		if (existingElectricityInvoice.isPresent()) { // Check for a previous Entity
			existingElectricityInvoice.get().setIsValid(true);
			// Save the changes
			electricityInvoice = existingElectricityInvoice.get();
		} else {
			// Create the new Entity
			electricityInvoice = ElectricityInvoice
					.builder()
					.reportingPointOwn(importXenergieInvoice.getZnumber())
					.periodFrom(importXenergieInvoice.getBillingDate().with(TemporalAdjusters.firstDayOfMonth()))
					.periodTo(importXenergieInvoice.getBillingDate().with(TemporalAdjusters.lastDayOfMonth()))
					.isValid(true)
					.build();
		}
		// Set the new Values
		electricityInvoice.setTaxEventDate(taxEventDate);
		electricityInvoice.setLoiDocumentType(importXenergieInvoiceLoiDocumentType);
		//electricityInvoice.setAgreementType(importXenergieInvoice.getAgreementType());
		electricityInvoice.setPriceInLevs(importXenergieInvoice.getPrice());
		electricityInvoice.setTotalSumInLevs(importXenergieInvoice.getTotalCostInclVat());
		electricityInvoice.setVatInLevs(importXenergieInvoice.getVat());
	
		// set relationship with the invoice
		electricityInvoice.setElectricityInvoice(previousElectricityInvoice);

		// Save the new Entity
		electricityInvoice = (ElectricityInvoice) trySave(electricityInvoice, true, true);

		if (electricityInvoice == null)
			return;

		// Set the relationship
		importXenergieInvoice.setElectricityInvoice(electricityInvoice);
		trySave(importXenergieInvoice, false, true);
	}

    public List<ElectricityInvoice> getElectricityInvoicesToGenerate() {
		ManagedCompany company = getSecUser().getCompany();

		if(company == null) logger.info("Company is null");

		ElectricityInvoiceRepository electricityInvoiceRepository = ((ElectricityInvoiceRepository) getRepositories().getRepositoryFor(ElectricityInvoice.class).get());
        List<ElectricityInvoice> electricityInvoices = electricityInvoiceRepository
                .findAllByIsValidIsTrueAndHasDbFileFalseAndCompanyAndDeleted(company, false); // Fetch all Electricity invoices for the process
        return electricityInvoices;
    }
	
	public String getLegalPersonIdentifier(LegalPerson legalPerson) {
        if (legalPerson == null)
            return null;

        return legalPerson.getEik() != null
                ? legalPerson.getEik()
                : (legalPerson.getEgn() != null ? legalPerson.getEgn() : null);
    }


    public String generateDocumentNumber(
            Optional<PowerPlant> optionalPowerPlant, ElectricityInvoice electricityInvoice, List<Map<String, Object>> invalidElectricityInvoiceRows
    ) {
        if (!optionalPowerPlant.isPresent()) { // If there is no such Power plant with this Access point
			Map<String, Object> error = new HashMap<>();
            error.put("id", electricityInvoice.getId());
			error.put("class", electricityInvoice.getClass().getSimpleName());
            error.put("field", "powerPlant");
            error.put("value", null);
            error.put("error", MISSING_POWER_PLANT);

            invalidElectricityInvoiceRows.add(error);
            return null;
        }

        // Fetch the Document type
        Optional<DocumentRange> optionalDocumentRange = optionalPowerPlant
                .get()
                .getOwner()
                .getDocumentRanges()
                .stream()
                .filter(DocumentRange::getIsActive) // Filter only the active ranges
                .sorted(Comparator.comparing(DocumentRange::getCreatedDate)) // Order by date ASC
                .findFirst(); // Get the first element

        if (!optionalDocumentRange.isPresent()) { // If there isn't an available document number
			Map<String, Object> error = new HashMap<>();
            error.put("id", optionalPowerPlant.get().getOwner().getId());
			error.put("class", LegalPerson.class.getSimpleName());
            error.put("field", "documentRanges");
            error.put("value", null);
            error.put("error", MISSING_VALID_DOCUMENT_RANGE);
			
            invalidElectricityInvoiceRows.add(error);
            return null;
        }

        optionalDocumentRange.get().setCurrent(optionalDocumentRange.get().getCurrent() + 1); // Set the number for the current document

        DocumentRange documentRange = (DocumentRange) trySave(optionalDocumentRange.get(), true, true);

        return documentRange.getCurrent().toString();
    }

    public List<Map<String, Object>> validateAndIssueElectricityInvoice(ElectricityInvoice electricityInvoice) {

        List<Map<String, Object>> invalidElectricityInvoiceRows = new LinkedList<>();

		//session needed for when this is called from Camel
		try (Session session = sessionFactory.openSession()) {
			electricityInvoice = (ElectricityInvoice) session.merge(electricityInvoice);

			String invoiceDocumentNumber = generateDocumentNumber(Optional.ofNullable(electricityInvoice.getPowerPlant()), electricityInvoice, invalidElectricityInvoiceRows);

			if (invoiceDocumentNumber == null) // Power plant or Document range missing or unavailable
				return invalidElectricityInvoiceRows;

			electricityInvoice.setInvoiceNumber(invoiceDocumentNumber);

			LegalPerson legalPerson = electricityInvoice.getPowerPlant().getOwner();

			if (legalPerson == null) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", electricityInvoice.getPowerPlant().getId());
				error.put("class", PowerPlant.class.getSimpleName());
				error.put("field", "owner");
				error.put("value", null);
				error.put("error", MISSING_LEGAL_PERSON);
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}

			String legalPersonIdentifier = getLegalPersonIdentifier(legalPerson);

			if (legalPersonIdentifier == null) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", legalPerson.getId());
				error.put("class", LegalPerson.class.getSimpleName());
				error.put("field", "[eik,egn]");
				error.put("value", null);
				error.put("error", MISSING_LEGAL_PERSON_IDENTIFIER);
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}

			String powerPlantIdentifier = electricityInvoice.getPowerPlant().getIdentificationNumber() != null
					? electricityInvoice.getPowerPlant().getIdentificationNumber()
					: (electricityInvoice.getPowerPlant().getProducerEic() != null ? electricityInvoice.getPowerPlant().getProducerEic() : null);

			if (powerPlantIdentifier == null) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", electricityInvoice.getPowerPlant().getId());
				error.put("class", PowerPlant.class.getSimpleName());
				error.put("field", "[identificationNumber,producerEic]");
				error.put("value", null);
				error.put("error", MISSING_POWER_PLANT_IDENTIFIER);
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}

			// Save the entity
			trySave(electricityInvoice, true, true);
		}

        return invalidElectricityInvoiceRows;
    }

    public String[] fillHtmlTemplateData(List<Map<String, Object>> invalidElectricityInvoiceRows, ElectricityInvoice electricityInvoice, PowerPlant powerPlant, LegalPerson recipient) {
        List<AgreementSelfInvoicing> agreements = powerPlant.getAgreementsSelfInvoicing();

        if( agreements.size() < 1 ) {
			Map<String, Object> error = new HashMap<>();
			error.put("id", powerPlant.getId());
			error.put("class", PowerPlant.class.getSimpleName());
			error.put("field", "agreementsSelfInvoicing");
			error.put("value", null);
			error.put("error", "MissingAgreement");
			
			invalidElectricityInvoiceRows.add(error);
			return null;
        }
        AgreementSelfInvoicing agreementSelfInvoicing = agreements
                .get(powerPlant.getAgreementsSelfInvoicing().size() - 1);

        boolean isVatIncluded = (agreementSelfInvoicing != null && agreementSelfInvoicing.getIsVatIncluded() != null) ? agreementSelfInvoicing.getIsVatIncluded() : false;

        String recipientName = (recipient != null && !recipient.getContacts().isEmpty() &&
                !recipient.getContacts().get(recipient.getContacts().size() - 1).getName().isEmpty() ?
                recipient.getContacts().get(recipient.getContacts().size() - 1).getName() : EMPTY_HTML_VALUE_PLACEHOLDER);

        String eikRecipient = (recipient != null && recipient.getEik() != null) ? recipient.getEik() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String vatNumberRecipient = (recipient != null && recipient.getVatNumber() != null) ? recipient.getVatNumber() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String documentTypeName = LOI_DOCUMENT_TYPES_BG_NAMES_MAP.get(electricityInvoice.getLoiDocumentType().getListOptionItemCode());

        String currentDateName = LocalDate.now().toString();

        LegalPerson owner = powerPlant.getOwner();

        String ownerIban = (owner != null && !owner.getBankAccounts().isEmpty()) ?
                owner.getBankAccounts().get(owner.getBankAccounts().size() - 1).getIban() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String eikOrEgn = owner != null ? getLegalPersonIdentifier(owner) : EMPTY_HTML_VALUE_PLACEHOLDER;

        String eikWithVat = "";
        if (isVatIncluded) {
            eikWithVat = (owner != null && owner.getVatNumber() != null) ? owner.getVatNumber() : EMPTY_HTML_VALUE_PLACEHOLDER;
        }

        String legalPersonName = (owner != null && owner.getName() != null) ? owner.getName() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String legalPersonAddress = (owner != null && owner.getAddress() != null) ? owner.getAddress() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String measurementUnitName = LOI_MEASUREMENT_UNITS_BG_NAMES_MAP.get(electricityInvoice.getLoiMeasurementUnit().getListOptionItemCode());

        String priceInLevsName = electricityInvoice.getPriceInLevs().toString();

        String priceInEurosName = electricityInvoice.getPriceInEuros().toString();

        String vatAmountInLevsName = electricityInvoice.getVatInLevs().toString();

        String vatAmountInEurosName = electricityInvoice.getVatInEuros().toString();

        String totalAmountInLevsWithVatName = electricityInvoice.getTotalSumInLevs().add(electricityInvoice.getVatInLevs()).toString();

        String totalAmountInEurosWithVatName = electricityInvoice.getTotalSumInEuros().add(electricityInvoice.getVatInEuros()).toString();

        String paymentAmountInLevsName = convertNumbersToBgnCurrency(electricityInvoice.getTotalSumInLevs().intValue());

        String paymentAmountInEurosName = convertNumbersToBgnCurrency(electricityInvoice.getTotalSumInEuros().intValue());

        String relationshipToInvoice =
                (electricityInvoice.getLoiDocumentType().getListOptionItemCode() == 2 ||
                        electricityInvoice.getLoiDocumentType().getListOptionItemCode() == 3) ? // Credit or Debit note
                        String.format(
                                "Към Фактура № %s / Дата: %s <br>",
                                electricityInvoice.getElectricityInvoice().getInvoiceNumber(),
                                electricityInvoice.getTaxEventDate())
                        : "";


        return new String[]{
                // ----------------- Оригинал -----------------
                String.format("%s - САМОФАКТУРИРАНЕ", documentTypeName)

                // Доставчик
                , documentTypeName
                , electricityInvoice.getInvoiceNumber() // Фактура Номер
                , currentDateName // Current Date
                , electricityInvoice.getTaxEventDate().toString() // Tax Event Date
                , relationshipToInvoice
                , legalPersonName // Legal Person Name
                , legalPersonAddress // Legal Person Address
                , eikOrEgn // Идентификационен №
                , isVatIncluded ? String.format("Идентификационен № по ДДС: %s<br>", eikWithVat) : ""// Идентификационен № по ДДС // TODO: if it has it дали контрагента е регистриран по ДДС
                , ownerIban // Банкова сметка

                // Получател
                , eikRecipient // Идентификационен Номер
                , vatNumberRecipient // Идентификационен № по ДДС

                // Таблица
                , powerPlant.getName()
                , electricityInvoice.getPeriodFrom().toString() // Период от
                , electricityInvoice.getPeriodTo().toString() // Период до
                , measurementUnitName // Loi Measurement Unit
                , electricityInvoice.getTotalQuantity().toString() // Quantity
                , priceInEurosName // Price in Euros
                , priceInLevsName // Price in Levs
                , electricityInvoice.getTotalSumInEuros().toString() // Total Sum in Euros
                , electricityInvoice.getTotalSumInLevs().toString() // Total Sum in Levs

                // Totals Table
                , electricityInvoice.getTotalSumInLevs().toString() // Данъчна основа в лева
                , electricityInvoice.getTotalSumInEuros().toString() // Данъчна основа в евро
                , isVatIncluded ? vatAmountInLevsName : "0" // ДДС в лева
                , isVatIncluded ? vatAmountInEurosName : "0" // ДДС в евро
                , isVatIncluded ? totalAmountInLevsWithVatName : electricityInvoice.getTotalSumInLevs().toString() // Обща стойност в лева
                , isVatIncluded ? totalAmountInEurosWithVatName : electricityInvoice.getTotalSumInEuros().toString() // Обща стойност в евро

                // Billed Sums
                , paymentAmountInLevsName // Сума за плащане в лева
                , paymentAmountInEurosName // Сума за плащане в евро
                , String.format("(%s)", recipientName) // Recipient Contact Name

                // ----------------- Копие -----------------
                , String.format("%s - САМОФАКТУРИРАНЕ", documentTypeName)

                // Доставчик
                , documentTypeName
                , electricityInvoice.getInvoiceNumber() // Фактура Номер
                , currentDateName // Current Date
                , electricityInvoice.getTaxEventDate().toString() // Tax Event Date
                , relationshipToInvoice
                , legalPersonName // Legal Person Name
                , legalPersonAddress // Legal Person Address
                , eikOrEgn // Идентификационен №
                , isVatIncluded ? String.format("Идентификационен № по ДДС: %s<br>", eikWithVat) : "" // Идентификационен № по ДДС - Legal Person // TODO: if it has it дали контрагента е регистриран по ДДС
                , ownerIban // Банкова сметка

                // Получател
                , eikRecipient // Идентификационен Номер
                , vatNumberRecipient // Идентификационен № по ДДС

                // Таблица
                , powerPlant.getName()
                , electricityInvoice.getPeriodFrom().toString() // Период от
                , electricityInvoice.getPeriodTo().toString() // Период до
                , measurementUnitName // Loi Measurement Unit
                , electricityInvoice.getTotalQuantity().toString() // Quantity
                , priceInEurosName // Price in Euros
                , priceInLevsName // Price in Levs
                , electricityInvoice.getTotalSumInEuros().toString() // Total Sum in Euros
                , electricityInvoice.getTotalSumInLevs().toString() // Total Sum in Levs

                // Totals Table
                , electricityInvoice.getTotalSumInLevs().toString() // Данъчна основа в лева
                , electricityInvoice.getTotalSumInEuros().toString() // Данъчна основа в евро
                , isVatIncluded ? vatAmountInLevsName : "0" // ДДС в лева
                , isVatIncluded ? vatAmountInEurosName : "0" // ДДС в евро
                , isVatIncluded ? totalAmountInLevsWithVatName : electricityInvoice.getTotalSumInLevs().toString() // Обща стойност в лева
                , isVatIncluded ? totalAmountInEurosWithVatName : electricityInvoice.getTotalSumInEuros().toString() // Обща стойност в евро

                // Billed Sums
                , paymentAmountInLevsName // Сума за плащане в лева
                , paymentAmountInEurosName // Сума за плащане в евро
                , String.format("(%s)", recipientName) // Recipient Contact Name
        };
    }
	
    public DBFile generatePdfFile(ElectricityInvoice electricityInvoice, String date, String legalPersonIdentifier, 
			String powerPlantIdentifier, String[] data, List<Map<String, Object>> invalidElectricityInvoiceRows) {

        try {
            // Fill the HTML template with data, creating a new HTML file
            InputStream filledHtmlInputStream = fillHtmlTemplate("src\\main\\resources\\templates\\invoice_template.html", data);

            // Convert the filled HTML template file to PDF file
            byte[] pdfFileContent = convertHtmlFileToPdfByteArray(filledHtmlInputStream);
            DBFile dbFile = new DBFile();
            dbFile.setName(String.format("BG_%s_%s_%s_%s_%s.pdf",
					electricityInvoice.getInvoiceNumber(),
					date,
					legalPersonIdentifier,
					powerPlantIdentifier,
					electricityInvoice.getReportingPointOwn()
			));
            dbFile.setContentType("application/pdf");
            dbFile.setContent(pdfFileContent);

            return (DBFile) trySave(dbFile, false, true);

        } catch (IOException e) {
			Map<String, Object> error = new HashMap<>();
			error.put("id", electricityInvoice.getId());
			error.put("class", ElectricityInvoice.class.getSimpleName());
			error.put("field", null);
			error.put("value", null);
			error.put("error", ERROR_CREATING_PDF_FILE);
			
			invalidElectricityInvoiceRows.add(error);
            return null;
        }
    }

    public List<Map<String, Object>> generateElectricityInvoicePdf(ElectricityInvoice electricityInvoice) {

		List<Map<String, Object>> invalidElectricityInvoiceRows = new LinkedList<>();

		//session needed for when this is called from Camel
		try ( Session session = sessionFactory.openSession() ) {
			electricityInvoice = (ElectricityInvoice) session.merge(electricityInvoice);

			if(electricityInvoice.getInvoiceNumber() == null || electricityInvoice.getInvoiceNumber().isEmpty()) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", electricityInvoice.getId());
				error.put("class", ElectricityInvoice.class.getSimpleName());
				error.put("field", "invoiceNumber");
				error.put("value", electricityInvoice.getInvoiceNumber());
				error.put("error", "MissingInvoiceNumber");
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}
			if(!electricityInvoice.getIsValid()) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", electricityInvoice.getId());
				error.put("class", ElectricityInvoice.class.getSimpleName());
				error.put("field", "isValid");
				error.put("value", electricityInvoice.getIsValid());
				error.put("error", "NotValid");
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}
			if(electricityInvoice.getSent()) {
				Map<String, Object> error = new HashMap<>();
				error.put("id", electricityInvoice.getId());
				error.put("class", ElectricityInvoice.class.getSimpleName());
				error.put("field", "sent");
				error.put("value", electricityInvoice.getSent());
				error.put("error", "SentLock");
				
				invalidElectricityInvoiceRows.add(error);
				return invalidElectricityInvoiceRows;
			}

			LegalPerson recipient = ((LegalPersonRepository) getRepositories().getRepositoryFor(LegalPerson.class).get())
				.findFirstByIsSenderAndCompanyAndDeleted(true, getSecUser().getCompany(), false);

			LegalPerson legalPerson = electricityInvoice.getPowerPlant().getOwner();

			String legalPersonIdentifier = getLegalPersonIdentifier(legalPerson);

			String powerPlantIdentifier = electricityInvoice.getPowerPlant().getIdentificationNumber() != null
					? electricityInvoice.getPowerPlant().getIdentificationNumber()
					: (electricityInvoice.getPowerPlant().getProducerEic() != null ? electricityInvoice.getPowerPlant().getProducerEic() : null);

			// Create an Array of the values for the template
			String[] templateData = fillHtmlTemplateData(invalidElectricityInvoiceRows, electricityInvoice, electricityInvoice.getPowerPlant(), recipient);
			if(templateData == null) {
				return invalidElectricityInvoiceRows;
			}
			// Generate PDF file
			DBFile dbFile = generatePdfFile(
					electricityInvoice,
					DateFormatUtils.formatDateToYYYYMMDD(electricityInvoice.getCreatedDate()),
					legalPersonIdentifier,
					powerPlantIdentifier,
					templateData,
					invalidElectricityInvoiceRows
			);
			if(dbFile == null) {
				return invalidElectricityInvoiceRows;
			}

			// TODO: Check whether there is a key in the map with the current row?

			electricityInvoice.setDbFile(dbFile);
			electricityInvoice.setHasDbFile(true);

			// Save the entity
			trySave(electricityInvoice, true, true);
		}
        return invalidElectricityInvoiceRows;
    }

    void setUserActionEventStartedTime(UserActionEvent event) {
        event.setStartedTime(ZonedDateTime.now());
		trySave(event, true, true);
    }

    void saveEventExecutionDetails(UserActionEvent event, String executionDetails) {
        event.setExecutionDetails(executionDetails.getBytes(StandardCharsets.UTF_8));
		event.setFinishedTime(ZonedDateTime.now());
		trySave(event, true, true);
    }
}
