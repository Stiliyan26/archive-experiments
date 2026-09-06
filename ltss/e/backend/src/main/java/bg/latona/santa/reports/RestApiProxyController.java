package bg.latona.santa.reports;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/service")
public class RestApiProxyController {

	@Value("${santa.services.SCHEME}")
	private String SCHEME;
	@Value("${santa.services.SERVER}")
	private String SERVER;
	@Value("${santa.services.PORT}")
	private int PORT;
	@Value("${santa.services.ORDER_PROCESS_URL}")
	private String ORDER_PROCESS_URL;
	@Value("${santa.services.ORDER_PROCESS_BASIC_AUTH}")
	private String ORDER_PROCESS_BASIC_AUTH;
	@Value("${santa.services.BANK_FILE_PARSER_URL}")
	private String BANK_FILE_PARSER_URL;
	@Value("${santa.services.BANK_FILE_PARSER_BASIC_AUTH}")
	private String BANK_FILE_PARSER_BASIC_AUTH;
	@Value("${santa.services.PAYMENT_MATCHER_URL}")
	private String PAYMENT_MATCHER_URL;
	@Value("${santa.services.PAYMENT_MATCHER_BASIC_AUTH}")
	private String PAYMENT_MATCHER_BASIC_AUTH;
	
	private static Logger logger = LoggerFactory.getLogger(RestApiProxyController.class);

	public RestApiProxyController() {
		// TODO Auto-generated constructor stub
	}
	
	public RestTemplate naiveRestTemplate() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
						.loadTrustMaterial(null, acceptingTrustStrategy)
						.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		CloseableHttpClient httpClient = HttpClients.custom()
						.setSSLSocketFactory(csf)
						.build();

		HttpComponentsClientHttpRequestFactory requestFactory =
						new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		return restTemplate;
	}

	@RequestMapping(method = RequestMethod.POST, path = "/orderProcessing", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity orderProcessing(@RequestParam String doctype, @RequestPart MultipartFile file)
	{
//		logger.error("doctype: {} ", doctype);
//		logger.error("file: {} ", file);
		try {
			URI uri = new URI(SCHEME, null, SERVER, PORT, null, null, null);
			uri = UriComponentsBuilder.fromUri(uri)
				.path(ORDER_PROCESS_URL)
				.build(true).toUri();
	
			RestTemplate restTemplate;
			try {
				restTemplate = naiveRestTemplate();
	
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "*/*");
				headers.set("Authorization", "Basic "+ORDER_PROCESS_BASIC_AUTH);
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				
				MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
				body.add("file", file.getResource());
				body.add("doctype", doctype);
				
				HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
				
//				logger.error("url: {} ", uri);
//				logger.error("headers: {} ", headers);
		
				try {
					ResponseEntity result = restTemplate.postForEntity(uri, httpEntity, String.class);
//					logger.error("result: {} ", result);
					return (new ResponseEntity(result.getBody(),result.getStatusCode()));
				} catch(HttpStatusCodeException e) {
					logger.error(e.getMessage());
					return ResponseEntity.status(e.getRawStatusCode())
										.headers(e.getResponseHeaders())
										.body(e.getResponseBodyAsString());
				}
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
				logger.error(e1.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						//.headers(e.getResponseHeaders())
						.body(e1.getMessage());
			}
		} catch(URISyntaxException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								//.headers(e.getResponseHeaders())
								.body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/bankFile", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity bankFile(@RequestParam String bankName, @RequestPart MultipartFile file)
	{
	//	logger.error("doctype: {} ", doctype);
	//	logger.error("file: {} ", file);
		try {
			URI uri = new URI(SCHEME, null, SERVER, PORT, null, null, null);
			uri = UriComponentsBuilder.fromUri(uri)
				.path(BANK_FILE_PARSER_URL)
				.build(true).toUri();
	
			RestTemplate restTemplate;
			try {
				restTemplate = naiveRestTemplate();
	
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "*/*");
				headers.set("Authorization", "Basic "+BANK_FILE_PARSER_BASIC_AUTH);
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				
				MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
				body.add("file", file.getResource());
				body.add("bankName", bankName);
				
				HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, headers);
				
	//			logger.error("url: {} ", uri);
	//			logger.error("headers: {} ", headers);
		
				try {
					ResponseEntity result = restTemplate.postForEntity(uri, httpEntity, String.class);
					//logger.error("result: {} ", result);
					return ResponseEntity.status(result.getStatusCode())
							//.headers(result.getHeaders())
							.body(result.getBody());
					//return (new ResponseEntity(result.getBody(),result.getStatusCode()));
				} catch(HttpStatusCodeException e) {
					logger.error(e.getMessage());
					return ResponseEntity.status(e.getRawStatusCode())
										.headers(e.getResponseHeaders())
										.body(e.getResponseBodyAsString());
				}
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
				logger.error(e1.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						//.headers(e.getResponseHeaders())
						.body(e1.getMessage());
			}
		} catch(URISyntaxException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								//.headers(e.getResponseHeaders())
								.body(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.POST, path = "/paymentMatcher", consumes = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity paymentMatcher(@RequestBody String data)
	{
		try {
			URI uri = new URI(SCHEME, null, SERVER, PORT, null, null, null);
			uri = UriComponentsBuilder.fromUri(uri)
				.path(PAYMENT_MATCHER_URL)
				.build(true).toUri();
	
			RestTemplate restTemplate;
			try {
				restTemplate = naiveRestTemplate();
	
				HttpHeaders headers = new HttpHeaders();
				headers.set("Accept", "*/*");
				headers.set("Authorization", "Basic "+PAYMENT_MATCHER_BASIC_AUTH);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
	//			logger.error("url: {} ", uri);
	//			logger.error("headers: {} ", headers);
				logger.error("data: {} ", data);
				
				HttpEntity<String> httpEntity = new HttpEntity<String>(data, headers);
		
				try {
					ResponseEntity result = restTemplate.postForEntity(uri, httpEntity, String.class);
					logger.error("result: {} ", result);
					return ResponseEntity.status(result.getStatusCode())
							//.headers(result.getHeaders())
							.body(result.getBody());
					//return (new ResponseEntity(result.getBody(),result.getStatusCode()));
				} catch(HttpStatusCodeException e) {
					logger.error(e.getMessage());
					return ResponseEntity.status(e.getRawStatusCode())
										.headers(e.getResponseHeaders())
										.body(e.getResponseBodyAsString());
				}
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e1) {
				logger.error(e1.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						//.headers(e.getResponseHeaders())
						.body(e1.getMessage());
			}
		} catch(URISyntaxException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
								//.headers(e.getResponseHeaders())
								.body(e.getMessage());
		}
	}
}
