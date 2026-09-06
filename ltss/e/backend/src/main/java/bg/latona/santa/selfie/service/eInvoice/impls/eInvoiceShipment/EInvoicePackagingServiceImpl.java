package bg.latona.santa.selfie.service.eInvoice.impls.eInvoiceShipment;

import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.domain.UploadZip;
import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.InvoiceDataDTO;
import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment.EInvoicePackagingService;
import bg.latona.santa.selfie.service.common.interfaces.XMLService;
import bg.latona.santa.selfie.service.signature.XAdESSigner;
import bg.latona.santa.selfie.service.zip.interfaces.ZipService;
import bg.latona.santa.reports.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class EInvoicePackagingServiceImpl implements EInvoicePackagingService {

    private final XMLService xmlService;
    private final FileService fileService;
    private final ZipService zipServiceImpl;
	private final XAdESSigner xAdESSigner;

    private static final String STYLESHEET_DECLARATION_FORMAT =
            "<?xml-stylesheet href=\"BG175370769_1_xslt_bul.xslt\" type=\"text/xsl\"?>\n";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public UploadZip createShipmentZip(InvoiceDataDTO invoiceData, List<ZipEntryData> attachments) throws Exception {
        String zipFileName = generateZipFileName(formatter);

        log.info("Starting shipment details XML creation for Invoices");
        byte[] shipmentXMLBytes = xmlService.createXMLByteArray(invoiceData, STYLESHEET_DECLARATION_FORMAT);

		byte[] signedXmlForEFaktura = null;

		byte[] signEnveloped = xAdESSigner.signEnveloped(shipmentXMLBytes);

		if (signEnveloped != null) {
			signedXmlForEFaktura = signEnveloped;
		} else {
			signedXmlForEFaktura = shipmentXMLBytes;
		}

        log.info("Start zipping for Invoice");
        byte[] shipmentZipBytes = zipServiceImpl.createZip(signedXmlForEFaktura, attachments);

        //TODO: remove for production
        String outputPath = fileService.createDirectory("eInvoiceZIP") + File.separator + zipFileName;

        try {
            fileService.writeContentToFile(shipmentZipBytes, outputPath);
        } catch (IOException e) {
            throw new ReportException("Creating zip file failed.");
        }

        log.info("Successfully generated and saved ZIP file: {}", zipFileName);

        return new UploadZip(zipFileName, shipmentZipBytes);
    }

    private String generateZipFileName(DateTimeFormatter formatter) {
        String timestamp = LocalDateTime.now().format(formatter);
        String uniqueId = UUID.randomUUID().toString();

        return "eFaktura_" + timestamp + "_" + uniqueId +  ".zip";
    }
}
