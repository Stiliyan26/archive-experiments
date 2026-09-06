package bg.latona.santa.selfie.util;

import bg.latona.santa.selfie.domain.ZipDownloadResponse;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@UtilityClass
public class ZipDownloadUtil {

    public ResponseEntity<ByteArrayResource> createZipResponse(ZipDownloadResponse zipData) {
        ByteArrayResource resource = new ByteArrayResource(zipData.getContent());

        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                String.format("attachment; filename=%s.zip", zipData.getFilename()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipData.getContent().length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
