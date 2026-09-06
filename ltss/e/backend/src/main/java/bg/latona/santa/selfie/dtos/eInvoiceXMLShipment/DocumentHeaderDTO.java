package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentHeaderDTO {

    @XmlElement(name = EInvoiceXmlConstants.DOCUMENT_TYPE)
    private String documentType;

    @XmlElement(name = EInvoiceXmlConstants.DOCUMENT_DATE)
    private String documentDate;
}