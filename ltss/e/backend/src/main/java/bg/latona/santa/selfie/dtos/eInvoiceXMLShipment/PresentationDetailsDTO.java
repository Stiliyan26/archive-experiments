package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor()
@XmlAccessorType(XmlAccessType.FIELD)
public class PresentationDetailsDTO {

    @XmlElement(name = EInvoiceXmlConstants.LAYOUT_ID)
    private String layoutId;

    @XmlElement(name = EInvoiceXmlConstants.LANGUAGE)
    private String language;

    @XmlElement(name = EInvoiceXmlConstants.TRANSFORMATION_ID)
    private String transformationId;

    @XmlElement(name = EInvoiceXmlConstants.DOCUMENT_TITLE)
    private String documentTitle;

    @Builder.Default
    @XmlElementWrapper(name = EInvoiceXmlConstants.ATTACHMENTS)
    @XmlElement(name = EInvoiceXmlConstants.ATTACHED_FILE)
    private List<AttachedFileDTO> attachedFiles = new ArrayList<>();
}