package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import bg.latona.santa.selfie.constant.EInvoiceXmlConstants;
import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "name",
        "street",
        "town",
        "zip",
        "country",
        "phone",
        "email",
        "fax",
        "web",
        "contact"
})
public class AddressDTO {

    @XmlElement(name = EInvoiceXmlConstants.NAME)
    private String name;

    @XmlElement(name = EInvoiceXmlConstants.STREET)
    private String street;

    @XmlElement(name = EInvoiceXmlConstants.TOWN)
    private String town;

    @XmlElement(name = EInvoiceXmlConstants.ZIP)
    private String zip;

    @XmlElement(name = EInvoiceXmlConstants.COUNTRY)
    private String country;

    @XmlElement(name = EInvoiceXmlConstants.PHONE)
    private String phone;

    @XmlElement(name = EInvoiceXmlConstants.EMAIL)
    private String email;

    @XmlElement(name = EInvoiceXmlConstants.FAX)
    private String fax;

    @XmlElement(name = EInvoiceXmlConstants.WEB)
    private String web;

    @XmlElement(name = EInvoiceXmlConstants.CONTACT)
    private String contact;
}
