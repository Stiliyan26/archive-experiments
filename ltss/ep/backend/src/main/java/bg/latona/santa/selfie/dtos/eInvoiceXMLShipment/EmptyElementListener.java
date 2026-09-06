package bg.latona.santa.selfie.dtos.eInvoiceXMLShipment;

import javax.xml.bind.Marshaller;

public class EmptyElementListener extends Marshaller.Listener {

	@Override
	public void beforeMarshal(Object source) {
		if (source instanceof AddressDTO) {
			AddressDTO obj = (AddressDTO) source;
			if (obj.getPhone() == null) {
				obj.setPhone(""); // Задаваме празен низ за генериране на <Phone/>
			}
			if (obj.getFax() == null) {
				obj.setFax("");
			}
			if (obj.getZip() == null) {
				obj.setZip("");
			}
		}
	}
}
