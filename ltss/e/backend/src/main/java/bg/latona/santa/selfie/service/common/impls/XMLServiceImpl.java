package bg.latona.santa.selfie.service.common.impls;

import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.EmptyElementListener;
import bg.latona.santa.selfie.service.common.interfaces.XMLService;
import bg.latona.santa.reports.ReportException;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class XMLServiceImpl implements XMLService {

    private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    @Override
    public <T> byte[] createXMLByteArray(T data) {
      return createXMLByteArray(data, null);
    }

    @Override
    public <T> byte[] createXMLByteArray(T data, String includeStylesheet) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(data.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);


			marshaller.setListener(new EmptyElementListener());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.write(XML_DECLARATION.getBytes());

            if (includeStylesheet != null) {
                baos.write(includeStylesheet.getBytes());
            }

            marshaller.marshal(data, baos);

            return baos.toByteArray();

        } catch (JAXBException | IOException e) {
            e.printStackTrace();

            throw new ReportException("Failed to generate XML for shipment details.");
        }
    }
}
