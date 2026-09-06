package bg.latona.santa.selfie.service.signature;



import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import sun.security.pkcs11.SunPKCS11;


import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Collections.singletonList;



@Slf4j
@Service
public class XAdESSigner {


	private static final String XMLDSIG_FILTER2_TRANSFORM_ALGORITHM = "http://www.w3.org/2002/06/xmldsig-filter2";
	private static final String C14N_CANONICALIZATION_ALGORITHM = "http://www.w3.org/2001/10/xml-exc-c14n#";
	private static final String SIGNED_PROPERTIES_REFERENCE_TYPE = "http://uri.etsi.org/01903#SignedProperties";
	private static final String SHA256_DIGEST_ALGORITHM = "http://www.w3.org/2001/04/xmlenc#sha256";
	private static final String RSA_SHA256_SIGN_ALGORITHM = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

	private static final C14NMethodParameterSpec EMPTY_PARAMS = null;

	private static final Set<String> ID_ATTRIBUTE_NAMES = new HashSet<>(Arrays.asList("id", "Id", "ID"));

	@Value("${tsee.name}")
	private String nameTSEE;
	@Value("${tsee.library}")
	private String libraryTSEE;
	@Value("${tsee.slot}")
	private String slotTSEE;
	@Value("${tsee.identificationNumber}")
	private String identificationNumberTSEE;
	@Value("${tsee.pin}")
	private String pinTSEE;


	@Value("${tes.name}")
	private String nameTES;
	@Value("${tes.library}")
	private String libraryTES;
	@Value("${tes.slot}")
	private String slotTES;
	@Value("${tes.identificationNumber}")
	private String identificationNumberTES;
	@Value("${tes.pin}")
	private String pinTES;





	public byte[] signEnveloped(byte[] xmlContent) throws Exception {

		log.info("Start signEnveloped");
		//Todo change when FindFirstByIsSenderAndCompanyAndDeleted is ready
		String identificationNumber = "175370769";

		char[] pin = null;
		String issuerSerialV2 = "";
		String digestValue = "";
		String config = "";

		if (identificationNumber.equals(identificationNumberTSEE)) {
			if (pinTSEE.equals("empty")){
				log.info("pinTSEE is empty");
				return null;
			} else {
				pin = pinTSEE.toCharArray();
			}

			 issuerSerialV2 = "issuerSerialV2TSEE";
			 digestValue = "digestValueTSEE";

			// Configuring the Sun PKCS#11 Provider
			config = "name=" + nameTSEE + "\n" +
					"library=" + libraryTSEE + "\n" +
					"slotListIndex=" + slotTSEE;
		} else if (identificationNumber.equals(identificationNumberTES)) {

			if (pinTES.equals("empty")){
				log.info("pinTES is empty");
				return null;
			} else {
				pin = pinTES.toCharArray();
			}

			issuerSerialV2 = "issuerSerialV2TES";
			digestValue = "digestValueTES";

			// Configuring the Sun PKCS#11 Provider
			config = "name=" + nameTES + "\n" +
					"library=" + libraryTES + "\n" +
					"slotListIndex=" + slotTES;
		} else {
			return null;
		}

		// Initializing  Apache Santuario
		Init.init();



		try {

			SunPKCS11 pkcs11Provider = new SunPKCS11(new java.io.ByteArrayInputStream(config.getBytes()));
			Security.addProvider(pkcs11Provider);

			// Loading KeyStore from PKCS#11
			KeyStore keyStore = KeyStore.getInstance("PKCS11", pkcs11Provider);
			keyStore.load(null, pin);

			// Key and certificate selection
			String alias = null;
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				alias = aliases.nextElement();
				log.debug("Found alias: " + alias);
				if (keyStore.isKeyEntry(alias)) break;
			}

			if (alias == null) {
				throw new Exception("No key found on the PKCS#11 token");
			}

			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, pin);
			X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

