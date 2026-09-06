package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.QVendorInvoiceRow;
import bg.latona.santa.entities.santa.common.VendorInvoiceRow;

public interface VendorInvoiceRowRepository extends CommonRepository<VendorInvoiceRow, QVendorInvoiceRow, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<VendorInvoiceRow> findByGoodsAndCompanyAndDeleted(CGoods goods, ManagedCompany company, boolean deleted);

}