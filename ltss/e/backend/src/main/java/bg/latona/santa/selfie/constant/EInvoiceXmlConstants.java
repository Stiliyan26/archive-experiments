package bg.latona.santa.selfie.constant;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class EInvoiceXmlConstants {

    //prop order
    public static final String ORDER_DOCUMENT_HEADER = "documentHeader";
    public static final String ORDER_SENDER = "sender";
    public static final String ORDER_RECIPIENT = "recipient";
    public static final String ORDER_PRESENTATION_DETAILS = "presentationDetails";


    //elements
    public static final String DOCUMENT = "Document";

    public static final String DOCUMENT_HEADER = "DocumentHeader";
    public static final String SENDER = "Sender";
    public static final String RECIPIENT = "Recipient";
    public static final String PRESENTATION_DETAILS = "PresentationDetails";

    public static final String DOCUMENT_TYPE = "DocumentType";
    public static final String DOCUMENT_DATE = "DocumentDate";

    public static final String SENDER_RECIPIENT_ID = "SendersRecipientID";
    public static final String IDENTIFICATION_NUMBER = "IdentificationNumber";
    public static final String ADDRESS = "Address";

    public static final String NAME = "Name";
    public static final String STREET = "Street";
    public static final String TOWN = "Town";
    public static final String ZIP = "ZIP";
    public static final String COUNTRY = "Country";
    public static final String PHONE = "Phone";
    public static final String EMAIL = "Email";
    public static final String FAX = "Fax";
    public static final String WEB = "Web";
    public static final String CONTACT = "Contact";

    public static final String LAYOUT_ID = "LayoutID";
    public static final String LANGUAGE = "Language";
    public static final String TRANSFORMATION_ID = "TransformationID";
    public static final String DOCUMENT_TITLE = "DocumentTitle";

    public static final String ATTACHMENTS = "Attachments";
    public static final String ATTACHED_FILE = "AttachedFile";
    public static final String ARCHIVE = "Archive";
    public static final String SHA1 = "SHA1";

    //Attributes
    public static final String GENERATING_SYSTEM = "GeneratingSystem";
    public static final String CANCELLATION = "Cancellation";
    public static final String DS_NAMESPACE = "xmlns:ds";
}