			// Loading the XML document
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document document = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlContent));


			XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");

			String signId = String.valueOf(UUID.randomUUID()).replace("-", "");
			String signatureId = "id-" + signId;
			String signedPropertiesId = "xades-id-" + signId;


			SignedInfo signedInfo = createSignedInfo(signedPropertiesId, signatureId, document, xmlSignatureFactory);
			KeyInfo keyInfo = createKeyInfo(xmlSignatureFactory, cert);
			XMLObject qualifyingProperties = createXmlStructure(document, signatureId, issuerSerialV2, digestValue);

			XMLSignature xmlSignature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo, singletonList(qualifyingProperties),  signatureId, "value-" + signatureId);

			String prefixDefault = "ds";
			DOMSignContext domSignContext = createDomSignContext(document, prefixDefault, privateKey);
			domSignContext.putNamespacePrefix(XMLDSIG_FILTER2_TRANSFORM_ALGORITHM,"dsig-filter2");

			xmlSignature.sign(domSignContext);

			Security.removeProvider(pkcs11Provider.getName());


			// Convert signed document to byte array
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);

			log.info("End signEnveloped");
			return outputStream.toByteArray();
		} catch (Exception e) {
			log.error("signEnveloped error message - " + e.getMessage());
			return null;
		}
	}


	private XMLObject createXmlStructure(Document ownerDocument, String signatureId, String issuerSerialV2Content, String digestValueContent) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.newDocument();

		// Creating the QualifyingProperties root element
		Element qualifyingProperties = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:QualifyingProperties");
		qualifyingProperties.setAttribute("xmlns:xades", "http://uri.etsi.org/01903/v1.3.2#");
		qualifyingProperties.setAttribute("Target", "#" + signatureId);

		// Add SignedProperties
		Element signedProperties = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:SignedProperties");
		signedProperties.setAttribute("Id", "xades-" + signatureId);

		// Add SignedSignatureProperties
		Element signedSignatureProperties = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:SignedSignatureProperties");

		// Add SigningTime
		Element signingTime = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:SigningTime");
		signingTime.setTextContent(currentTime().toString());
		signedSignatureProperties.appendChild(signingTime);

		// Add SigningCertificateV2
		Element signingCertificateV2 = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:SigningCertificateV2");

		// Add Cert
		Element cert = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:Cert");

		// Add CertDigest
		Element certDigest = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:CertDigest");

		// Add DigestMethod
		Element digestMethod = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:DigestMethod");
		digestMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha512");

		// Add DigestValue
		Element digestValue = doc.createElementNS("http://www.w3.org/2000/09/xmldsig#", "ds:DigestValue");
		digestValue.setTextContent(digestValueContent);
		certDigest.appendChild(digestMethod);
		certDigest.appendChild(digestValue);
		cert.appendChild(certDigest);

		// Add IssuerSerialV2
		Element issuerSerialV2 = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:IssuerSerialV2");
		issuerSerialV2.setTextContent(issuerSerialV2Content);
		cert.appendChild(issuerSerialV2);

		// Adding Cert to SigningCertificateV2
		signingCertificateV2.appendChild(cert);
		signedSignatureProperties.appendChild(signingCertificateV2);

		// Add SignedDataObjectProperties
		Element signedDataObjectProperties = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:SignedDataObjectProperties");

		// Add DataObjectFormat
		Element dataObjectFormat = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:DataObjectFormat");
		dataObjectFormat.setAttribute("ObjectReference", "#r-"+ signatureId + "-1");

		// Add MimeType
		Element mimeType = doc.createElementNS("http://uri.etsi.org/01903/v1.3.2#", "xades:MimeType");
		mimeType.setTextContent("application/octet-stream");
		dataObjectFormat.appendChild(mimeType);
		signedDataObjectProperties.appendChild(dataObjectFormat);

		signedProperties.appendChild(signedSignatureProperties);

		signedProperties.appendChild(signedDataObjectProperties);

		qualifyingProperties.appendChild(signedProperties);

		Node importedQualifyingProperties = ownerDocument.importNode(qualifyingProperties, true);

		markIdsRecursively(importedQualifyingProperties.getChildNodes());

		DOMStructure qualifyingPropertiesObject = new DOMStructure(importedQualifyingProperties);
		return XMLSignatureFactory.getInstance("DOM").newXMLObject(Collections.singletonList(qualifyingPropertiesObject), null, null, null);
	}


	private SignedInfo createSignedInfo(String signedPropertiesId, String signatureId, Document document, XMLSignatureFactory xmlSignatureFactory) throws Exception {

		CanonicalizationMethod c14nMethod = xmlSignatureFactory.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#", EMPTY_PARAMS);

		SignatureMethod signMethod = xmlSignatureFactory.newSignatureMethod(RSA_SHA256_SIGN_ALGORITHM, (SignatureMethodParameterSpec) EMPTY_PARAMS);

		List<Reference> references = Arrays.asList(
				createSignedDocumentReference(signatureId,  xmlSignatureFactory),
				createSignedPropertiesReference(signedPropertiesId, xmlSignatureFactory)
		);

		return xmlSignatureFactory.newSignedInfo(c14nMethod, signMethod, references);
	}

	private Reference createSignedDocumentReference(String signatureId, XMLSignatureFactory xmlSignatureFactory) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, ParserConfigurationException, XPathExpressionException, NoSuchProviderException {

		DigestMethod digestMethod = xmlSignatureFactory.newDigestMethod(SHA256_DIGEST_ALGORITHM, (DigestMethodParameterSpec) EMPTY_PARAMS);

		XPathType xPath = new XPathType("/descendant::ds:Signature", XPathType.Filter.SUBTRACT);
		XPathFilter2ParameterSpec filter2Params = new XPathFilter2ParameterSpec(Collections.singletonList(xPath));

		XMLSignatureFactory xmlSignatureFactorySec = XMLSignatureFactory.getInstance("DOM");
		Transform envelopedSignatureTransform = xmlSignatureFactorySec.newTransform("http://www.w3.org/2002/06/xmldsig-filter2",  filter2Params);

		Transform c14nWithCommentsTransform = xmlSignatureFactory.newTransform(C14N_CANONICALIZATION_ALGORITHM, EMPTY_PARAMS);

		List<Transform> transforms = Arrays.asList(envelopedSignatureTransform, c14nWithCommentsTransform);

		Reference reference = xmlSignatureFactory.newReference("", digestMethod, transforms, null, "r-" + signatureId + "-1");

		return reference;
	}


	//<ds:Reference Type="http://uri.etsi.org/01903#SignedProperties" URI="#xades-id-b5c031fcb6919cc459c4f3e592065c9d">
	private Reference createSignedPropertiesReference(String signedPropertiesId, XMLSignatureFactory xmlSignatureFactory) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		String uri = "#" + signedPropertiesId;
		DigestMethod digestMethod = xmlSignatureFactory.newDigestMethod(SHA256_DIGEST_ALGORITHM, (DigestMethodParameterSpec) EMPTY_PARAMS);
		Transform c14nWithCommentsTransform = xmlSignatureFactory.newTransform(C14N_CANONICALIZATION_ALGORITHM, EMPTY_PARAMS);

		List<Transform> transforms = singletonList(c14nWithCommentsTransform);

		return xmlSignatureFactory.newReference(uri, digestMethod, transforms, SIGNED_PROPERTIES_REFERENCE_TYPE, null);
	}

	private KeyInfo createKeyInfo(XMLSignatureFactory xmlSignatureFactory, X509Certificate cert) {
		KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
		X509Data x509Data = keyInfoFactory.newX509Data(singletonList(cert));
		return keyInfoFactory.newKeyInfo(singletonList(x509Data));
	}



	private DOMSignContext createDomSignContext(Document document, String prefix, PrivateKey privateKey) throws TransformerException, FileNotFoundException {

		Element rootNode = document.getDocumentElement();
		DOMSignContext domSignContext = new DOMSignContext(privateKey, rootNode);

		if (!prefix.isEmpty() ) {
			domSignContext.setDefaultNamespacePrefix(prefix);
		}

		return domSignContext;
	}



	private XMLGregorianCalendar currentTime() {
		try {
			ZonedDateTime now = ZonedDateTime.now().withZoneSameInstant(java.time.ZoneOffset.UTC);
			GregorianCalendar gregorianCalendar = GregorianCalendar.from(now);
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	private void markIdsRecursively(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node item = nodeList.item(i);

			if (item instanceof Element) {
				Element element = (Element) item;

				// Check for ID attributes and tagging
				for (String idAttributeName : ID_ATTRIBUTE_NAMES) {
					if (element.hasAttribute(idAttributeName)) {
						element.setIdAttribute(idAttributeName, true);
					}
				}
			}

			// Recursive Invocation for Child Nodes
			markIdsRecursively(item.getChildNodes());
		}
	}


}