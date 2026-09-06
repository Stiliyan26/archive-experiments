package bg.latona.santa.repositories;

import bg.latona.santa.entities.person.QCustomer;
import bg.latona.santa.entities.person.Customer;

public interface CustomerRepository extends CommonRepository<Customer, QCustomer, Long> {

}