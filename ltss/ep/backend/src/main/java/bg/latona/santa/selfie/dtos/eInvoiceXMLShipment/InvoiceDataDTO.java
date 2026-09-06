package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.*;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@XmlRootElement(name = EInvoiceXmlConstants.DOCUMENT)
@XmlType(propOrder = {
        EInvoiceXmlConstants.ORDER_DOCUMENT_HEADER,
        EInvoiceXmlConstants.ORDER_SENDER,
        EInvoiceXmlConstants.ORDER_RECIPIENT,
        EInvoiceXmlConstants.ORDER_PRESENTATION_DETAILS
})
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceDataDTO {

    @XmlAttribute(name = EInvoiceXmlConstants.DS_NAMESPACE, required = true)
    private final String dsNamespace = "http://www.w3.org/2000/09/xmldsig#";

    @XmlAttribute(name = EInvoiceXmlConstants.GENERATING_SYSTEM, required = true)
    private String generatingSystem = "SDXTools v1.0.1203";

    @XmlAttribute(name = EInvoiceXmlConstants.CANCELLATION, required = true)
    private boolean cancellation = false;

    @XmlElement(name = EInvoiceXmlConstants.DOCUMENT_HEADER)
    private DocumentHeaderDTO documentHeader;

    @XmlElement(name = EInvoiceXmlConstants.SENDER)
    private SenderDTO sender;

    @XmlElement(name = EInvoiceXmlConstants.RECIPIENT)
    private RecipientDTO recipient;

    @XmlElement(name = EInvoiceXmlConstants.PRESENTATION_DETAILS)
    private PresentationDetailsDTO presentationDetails;
}

