package bg.latona.santa.selfie.dtos.sapXMLShipment;

import bg.latona.santa.selfie.constant.SapXmlConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexDataDTO {

    @XmlElement(name = SapXmlConstants.INVOICE_HEADER)
    private InvoiceHeaderDTO invoiceHeader;
}
