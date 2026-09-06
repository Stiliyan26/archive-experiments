package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class AttachedFileDTO {

    @XmlValue
    private String fileName;

    @XmlAttribute(name = EInvoiceXmlConstants.ARCHIVE)
    private boolean isArchived;

    @XmlAttribute(name = EInvoiceXmlConstants.SHA1)
    private String SHA1;
}
