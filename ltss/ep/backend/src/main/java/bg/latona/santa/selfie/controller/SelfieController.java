package bg.latona.santa.selfie.controller;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;
import bg.latona.santa.selfie.domain.ZipDownloadResponse;
import bg.latona.santa.selfie.service.downloadPdf.interfaces.PdfService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceUploadService.EInvoiceUploadService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.ElectricityInvoiceService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPFileExportService;
import bg.latona.santa.selfie.service.signature.XAdESSigner;
import bg.latona.santa.selfie.util.ZipDownloadUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping(value = "/api/reports")
public class SelfieController {

    private final EInvoiceUploadService eInvoiceUploadService;
    private final PdfService pdfService;
    private final SAPFileExportService sapFileExportService;
    private final ElectricityInvoiceService electricityInvoiceService;

    private final XAdESSigner xAdESSigner;

/*
	@PostMapping(value = "/signXades",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
	public ResponseEntity<String> signXades(@RequestBody String xmlContent) {
        try {
			String signedXml = String.valueOf(xAdESSigner.signEnvelopedSec());



            return ResponseEntity.ok(signedXml);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("<error>" + e.getMessage() + "</error>");
        }
    }

*/


    @PostMapping(value = "/uploadZipFile")
    public ResponseEntity<String> uploadZipFile(@RequestBody Set<String> filterRequest) {
        String response = eInvoiceUploadService.uploadZipFileEInvoice(filterRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Shipment ZIP created and uploaded successfully! " + response);
    }


    @GetMapping(value = "/downloadPdfZip")
    ResponseEntity<ByteArrayResource> downloadPdfZip(@ModelAttribute FileFilterRequest fileFilterRequest) {
        byte[] pdfZip = pdfService.downloadAndZipPdfs(fileFilterRequest);

        return ZipDownloadUtil.createZipResponse(
                ZipDownloadResponse.builder()
                        .content(pdfZip)
                        .filename("compiled_pdfs")
                        .build()
        );
    }


    @GetMapping(value = "/downloadSapFilesZip")
    ResponseEntity<ByteArrayResource> downloadSapFilesZip(@ModelAttribute FileFilterRequest fileFilterRequest) {
        byte[] sapZip = sapFileExportService.downloadSapFilesZip(fileFilterRequest);

        return ZipDownloadUtil.createZipResponse(
                ZipDownloadResponse.builder()
                        .content(sapZip)
                        .filename("sap_files")
                        .build()
        );
    }


    @PostMapping(value = "/importValuesFile/{dbFileId}")
    @ResponseBody
    Map<Integer, List<String>> importValuesFile(@PathVariable Long dbFileId) {
        return electricityInvoiceService.processValuesFile(dbFileId);
    }


    @PostMapping(value = "/importQuantitiesFile/{dbFileId}")
    @ResponseBody
    Map<Integer, List<String>> importQuantitiesFile(@PathVariable Long dbFileId) {
        return electricityInvoiceService.processQuantitiesFile(dbFileId);
    }


    @PostMapping(value = "/importValuesAndQuantitiesFile/{dbFileId}")
    @ResponseBody
    Map<Integer, List<String>> importValuesAndQuantitiesFile(@PathVariable Long dbFileId) {
        return electricityInvoiceService.processValuesAndQuantitiesFile(dbFileId);
    }


    @GetMapping(value = "/populateElectricityInvoice")
    public Set<Map<String, Set<String>>> populateElectricityInvoice(
            @RequestParam("periodFrom") String periodFrom,
            @RequestParam("periodTo") String periodTo,
            @RequestParam("taxEventDate") String taxEventDate,
            @RequestParam("documentType") String documentType
    ) {
        return electricityInvoiceService.populateElectricityInvoice(periodFrom, periodTo, taxEventDate, documentType);
    }


    @GetMapping(value = "/generatePdfDocuments")
    public Map<String, List<String>> generatePdfDocuments() {
        return electricityInvoiceService.generatePdfDocuments();
    }


    @PostMapping(value = "/createUpdatedElectricityInvoice/{electricityInvoiceId}/{totalQuantity}/{priceInLevs}/{loiDocumentTypeId}")
    public ResponseEntity<Void> createUpdatedElectricityInvoice(
            @PathVariable Long electricityInvoiceId,
            @PathVariable BigDecimal totalQuantity,
            @PathVariable BigDecimal priceInLevs,
            @PathVariable Long loiDocumentTypeId) {
        electricityInvoiceService.createUpdatedElectricityInvoice(electricityInvoiceId, totalQuantity, priceInLevs, loiDocumentTypeId);

        return ResponseEntity.ok().build();
    }
}
