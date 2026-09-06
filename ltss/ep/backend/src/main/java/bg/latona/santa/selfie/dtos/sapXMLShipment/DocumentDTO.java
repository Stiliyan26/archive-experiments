package bg.latona.santa.selfie.dtos.sapXMLShipment;

import bg.latona.santa.selfie.constant.SapXmlConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = SapXmlConstants.DOCUMENT)
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentDTO {

    @XmlElement(name = SapXmlConstants.INDEX_DATA)
    private IndexDataDTO indexData;
}
