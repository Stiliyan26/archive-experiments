package bg.latona.santa.selfie.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ZipDownloadResponse {

    private byte[] content;

    private String filename;
}