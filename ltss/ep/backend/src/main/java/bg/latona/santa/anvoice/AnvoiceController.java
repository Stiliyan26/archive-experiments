package bg.latona.santa.anvoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/anvoice")
public class AnvoiceController {

	private static final Logger logger = LoggerFactory.getLogger(AnvoiceController.class);

	AnvoiceProcedures anvoiceProcedures;

	@Autowired
	AnvoiceController(AnvoiceProcedures anvoiceProcedures) {
		this.anvoiceProcedures = anvoiceProcedures;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/xenergieInvoices")
	@ResponseBody
	public List<Map<String, Object>> getXenergieInvoices() {
		return anvoiceProcedures.getXenergieInvoices();
	}

    @PostMapping(value = "/parseXenergieInvoicesFile/{dbFileId}")
    @ResponseBody
    Map<Integer, List<String>> parseXenergieInvoicesFile(@PathVariable Long dbFileId) {
        return anvoiceProcedures.parseXenergieInvoicesFile(dbFileId); // If all rows are with valid data: an empty Map will be returned
    }

    @GetMapping(value = "/populateElectricityInvoice")
    public ResponseEntity<Void> populateElectricityInvoice(
            @RequestParam("periodFrom") String periodFromStr,
            @RequestParam("periodTo") String periodToStr,
            @RequestParam("taxEventDate") String taxEventDateStr,
            @RequestParam("documentType") String documentType
    ) {
		logger.info("Call to populateElectricityInvoice with params "+periodFromStr+"; "+periodToStr+"; "+taxEventDateStr+"; "+documentType);
        // Parse the Data coming from the GET request URL
        LocalDate periodFrom = anvoiceProcedures.getLocalDateFromString(periodFromStr);
        LocalDate periodTo = anvoiceProcedures.getLocalDateFromString(periodToStr);
        LocalDate taxEventDate = anvoiceProcedures.getLocalDateFromString(taxEventDateStr);

		anvoiceProcedures.populateElectricityInvoice(periodFrom, periodTo, taxEventDate, 
				documentType.equals("invoice") ? 1L 
				: (documentType.equals("creditNote") ? 2L : 3L));

        return ResponseEntity.ok().build();
    }
}
