package bg.latona.santa.selfie.service.sap.impls;

import bg.latona.santa.selfie.dtos.sapXMLShipment.DocumentDTO;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.XMLService;
import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPPackagingService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPXMLService;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.selfie.SapXml;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.reports.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
@Service
public class SAPPackagingServiceImpl implements SAPPackagingService {

    private final XMLService xmlService;
    private final FileService fileService;
    private final SAPXMLService sapxmlService;
    private final EntityPersistenceService entityPersistenceService;

    private static final String SAP_XML_FOLDER = "sapXml";
    private static String fileName;


    @Override
    public void writeSapXmlFile(ElectricityInvoice electricityInvoice) {
        try {
            DocumentDTO documentDTO = sapxmlService.createDocumentDTO(electricityInvoice);

            byte[] sapDocumentByteArray = xmlService.createXMLByteArray(documentDTO);

            fileName = electricityInvoice.getDbFile().getName();

            if (fileName.endsWith(".pdf")) {
                fileName = fileName.replace(".pdf", ".xml");
            }

            String fullPath = getFilePath();

            fileService.writeContentToFile(sapDocumentByteArray, fullPath);

            DBFile dbFile = createDbFile(sapDocumentByteArray);
            entityPersistenceService.persistEntity(dbFile, false, true);

            SapXml sapXml = createSapXml(electricityInvoice, dbFile);
            entityPersistenceService.persistEntity(sapXml, false, false);

            log.info("Successfully wrote SAP XML file: {}", fileName);

        } catch (Exception e) {
            log.error("Error writing SAP XML file: {}", e.getMessage());
            throw new ReportException("Failed to write SAP XML file: " + e.getMessage());
        }
    }

    private SapXml createSapXml(ElectricityInvoice electricityInvoice, DBFile dbFile) {
        SapXml sapXml = new SapXml();

        sapXml.setElectricityInvoice(electricityInvoice);
        sapXml.setDbFile(dbFile);

        return sapXml;
    }

    private DBFile createDbFile(byte[] sapDocumentByteArray) {
        DBFile dbFile = new DBFile();

        dbFile.setContent(sapDocumentByteArray);
        dbFile.setContentType("application/xml");
        dbFile.setName(fileName);

        return dbFile;
    }


    private String getPowerPlantIdentifier(PowerPlant powerPlant) {
        return powerPlant.getIdentificationNumber() != null
                ? powerPlant.getIdentificationNumber()
                : powerPlant.getProducerEic();
    }

    private String createFolderPath() {
        return fileService.createDirectory(SAP_XML_FOLDER);
    }

    private String getFilePath() {
        return createFolderPath() + File.separator + fileName;
    }
}
