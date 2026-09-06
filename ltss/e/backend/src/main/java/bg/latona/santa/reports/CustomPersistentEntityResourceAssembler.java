package bg.latona.santa.reports;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeanWrapper;
//import org.springframework.beans.BeanWrapperImpl;
//import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
//import org.springframework.data.mapping.PersistentProperty;
//import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.context.PersistentEntities;
//import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.support.SelfLinkProvider;
//import org.springframework.data.rest.webmvc.EmbeddedResourcesAssembler;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResource.Builder;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.rest.webmvc.support.Projector;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;


public class CustomPersistentEntityResourceAssembler<T> implements RepresentationModelAssembler<Object, EntityModel<T>> {

	private static Logger logger = LoggerFactory.getLogger(CustomPersistentEntityResourceAssembler.class);
	
	private MultiValueMap<String, String> params;
	private final @NonNull PersistentEntities entities;
	//private final @NonNull Projector projector;
	private final @NonNull Associations associations;
	private final @NonNull SelfLinkProvider linkProvider;
	private final @NonNull EmbeddedWrappers wrappers = new EmbeddedWrappers(false);

	public CustomPersistentEntityResourceAssembler(MultiValueMap<String, String> params, PersistentEntities entities, Associations associations, SelfLinkProvider linkProvider) {
		this.params = params;
		this.entities = entities;
		this.associations = associations;
		this.linkProvider = linkProvider;
	}
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceAssembler#toResource(java.lang.Object)
	 */
	@Override
	public EntityModel<T> toModel(Object instance) {
		Assert.notNull(instance, "Entity instance must not be null!");
		//return wrap(projector.projectExcerpt(instance), instance).build();
		Map<String,Object> entityMap = new HashMap<String,Object>();
		//build the query
		
		//TODO use the real class if needed but add it as an attribute to have proper information in the response?
		if(params != null) {
			String summaryParam = params.getFirst("summary");
			if(summaryParam != null) {
				Object entity = ((javax.persistence.Tuple) instance).get(0,((javax.persistence.Tuple) instance).getElements().get(0).getJavaType());
				if("_summaryRow".equals(entity)) {
					//the root entityType is not correct, so we assume this is the summary row
					class Summary extends HashMap<String,Object>{};
					javax.persistence.Tuple tuple = (javax.persistence.Tuple) instance;
					Summary summaryMap = new Summary();
					summaryMap.put("count(*)", tuple.get(1,Long.class));
	
					if(summaryParam != null) {
						String[] summaryDefs = summaryParam.split(","); 
						for(int summaryDefIndex = 0; summaryDefIndex < summaryDefs.length; summaryDefIndex++) {
							int colonPos = summaryDefs[summaryDefIndex].indexOf(':');
							String alias;
							if(colonPos >= 0) {
								alias = summaryDefs[summaryDefIndex].substring(colonPos+1);
							} else {
								alias = summaryDefs[summaryDefIndex];
							}
							summaryMap.put(alias,tuple.get(summaryDefIndex+2)); //+2 because of literal and count(*)
						}
					}
					
					return (EntityModel<T>) EntityModel.of(summaryMap);
				}
			}
			
			String selectParam = params.getFirst("select");
			if(selectParam != null) {
				String[] selectedEntities = selectParam.split(","); 
				for(int selectedEntityIndex = 0; selectedEntityIndex < selectedEntities.length; selectedEntityIndex++) {
	
					int colonPos = selectedEntities[selectedEntityIndex].indexOf(':');
					String alias;
					if(colonPos >= 0) {
						alias = selectedEntities[selectedEntityIndex].substring(colonPos+1);
					} else {
						alias = selectedEntities[selectedEntityIndex];
					}
	
					//System.out.println("CustomPersistentEntityResourceAssembler.toResource select param: "+selectedEntities[selectedEntityIndex]);
					Object entity = ((javax.persistence.Tuple) instance).get(selectedEntityIndex);
					if(entity != null) {
						try {
							entityMap.put(alias, wrap(entity, entity).build());
						} catch(IllegalArgumentException e) {
							entityMap.put(alias,((javax.persistence.Tuple) instance).get(selectedEntityIndex));
						}
					} else {
						entityMap.put(alias, null);
					}
				}
				return (EntityModel<T>) EntityModel.of(entityMap);
			}
			return (EntityModel<T>) EntityModel.of(wrap(instance, instance).build());
		}
		//needed for calculateOnly to return default entities
		Class claz = instance.getClass();
		entityMap.put(claz.getSimpleName(), instance);
		for(Field attr : claz.getDeclaredFields()) {
			if( !((Class) attr.getType()).isPrimitive() ) {
				attr.setAccessible(true);
				try {
					Object attrValue = attr.get(instance);
					if(attrValue != null) {
						entityMap.put(claz.getSimpleName()+"."+attr.getName(), wrap(attrValue, attrValue).build());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					//logger.warn(e.toString());
				}
			}
		}
		return (EntityModel<T>) EntityModel.of(entityMap);
	}

	/**
	 * Returns the full object as {@link PersistentEntityResource} using the underlying {@link Projector}.
	 * 
	 * @param instance must not be {@literal null}.
	 * @return
	 */
	public PersistentEntityResource toFullResource(Object instance) {
		Assert.notNull(instance, "Entity instance must not be null!");
		//return wrap(projector.project(instance), instance).build();
		return wrap(instance, instance).build();
	}

	private Builder wrap(Object instance, Object source) {

		PersistentEntity<?, ?> entity = entities.getPersistentEntity(source.getClass()).orElse(null);
		Builder result;
		result = PersistentEntityResource.build(instance, entity);
		result = result.withEmbedded(getEmbeddedResources(source));
		if( linkProvider != null ) {
			result = result.withLink(getSelfLinkFor(source)).withLink(linkProvider.createSelfLinkFor(source));
		}
		
		return result;
	}

	/**
	 * Returns the embedded resources to render. This will add an {@link RelatedResource} for linkable associations if
	 * they have an excerpt projection registered.
	 * 
	 * @param instance must not be {@literal null}.
	 * @return
	 */
	private Iterable<EmbeddedWrapper> getEmbeddedResources(Object instance) {
		//return new EmbeddedResourcesAssembler(entities, associations, projector).getEmbeddedResources(instance);
		final List<EmbeddedWrapper> associationProjections = new ArrayList<EmbeddedWrapper>();
		//TODO add the embedded resources
		//BeanWrapper wrapper = new BeanWrapperImpl(instance);
//		PersistentEntity<?, ?> entity = repositories.getPersistentEntity(instance.getClass());
//		final ResourceMetadata metadata = mappings.getMappingFor(instance.getClass());
//		entity.doWithAssociations(new SimpleAssociationHandler() {
//			@Override
//			public void doWithAssociation(Association<? extends PersistentProperty<?>> association) {
//				PersistentProperty<?> property = association.getInverse();
//				String rel = metadata.getMappingFor(property).getRel();
//				associationProjections.add(wrappers.wrap(embeddedObj, rel));
//			}
//		});
		//associationProjections.add(wrappers.wrap(wrapper.getPropertyValue(reportDef.joinPath), reportDef.joinPath));
		return associationProjections;
	}

	/**
	 * Creates the self link for the given domain instance.
	 * 
	 * @param instance must be a managed entity, not {@literal null}.
	 * @return
	 */
	public Link getSelfLinkFor(Object instance) {

		Link link = linkProvider.createSelfLinkFor(instance);
		return Link.of(link.expand().getHref(), IanaLinkRelations.SELF);
	}
}