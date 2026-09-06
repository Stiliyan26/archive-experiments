package bg.latona.santa.selfie.service.signature;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;

@Slf4j
@Service
public class XMLSignerService {

    private static final String SIGNATURE_METHOD_RSA_SHA1 =
            "http://www.w3.org/2000/09/xmldsig#rsa-sha1";

    private static final String TRANSFORM_ENVELOPED =
            "http://www.w3.org/2000/09/xmldsig#enveloped-signature";

    private static final String CANONICAL_XML_1_0 =
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";

    private static final String DIGEST_SHA1 =
            "http://www.w3.org/2000/09/xmldsig#sha1";


    @Value("${xml.signer.keystore-path}")
    private String keystorePath;

    @Value("${xml.signer.keystore-password}")
    private String keystorePassword;

    @Value("${xml.signer.key-alias}")
    private String keyAlias;


    public String signXML(String xmlContent, String referenceId) throws Exception {
        log.info("Starting XML signing process");

        try {
            // Load the keystore
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keystore.load(fis, keystorePassword.toCharArray());
            }

            PrivateKey privateKey = (PrivateKey) keystore.getKey(keyAlias, keystorePassword.toCharArray());
            X509Certificate certificate = (X509Certificate) keystore.getCertificate(keyAlias);

            if (privateKey == null || certificate == null) {
                throw new RuntimeException("Failed to load private key or certificate for alias: " + keyAlias);
            }

            // Parse the XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

            // Set ID attribute on root element
            Element rootElement = doc.getDocumentElement();
            rootElement.setAttributeNS(null, "Id", referenceId);
            rootElement.setIdAttributeNS(null, "Id", true);

            // Create XMLSignatureFactory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Create Reference with single transform
            Reference ref = fac.newReference(
                    "#" + referenceId,
                    fac.newDigestMethod(DIGEST_SHA1, null),
                    Collections.singletonList(
                            fac.newTransform(TRANSFORM_ENVELOPED, (TransformParameterSpec) null)
                    ),
                    null,
                    null
            );

            // Create SignedInfo
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(
                            CANONICAL_XML_1_0,
                            (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SIGNATURE_METHOD_RSA_SHA1, null),
                    Collections.singletonList(ref)
            );

            // Create KeyInfo with RSA KeyValue
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            RSAPublicKey rsaKey = (RSAPublicKey) certificate.getPublicKey();
            KeyValue keyValue = kif.newKeyValue(rsaKey);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(keyValue));

            // Create XMLSignature
            XMLSignature signature = fac.newXMLSignature(si, ki);

            // Create DOMSignContext and add namespace mappings
            DOMSignContext dsc = new DOMSignContext(privateKey, doc.getDocumentElement());
            dsc.putNamespacePrefix(XMLSignature.XMLNS, "");
            dsc.setDefaultNamespacePrefix("");

            // Sign the document
            signature.sign(dsc);

            // Handle namespaces after signing
            NodeList signatureList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            NodeList signedInfoList = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "SignedInfo");

            if (signatureList.getLength() > 0 && signedInfoList.getLength() > 0) {
                Element signatureElement = (Element) signatureList.item(0);
                Element signedInfo = (Element) signedInfoList.item(0);

                // Remove SignedInfo from its current position
                Node oldSignedInfo = signatureElement.removeChild(signedInfo);

                // Create a new SignedInfo element with correct namespaces
                Element newSignedInfo = doc.createElementNS(XMLSignature.XMLNS, "SignedInfo");

                newSignedInfo.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", XMLSignature.XMLNS);
                newSignedInfo.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
                newSignedInfo.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xenc", "http://www.w3.org/2001/04/xmlenc#");

                // Copy the content of old SignedInfo to new one
                while (oldSignedInfo.hasChildNodes()) {
                    newSignedInfo.appendChild(oldSignedInfo.getFirstChild());
                }

                // Insert the new SignedInfo element in the correct position
                signatureElement.insertBefore(newSignedInfo, signatureElement.getFirstChild());
            }

            // Transform to string
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(doc), new StreamResult(sw));

            return sw.toString();

        } catch (Exception e) {
            log.error("Error during XML signing process", e);
            throw new RuntimeException("Failed to sign XML: " + e.getMessage(), e);
        }
    }

    public boolean verifySignature(String signedXml) throws Exception {
        log.info("Starting signature verification process");

        try {
            // Parse the signed XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(signedXml.getBytes("UTF-8")));

            // Find Signature element
            NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (nl.getLength() == 0) {
                throw new Exception("No XML Digital Signature Found");
            }

            // Load the keystore for verification
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                keystore.load(fis, keystorePassword.toCharArray());
            }
            X509Certificate cert = (X509Certificate) keystore.getCertificate(keyAlias);

            // Create a DOM XMLSignatureFactory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Create a DOMValidateContext
            DOMValidateContext valContext = new DOMValidateContext(cert.getPublicKey(), nl.item(0));

            // Unmarshal the XMLSignature
            XMLSignature signature = fac.unmarshalXMLSignature(valContext);

            // Validate the XMLSignature
            boolean coreValidity = signature.validate(valContext);

            if (coreValidity) {
                log.info("Signature validated successfully");
            } else {
                log.warn("Signature validation failed");
            }

            return coreValidity;

        } catch (Exception e) {
            log.error("Error during signature verification", e);
            throw new RuntimeException("Failed to verify signature: " + e.getMessage(), e);
        }
    }
}