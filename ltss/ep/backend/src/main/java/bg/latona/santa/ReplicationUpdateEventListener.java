package bg.latona.santa;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Used only for debugging purposes
public class ReplicationUpdateEventListener implements PostUpdateEventListener {
	private static final long serialVersionUID = -7538317372106332064L;
	public static final ReplicationUpdateEventListener INSTANCE = new ReplicationUpdateEventListener();
	private static Logger logger = LoggerFactory.getLogger(ReplicationUpdateEventListener.class);
	
	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		final Object entity = event.getEntity();
		logger.info("onPostUpdate object: "+entity);
		Thread.dumpStack();
	}
	
	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}
}