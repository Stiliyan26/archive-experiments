package bg.latona.santa.selfie.service.eInvoice.helpers;

import bg.latona.santa.selfie.exception.eInvoiceSoap.ParserException;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Component
public class XmlParser {

    public String extractValueByTagName(String xmlResponse, String tagName) {
        try {
            Document document = setupDocument(xmlResponse);

            document.getDocumentElement().normalize();

            NodeList nodeList = getNodeListByTagName(document, tagName);

            if (nodeList.getLength() > 0) {
                return getTextContentFromNode(nodeList.item(0));
            }

            throw new ParserException("Tag '" + tagName + "' not found in XML response.");
        } catch (Exception e) {
            throw new ParserException("Error parsing XML response.", e);
        }
    }

    private Document setupDocument(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new InputSource(new StringReader(xmlResponse)));
    }

    private NodeList getNodeListByTagName(Document document, String tagName) {
        return document.getElementsByTagName(tagName);
    }

    private String getTextContentFromNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            return element.getTextContent();
        }

        return null;
    }
}
