package bg.latona.santa.selfie.util;


import com.itextpdf.html2pdf.HtmlConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;


public class PdfUtils {

    public static ResponseEntity<FileSystemResource> createPdfResponse(String pdfFilePath) {
        File pdfFile = new File(pdfFilePath);

        if (!pdfFile.exists())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        FileSystemResource resource = new FileSystemResource(pdfFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", pdfFile.getName());
        headers.setContentLength(pdfFile.length());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }


    public static byte[] convertHtmlFileToPdfByteArray(InputStream htmlInputStream) throws IOException {
        try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(htmlInputStream, pdfOutputStream);

            return pdfOutputStream.toByteArray();
        }
    }


    public static void convertExcelFileToPdf( // using LibreOffice
                                              String outputFilePath,
                                              String folderPath
    ) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "C:\\Program Files\\LibreOffice\\program\\soffice",
                    "--headless",
                    "--convert-to", "pdf",
                    outputFilePath + ".xlsx",
                    "--outdir",
                    folderPath
            );

            Process process = builder.start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return; // Exit the method if an error occurs while converting the Excel file to PDF
        }

        try (PDDocument document = PDDocument.load(new File(outputFilePath + ".pdf"))) {
            PDDocument singlePageDoc = new PDDocument();
            PDPage firstPage = document.getPage(0);
            singlePageDoc.addPage(firstPage);
            singlePageDoc.save(outputFilePath + ".pdf");
            singlePageDoc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
