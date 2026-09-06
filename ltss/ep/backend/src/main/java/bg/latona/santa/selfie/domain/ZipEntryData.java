package bg.latona.santa.selfie.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ZipEntryData {

    private final String name;

    private final byte[] content;
}
