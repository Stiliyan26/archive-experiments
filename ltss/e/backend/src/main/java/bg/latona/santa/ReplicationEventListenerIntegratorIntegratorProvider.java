package bg.latona.santa;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;

//Used only for debugging purposes
public class ReplicationEventListenerIntegratorIntegratorProvider implements IntegratorProvider {
	
	@Override
	public List<Integrator> getIntegrators() {
		LinkedList<Integrator> list = new LinkedList<Integrator>();
		list.add(new ReplicationEventListenerIntegrator());
		return list;
	}
}