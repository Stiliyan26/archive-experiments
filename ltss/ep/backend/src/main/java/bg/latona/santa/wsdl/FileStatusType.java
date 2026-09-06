
package bg.latona.santa.wsdl;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FileStatusType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FileStatusType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileTicket"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileResultStatus"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileResultStatusText"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileInvoiceCount"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileInvoiceCommit"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileInvoiceAmount"/&gt;
 *         &lt;element ref="{https://efaktura.bg/soap/}FileInvoices"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FileStatusType", propOrder = {
    "fileTicket",
    "fileResultStatus",
    "fileResultStatusText",
    "fileInvoiceCount",
    "fileInvoiceCommit",
    "fileInvoiceAmount",
    "fileInvoices"
})
public class FileStatusType {

    @XmlElement(name = "FileTicket", required = true)
    protected String fileTicket;
    @XmlElement(name = "FileResultStatus", required = true)
    protected String fileResultStatus;
    @XmlElement(name = "FileResultStatusText", required = true)
    protected String fileResultStatusText;
    @XmlElement(name = "FileInvoiceCount", required = true)
    protected BigInteger fileInvoiceCount;
    @XmlElement(name = "FileInvoiceCommit", required = true)
    protected BigInteger fileInvoiceCommit;
    @XmlElement(name = "FileInvoiceAmount")
    protected float fileInvoiceAmount;
    @XmlElement(name = "FileInvoices", required = true)
    protected FileResultType fileInvoices;

    /**
     * Gets the value of the fileTicket property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileTicket() {
        return fileTicket;
    }

    /**
     * Sets the value of the fileTicket property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileTicket(String value) {
        this.fileTicket = value;
    }

    /**
     * Gets the value of the fileResultStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileResultStatus() {
        return fileResultStatus;
    }

    /**
     * Sets the value of the fileResultStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileResultStatus(String value) {
        this.fileResultStatus = value;
    }

    /**
     * Gets the value of the fileResultStatusText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileResultStatusText() {
        return fileResultStatusText;
    }

    /**
     * Sets the value of the fileResultStatusText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileResultStatusText(String value) {
        this.fileResultStatusText = value;
    }

    /**
     * Gets the value of the fileInvoiceCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileInvoiceCount() {
        return fileInvoiceCount;
    }

    /**
     * Sets the value of the fileInvoiceCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileInvoiceCount(BigInteger value) {
        this.fileInvoiceCount = value;
    }

    /**
     * Gets the value of the fileInvoiceCommit property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFileInvoiceCommit() {
        return fileInvoiceCommit;
    }

    /**
     * Sets the value of the fileInvoiceCommit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setFileInvoiceCommit(BigInteger value) {
        this.fileInvoiceCommit = value;
    }

    /**
     * Gets the value of the fileInvoiceAmount property.
     * 
     */
    public float getFileInvoiceAmount() {
        return fileInvoiceAmount;
    }

    /**
     * Sets the value of the fileInvoiceAmount property.
     * 
     */
    public void setFileInvoiceAmount(float value) {
        this.fileInvoiceAmount = value;
    }

    /**
     * Gets the value of the fileInvoices property.
     * 
     * @return
     *     possible object is
     *     {@link FileResultType }
     *     
     */
    public FileResultType getFileInvoices() {
        return fileInvoices;
    }

    /**
     * Sets the value of the fileInvoices property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileResultType }
     *     
     */
    public void setFileInvoices(FileResultType value) {
        this.fileInvoices = value;
    }

}
