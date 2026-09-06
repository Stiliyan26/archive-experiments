package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@Builder
@XmlAccessorType(XmlAccessType.FIELD)
public class SenderDTO {

    @XmlElement(name = EInvoiceXmlConstants.IDENTIFICATION_NUMBER)
    private String identificationNumber;

    @XmlElement(name = EInvoiceXmlConstants.ADDRESS)
    private AddressDTO address;
}