
package bg.latona.santa.wsdl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FileResultType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FileResultType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}Invoices" minOccurs="0"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}Documents" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileResultType", propOrder = {
    "invoices",
    "documents"
})
public class FileResultType {

    @XmlElement(name = "Invoices")
    protected InvoicesType invoices;
    @XmlElement(name = "Documents")
    protected InvoicesType documents;

    /**
     * Gets the value of the invoices property.
     * 
     * @return
     *     possible object is
     *     {@link InvoicesType }
     *     
     */
    public InvoicesType getInvoices() {
        return invoices;
    }

    /**
     * Sets the value of the invoices property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoicesType }
     *     
     */
    public void setInvoices(InvoicesType value) {
        this.invoices = value;
    }

    /**
     * Gets the value of the documents property.
     * 
     * @return
     *     possible object is
     *     {@link InvoicesType }
     *     
     */
    public InvoicesType getDocuments() {
        return documents;
    }

    /**
     * Sets the value of the documents property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoicesType }
     *     
     */
    public void setDocuments(InvoicesType value) {
        this.documents = value;
    }

}
