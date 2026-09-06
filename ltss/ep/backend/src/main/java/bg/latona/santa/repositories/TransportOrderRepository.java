package bg.latona.santa.repositories;

import bg.latona.santa.entities.transport.QTransportOrder;
import bg.latona.santa.entities.transport.TransportOrder;

public interface TransportOrderRepository extends CommonRepository<TransportOrder, QTransportOrder, Long> {
	
}