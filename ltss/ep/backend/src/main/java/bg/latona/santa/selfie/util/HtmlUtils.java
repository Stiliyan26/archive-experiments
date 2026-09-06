package bg.latona.santa.selfie.util;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.HTML_REPLACE_SYMBOL;


public class HtmlUtils {

    public static InputStream fillHtmlTemplate(String INPUT_HTML_FILE_PATH, String[] data) throws IOException {
        int currentIndex = 0;
        StringBuilder resultHtml = new StringBuilder();

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(INPUT_HTML_FILE_PATH)), StandardCharsets.UTF_8))
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                StringBuilder lineBuilder = new StringBuilder(line);

                int index;
                while (currentIndex < data.length && (index = lineBuilder.indexOf(HTML_REPLACE_SYMBOL)) != -1) {
                    lineBuilder.replace(index, index + HTML_REPLACE_SYMBOL.length(), data[currentIndex] == null ? "" : data[currentIndex]);
                    currentIndex++;
                }
                resultHtml.append(lineBuilder).append(System.lineSeparator());
            }
        }

        return new ByteArrayInputStream(resultHtml.toString().getBytes(StandardCharsets.UTF_8));
    }
}
