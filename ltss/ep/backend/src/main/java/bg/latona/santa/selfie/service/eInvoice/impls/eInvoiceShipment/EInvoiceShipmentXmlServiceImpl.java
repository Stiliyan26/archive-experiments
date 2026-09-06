package bg.latona.santa.selfie.service.eInvoice.impls.eInvoiceShipment;

import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.*;
import bg.latona.santa.selfie.service.common.impls.entityRelated.DbLegalPersonServiceImpl;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment.EInvoiceShipmentXmlService;
import bg.latona.santa.entities.person.Contact;
import bg.latona.santa.entities.person.LegalPerson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EInvoiceShipmentXmlServiceImpl implements EInvoiceShipmentXmlService {

    private final DbLegalPersonServiceImpl dbLegalPersonService;

	@Value("${tsee.identificationNumber}")
	private String identificationNumberTSEE;

    public InvoiceDataDTO createInvoiceData(LegalPerson legalPerson) {
        DocumentHeaderDTO documentHeaderDTO = createDocumentHeader();
        SenderDTO senderDTO = createSender();
        RecipientDTO recipientDTO = createRecipient(legalPerson);
        PresentationDetailsDTO presentationDetailsDTO = createPresentationDetailsDTO();

        InvoiceDataDTO invoiceDataDTO = new InvoiceDataDTO();

        invoiceDataDTO.setDocumentHeader(documentHeaderDTO);
        invoiceDataDTO.setSender(senderDTO);
        invoiceDataDTO.setRecipient(recipientDTO);
        invoiceDataDTO.setPresentationDetails(presentationDetailsDTO);

        return invoiceDataDTO;
    }

    //TODO: use relation to get the loiDocumentType
    private DocumentHeaderDTO createDocumentHeader() {
        return DocumentHeaderDTO.builder()
                .documentType("SDX")
                .documentDate("2024-04-10")
                .build();
    }

	//Todo change params
    private SenderDTO createSender() {
        LegalPerson sender = dbLegalPersonService.getLoggedLegalPerson(true);

        AddressDTO senderAddressDto = sender != null
                ? createAddressFromLegalPerson(sender)
                : createDefaultAddress();

		String identificationNumber = sender != null
				? sender.getIdentificationNumber()
				: identificationNumberTSEE;

        return SenderDTO.builder()
                .identificationNumber(identificationNumber)
                .address(senderAddressDto)
                .build();
    }

    private AddressDTO createAddressFromLegalPerson(LegalPerson sender) {
        Contact contact = sender.getContacts() != null && !sender.getContacts().isEmpty()
                ? sender.getContacts().get(0)
                : null;

        return AddressDTO.builder()
                .name(sender.getName())
                .street(sender.getStreet())
                .town(sender.getCity())
                .zip(sender.getPostCode())
                .country(sender.getCountry())
                .phone(contact != null ? contact.getPhone() : "")
                .email(contact != null ? contact.getEmail() : "")
                .fax(contact != null ? contact.getFax() : "")
                .web(contact != null ? contact.getWebSite() : "")
                .contact(contact != null ? contact.getName() : "")
                .build();
    }

    private AddressDTO createDefaultAddress() {
        return AddressDTO.builder()
                .name("ЕВН България Електроснабдяване ЕАД Краен снабдител")
                .street("ул. Христо Г.Данов, 37")
                .town("Пловдив")
                .zip("1000")
                .country("България")
                .phone("+359 700 1 3636")
                .email("info@evn-trading.com")
                .fax("Ф +359 2 980 25 99")
                .web("www.evn-trading.com")
                .contact("Мариана Стоянова")
                .build();
    }

    private RecipientDTO createRecipient(LegalPerson legalPerson) {
        String phone = "";
        String email = "";
        String fax = "";

        if (legalPerson.getContacts() != null && !legalPerson.getContacts().isEmpty()) {
            Contact contact = legalPerson.getContacts().get(0);
            phone = contact.getPhone();
            email = contact.getEmail();
            fax = contact.getFax();
        }

        AddressDTO recipientAddress = AddressDTO
                .builder()
                .name(legalPerson.getName())
                .street(legalPerson.getStreet())
                .town(legalPerson.getCity())
                .zip(legalPerson.getPostCode())
                .phone(phone)
                .email(email)
                .fax(fax)
                .build();

        return RecipientDTO.builder()
                .identificationNumber(legalPerson.getIdentificationNumber())
                .sendersRecipientID(legalPerson.getId().toString())
                .address(recipientAddress)
                .build();
    }

    private PresentationDetailsDTO createPresentationDetailsDTO() {
        return PresentationDetailsDTO.builder()
                .layoutId("0200")
                .language("bul")
                .transformationId("1")
                .documentTitle("Важна пратка")
                .build();
    }
}
