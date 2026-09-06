
package bg.latona.santa.wsdl;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the bg.latona.santa.wsdl package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FileTicket_QNAME = new QName("https://efaktura.bg/soap/", "FileTicket");
    private final static QName _FileResultStatus_QNAME = new QName("https://efaktura.bg/soap/", "FileResultStatus");
    private final static QName _FileResultStatusText_QNAME = new QName("https://efaktura.bg/soap/", "FileResultStatusText");
    private final static QName _FileInvoiceCount_QNAME = new QName("https://efaktura.bg/soap/", "FileInvoiceCount");
    private final static QName _FileInvoiceCommit_QNAME = new QName("https://efaktura.bg/soap/", "FileInvoiceCommit");
    private final static QName _FileInvoiceAmount_QNAME = new QName("https://efaktura.bg/soap/", "FileInvoiceAmount");
    private final static QName _FileInvoices_QNAME = new QName("https://efaktura.bg/soap/", "FileInvoices");
    private final static QName _Invoices_QNAME = new QName("https://efaktura.bg/soap/", "Invoices");
    private final static QName _Documents_QNAME = new QName("https://efaktura.bg/soap/", "Documents");
    private final static QName _Attachment_QNAME = new QName("https://efaktura.bg/soap/", "Attachment");
    private final static QName _Invoice_QNAME = new QName("https://efaktura.bg/soap/", "Invoice");
    private final static QName _Document_QNAME = new QName("https://efaktura.bg/soap/", "Document");
    private final static QName _Errors_QNAME = new QName("https://efaktura.bg/soap/", "errors");
    private final static QName _Err_QNAME = new QName("https://efaktura.bg/soap/", "err");
    private final static QName _ErrText_QNAME = new QName("https://efaktura.bg/soap/", "errText");
    private final static QName _Xpath_QNAME = new QName("https://efaktura.bg/soap/", "xpath");
    private final static QName _FileName_QNAME = new QName("https://efaktura.bg/soap/", "FileName");
    private final static QName _InvoiceNumber_QNAME = new QName("https://efaktura.bg/soap/", "InvoiceNumber");
    private final static QName _InvoiceDate_QNAME = new QName("https://efaktura.bg/soap/", "InvoiceDate");
    private final static QName _DocumentDate_QNAME = new QName("https://efaktura.bg/soap/", "DocumentDate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: bg.latona.santa.wsdl
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FileResultType }
     * 
     */
    public FileResultType createFileResultType() {
        return new FileResultType();
    }

    /**
     * Create an instance of {@link InvoicesType }
     * 
     */
    public InvoicesType createInvoicesType() {
        return new InvoicesType();
    }

    /**
     * Create an instance of {@link AttachmentType }
     * 
     */
    public AttachmentType createAttachmentType() {
        return new AttachmentType();
    }

    /**
     * Create an instance of {@link InvoiceType }
     * 
     */
    public InvoiceType createInvoiceType() {
        return new InvoiceType();
    }

    /**
     * Create an instance of {@link DocumentType }
     * 
     */
    public DocumentType createDocumentType() {
        return new DocumentType();
    }

    /**
     * Create an instance of {@link ErrorsType }
     * 
     */
    public ErrorsType createErrorsType() {
        return new ErrorsType();
    }

    /**
     * Create an instance of {@link ErrType }
     * 
     */
    public ErrType createErrType() {
        return new ErrType();
    }

    /**
     * Create an instance of {@link FileStatusType }
     * 
     */
    public FileStatusType createFileStatusType() {
        return new FileStatusType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileTicket")
    public JAXBElement<String> createFileTicket(String value) {
        return new JAXBElement<String>(_FileTicket_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileResultStatus")
    public JAXBElement<String> createFileResultStatus(String value) {
        return new JAXBElement<String>(_FileResultStatus_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileResultStatusText")
    public JAXBElement<String> createFileResultStatusText(String value) {
        return new JAXBElement<String>(_FileResultStatusText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileInvoiceCount")
    public JAXBElement<BigInteger> createFileInvoiceCount(BigInteger value) {
        return new JAXBElement<BigInteger>(_FileInvoiceCount_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileInvoiceCommit")
    public JAXBElement<BigInteger> createFileInvoiceCommit(BigInteger value) {
        return new JAXBElement<BigInteger>(_FileInvoiceCommit_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Float }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Float }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileInvoiceAmount")
    public JAXBElement<Float> createFileInvoiceAmount(Float value) {
        return new JAXBElement<Float>(_FileInvoiceAmount_QNAME, Float.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FileResultType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FileResultType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileInvoices")
    public JAXBElement<FileResultType> createFileInvoices(FileResultType value) {
        return new JAXBElement<FileResultType>(_FileInvoices_QNAME, FileResultType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvoicesType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link InvoicesType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "Invoices")
    public JAXBElement<InvoicesType> createInvoices(InvoicesType value) {
        return new JAXBElement<InvoicesType>(_Invoices_QNAME, InvoicesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvoicesType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link InvoicesType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "Documents")
    public JAXBElement<InvoicesType> createDocuments(InvoicesType value) {
        return new JAXBElement<InvoicesType>(_Documents_QNAME, InvoicesType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AttachmentType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "Attachment")
    public JAXBElement<AttachmentType> createAttachment(AttachmentType value) {
        return new JAXBElement<AttachmentType>(_Attachment_QNAME, AttachmentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InvoiceType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link InvoiceType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "Invoice")
    public JAXBElement<InvoiceType> createInvoice(InvoiceType value) {
        return new JAXBElement<InvoiceType>(_Invoice_QNAME, InvoiceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "Document")
    public JAXBElement<DocumentType> createDocument(DocumentType value) {
        return new JAXBElement<DocumentType>(_Document_QNAME, DocumentType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrorsType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrorsType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "errors")
    public JAXBElement<ErrorsType> createErrors(ErrorsType value) {
        return new JAXBElement<ErrorsType>(_Errors_QNAME, ErrorsType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ErrType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ErrType }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "err")
    public JAXBElement<ErrType> createErr(ErrType value) {
        return new JAXBElement<ErrType>(_Err_QNAME, ErrType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "errText")
    public JAXBElement<String> createErrText(String value) {
        return new JAXBElement<String>(_ErrText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "xpath")
    public JAXBElement<String> createXpath(String value) {
        return new JAXBElement<String>(_Xpath_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "FileName")
    public JAXBElement<String> createFileName(String value) {
        return new JAXBElement<String>(_FileName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link String }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "InvoiceNumber")
    public JAXBElement<String> createInvoiceNumber(String value) {
        return new JAXBElement<String>(_InvoiceNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "InvoiceDate")
    public JAXBElement<XMLGregorianCalendar> createInvoiceDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_InvoiceDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     */
    @XmlElementDecl(namespace = "https://efaktura.bg/soap/", name = "DocumentDate")
    public JAXBElement<XMLGregorianCalendar> createDocumentDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_DocumentDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

}
