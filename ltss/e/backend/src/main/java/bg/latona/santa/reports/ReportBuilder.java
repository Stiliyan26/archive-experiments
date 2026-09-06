package bg.latona.santa.reports;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.transaction.Transactional;

import org.hibernate.MappingException;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.query.criteria.internal.BasicPathUsageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.FieldUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.security.AccessControl;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;

@Repository
@Transactional
public class ReportBuilder {

	private static Logger logger = LoggerFactory.getLogger(ReportBuilder.class);
	@PersistenceContext
	private EntityManager entityManager; //USING HQL
	@Autowired
	private WebApplicationContext appContext;
	@Autowired
	private RepositoryResourceMappings mappings;
	
	private class NativeQuery {
		List<NativeOrder> orderByList;
		List<NativeSelection> selectionList;
		NativeBuilder builder;
		NativeRoot root;
		NativeExpression where;
		List<NativeExpression> groupBy;
		NativeExpression having;
		public NativeQuery(NativeBuilder builder) {
			this.builder = builder;
		}
		public NativeRoot from(Class entityClass) {
			this.root = new NativeRoot(builder, entityClass);
			return this.root;
		}
		public NativeQuery where(NativeExpression restriction) {
			this.where = restriction;
			return this;
		}
		public NativeQuery groupBy(List<NativeExpression> grouping) {
			this.groupBy = grouping;
			return this;
		}
		public NativeQuery having(NativeExpression restriction) {
			this.having = restriction;
			return this;
		}
		public NativeSubquery subquery(Class type) {
			return new NativeSubquery(builder);
		}
		public NativeQuery select(NativeSelection selection) {
			this.selectionList = new LinkedList<NativeSelection>();
			this.selectionList.add(selection);
			return this;
		}
		public NativeQuery multiselect(java.util.List<NativeSelection> selectionList) {
			this.selectionList = selectionList;
			return this;
		}
		public NativeQuery orderBy(java.util.List<NativeOrder> o) {
			this.orderByList = o;
			return this;
		}
		public void build(StringBuilder stringBuilder) {
			if(selectionList.size()>0) {
				stringBuilder.append(" SELECT ");
				Iterator<NativeSelection> it = selectionList.iterator();
				NativeSelection selection = it.next();
				selection.build(stringBuilder);
				while(it.hasNext()) {
					stringBuilder.append(',');
					selection = it.next();
					selection.build(stringBuilder);
				}
			}
			root.build(stringBuilder);
			if(where != null) {
				stringBuilder.append("\n WHERE ");
				where.build(stringBuilder);
			}
			if(groupBy != null && groupBy.size()>0) {
				stringBuilder.append("\n GROUP BY ");
				Iterator<NativeExpression> it = groupBy.iterator();
				it.next().build(stringBuilder);
				while(it.hasNext()) {
					stringBuilder.append(',');
					it.next().build(stringBuilder);
				}
			}
			if(having != null) {
				stringBuilder.append("\n HAVING ");
				having.build(stringBuilder);
			}
			if(orderByList != null && orderByList.size()>0) {
				stringBuilder.append("\n ORDER BY ");
				Iterator<NativeOrder> it = orderByList.iterator();
				it.next().build(stringBuilder);
				while(it.hasNext()) {
					stringBuilder.append(',');
					it.next().build(stringBuilder);
				}
			}
		}
	}
	private class NativeSubquery extends NativeQuery {
		public NativeSubquery(NativeBuilder builder) {
			super(builder);
		}
	}
	private class NativeBuilder {
		public NativeParameterExpression parameter(Class clazz, String name) {
			return new NativeParameterExpression(this, clazz, name);
		}
		public NativePredicate equal(NativeExpression x, NativeExpression y) {
			return new NativeXFYPredicate(this, x,"=",y);
		}
		public NativePredicate isNull(NativeExpression x) {
			return new NativeXFPredicate(this, x,"IS NULL");
		}
		public NativePredicate like(NativeExpression x, NativeExpression pattern) {
			return new NativeXFYPredicate(this, x,"LIKE",pattern);
		}
		public NativePredicate between(NativeExpression v, NativeExpression x, NativeExpression y) {
			return new NativeVFXFYPredicate(this, v,"BETWEEN",x,"AND",y);
		}
		public NativePredicate and(NativeExpression x, NativeExpression y) {
			return new NativeXFYPredicate(this, x,"AND",y);
		}
		public NativePredicate and(NativePredicate... restrictions) {
			NativePredicate restriction = restrictions[0];
			for(int it=1; it<restrictions.length; it++) {
				restriction = and(restriction,restrictions[it]);
			}
			return restriction;
		}
		public NativePredicate or(NativeExpression x, NativeExpression y) {
			return new NativeXFYPredicate(this, x,"OR",y);
		}
		public NativePredicate or(NativePredicate... restrictions) {
			NativePredicate restriction = restrictions[0];
			for(int it=1; it<restrictions.length; it++) {
				restriction = or(restriction,restrictions[it]);
			}
			return restriction;
		}
		public NativePredicate exists(NativeSubquery subquery) {
			return new NativeFSPredicate(this, "EXISTS",subquery);
		}
		public NativePredicate not(NativeExpression expr) {
			return new NativeFXPredicate(this, "NOT",expr);
		}
		public NativeExpression starExpression(NativePath path) {
			return new NativeStarExpression(this, path);
		}
		public NativeExpression upper(NativeExpression x) {
			return new NativeSingleParamFunctionExpression(this, "UPPER", x);
		}
		public NativeOrder asc(NativeExpression x) {
			return new NativeOrder(x,true);
		}
		public NativeOrder desc(NativeExpression x) {
			return new NativeOrder(x,false);
		}
		public NativeExpression sum(NativeExpression nativeExpression) {
			return new NativeSingleParamFunctionExpression(this, "SUM",nativeExpression);
		}
		public NativeExpression sum(NativeExpression x, NativeExpression y) {
			return new NativeXFYPredicate(this, x,"+",y);
		}
		public NativeExpression count(NativeExpression nativeExpression) {
			return new NativeSingleParamFunctionExpression(this, "COUNT",nativeExpression);
		}
		public NativeExpression avg(NativeExpression nativeExpression) {
			return new NativeSingleParamFunctionExpression(this, "AVG",nativeExpression);
		}
		public NativeExpression min(NativeExpression nativeExpression) {
			return new NativeSingleParamFunctionExpression(this, "MIN",nativeExpression);
		}
		public NativeExpression max(NativeExpression nativeExpression) {
			return new NativeSingleParamFunctionExpression(this, "MAX",nativeExpression);
		}
		public NativeExpression quot(NativeExpression nativeExpression, NativeExpression nativeExpression2) {
			return new NativeSingleParamFunctionExpression(this, "/",nativeExpression);
		}
		public NativeExpression prod(NativeExpression nativeExpression, NativeExpression nativeExpression2) {
			return new NativeSingleParamFunctionExpression(this, "*",nativeExpression);
		}
		public NativeExpression diff(NativeExpression x, NativeExpression y) {
			return new NativeXFYPredicate(this, x,"-",y);
		}
		public NativeQuery createNativeQuery() {
			return new NativeQuery(this);
		}
	}
	private interface NativeSelection {
		public NativeBuilder getBuilder();
		public String getAlias();
		public void buildRef(StringBuilder stringBuilder);
		public void build(StringBuilder stringBuilder);
	}
	private abstract class NativeExpression implements NativeSelection {
		private Class clazz;
		private NativeBuilder builder;
		String alias;
		public NativeExpression(NativeBuilder builder) {
			this.builder = builder;
		}
		@Override
		public NativeBuilder getBuilder() {
			return builder;
		}
		@Override
		public String getAlias() {
			return alias;
		}
		public Class getJavaType() {
			return clazz;
		}
		public Class getClazz() {
			return clazz;
		}
		public void setClazz(Class clazz) {
			this.clazz = clazz;
		}
		@Override
		public void buildRef(StringBuilder stringBuilder) {
			if(alias != null) {
				stringBuilder.append(alias);
			} else {
				build(stringBuilder);
			}
		}
	}
	private class NativeStarExpression extends NativeExpression {
		NativePath path;
		public NativeStarExpression(NativeBuilder builder, NativePath path) {
			super(builder);
			this.path = path;
			this.alias = "*";
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			if(this.path != null) {
				this.path.buildRef(stringBuilder);
				stringBuilder.append('.');
			}
			stringBuilder.append(this.alias);
		}
	}
	private class NativeParameterExpression extends NativeExpression {
		String name;
		public NativeParameterExpression(NativeBuilder builder, Class clazz, String name) {
			super(builder);
			setClazz(clazz);
			this.name = name;
		}
		public String getName() { return name; }
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append(" :").append(name);
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativeSingleParamFunctionExpression extends NativeExpression {
		String function;
		NativeExpression expression;
		public NativeSingleParamFunctionExpression(NativeBuilder builder, String function, NativeExpression expression) {
			super(builder);
			this.function = function;
			this.expression = expression;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append(' ').append(function).append('(');
			expression.build(stringBuilder);
			stringBuilder.append(')');
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativePath extends NativeExpression {
		NativePath path;
		String attribute;
		Attribute attrObject;
		String pathJoinsNumbers = "";
		String columnName;
		public NativePath(NativeBuilder builder, NativePath path, String attribute, Class clazz) {
			super(builder);
			setPathAndAttribute(path, attribute);
			setClazz(clazz);
		}
		public NativePath(NativeBuilder builder, NativePath path, String attribute) {
			super(builder);
			setPathAndAttribute(path, attribute);
			Class clazz;
			if(attrObject != null && attrObject.isCollection()) {
				clazz = ((PluralAttribute) attrObject).getElementType().getJavaType();
			} else {
				clazz = attrObject.getJavaType();
			}
			setClazz(clazz);
		}
		public void setPathAndAttribute(NativePath path, String attribute) {
			this.path = path;
			this.attribute = attribute;
			if(path != null) {
				logger.trace("setPathAndAttribute: "+path.getClazz().toString()+" "+attribute);
				try {
					this.attrObject = entityManager.getMetamodel().entity(path.getClazz()).getAttribute(attribute);
				} catch (IllegalArgumentException e) {
					Set<Attribute> temp = entityManager.getMetamodel().entity(path.getClazz()).getAttributes();
					logger.trace("IllegalArgumentException context: ");
					for(Attribute attr: temp) {
						logger.trace("- attribute: "+attr.getName());
					}
					throw e;
				}
				//get column name
				SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
				EntityPersister entityPersister = sessionFactory.getEntityPersister( path.getClazz().getName() );
				if (entityPersister instanceof AbstractEntityPersister) {
					AbstractEntityPersister persisterImpl = (AbstractEntityPersister) entityPersister;
					//traceAbstractEntityPersister(persisterImpl,attribute);
					this.columnName = persisterImpl.getPropertyColumnNames(attribute)[0];
				} else {
					throw new RuntimeException("Unexpected persister type; a subtype of AbstractEntityPersister expected.");
				}
			}
		}
		public NativePath getId() {
			return new NativePath(getBuilder(), this, "id");
		}
		public NativePath get(String attributeName) {
			return new NativePath(getBuilder(), this, attributeName);
		}
		public NativePath get(SingularAttribute attr) {
			return get(attr.getName());
		}
		public NativeExpression type() {
			return get("dtype");
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			if(path != null) {
				path.buildRef(stringBuilder);
				stringBuilder.append('.');
			}
			stringBuilder.append(columnName);
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
			//stringBuilder.append(" --path ").append(alias).append("\n");
		}
	}
	private class NativeTableExpression extends NativeExpression {
		String tableDefinition;
		NativeSubquery subquery;
		public NativeTableExpression(NativeBuilder builder, Class clazz) {
			super(builder);
			SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
			EntityPersister entityPersister = sessionFactory.getEntityPersister( clazz.getName() );
			if (entityPersister instanceof AbstractEntityPersister) {
				AbstractEntityPersister persisterImpl = (AbstractEntityPersister) entityPersister;
				tableDefinition = persisterImpl.getTableName();
			} else {
				throw new RuntimeException("Unexpected persister type; a subtype of AbstractEntityPersister expected.");
			}
		}
		public void build(StringBuilder stringBuilder) {
			if(subquery != null) {
				stringBuilder.append(" (");
				subquery.build(stringBuilder);
				stringBuilder.append(")");
			} else {
				stringBuilder.append(tableDefinition);
			}
		}
	}
	private class NativeFrom extends NativePath {
		List<NativeJoin> joins = new LinkedList<NativeJoin>();
		NativeTableExpression tableExpr;
		public NativeFrom(NativeBuilder builder, Class clazz) {
			super(builder, null, "", clazz);
			setClazz(clazz);
			if(alias == null) {
				alias = attribute + this.pathJoinsNumbers;
			}
		}
		public NativeFrom(NativeBuilder builder, NativeFrom path, String attribute) {
			super(builder, path, attribute);
			if(alias == null) {
				alias = attribute + this.pathJoinsNumbers;
			}
		}
		@Override
		public void setClazz(Class clazz) {
			super.setClazz(clazz);
			this.tableExpr = new NativeTableExpression(this.getBuilder(), clazz);
		}
		@Override
		public NativePath get(String attributeName) {
			NativePath result = super.get(attributeName);
			//check if we need to make subquery for superclass attribute
			Metamodel metamodel = entityManager.getMetamodel();
			try {
				EntityType entityType = metamodel.entity(getClazz());
				logger.trace("Checking class "+entityType.getName()+" for attribute "+attributeName);
				entityType.getDeclaredAttribute(attributeName); //exception if it is not in this entity
				//no exception, so we don't need superclass for it
//				if(tableExpr.subquery != null) {
//					//if we have a subquery, the attribute should be selected
//					logger.trace("Adding attribute "+attributeName+" to select of subquery for "+getClazz().getSimpleName());
//					List<NativeSelection> selectList = tableExpr.subquery.selectionList;
//					selectList.add(tableExpr.subquery.root.get(attributeName));
//					tableExpr.subquery.multiselect(selectList);
//				}
			} catch(IllegalArgumentException e) {
				//exception, so attribute is in some superclass
				try {
					EntityType entityType = metamodel.entity(getClazz().getSuperclass()); //exception if superclass is not entity
					logger.debug("Attribute "+attributeName+" is in the superclass "+getClazz().getSuperclass().getSimpleName()+" of class "+getClazz().getSimpleName());
					//check if we have subquery
					if(tableExpr.subquery == null) {
						//create subquery
						tableExpr.subquery = new NativeSubquery(this.getBuilder());
						NativeRoot root = tableExpr.subquery.from(getClazz());
						NativeJoin superClass = root.joinWithCustomOnClause(getClazz().getSuperclass(), JoinType.INNER);
						superClass.on(this.getBuilder().equal(root.getId(), superClass.getId()));
						tableExpr.subquery.select(getBuilder().starExpression(root));
					}
					//if we have a subquery, the attribute should be selected
					NativeJoin superClassJoin = tableExpr.subquery.root.joins.get(0);
					logger.trace("Adding attribute "+attributeName+" to select of subquery for "+getClazz().getSuperclass().getSimpleName());
					List<NativeSelection> selectList = tableExpr.subquery.selectionList;
					selectList.add(superClassJoin.get(attributeName));
					tableExpr.subquery.multiselect(selectList);
				} catch(IllegalArgumentException ex) {
					logger.trace("Class "+getClazz().getSimpleName()+" does not declare attribute "+attributeName+" but superclass is not entity or it also doesn't declare it: "+getClazz().getSuperclass().getSimpleName());
//					if(tableExpr.subquery != null) {
//						//if we have a subquery, the attribute should be selected
//						logger.trace("Adding attribute "+attributeName+" to select of subquery for "+getClazz().getSimpleName());
//						List<NativeSelection> selectList = tableExpr.subquery.selectionList;
//						selectList.add(tableExpr.subquery.root.get(attributeName));
//						tableExpr.subquery.multiselect(selectList);
//					}
				}
			}
			return result;
		}
		public NativeJoin join(String attribute, JoinType type) throws NoSuchFieldException {
			NativeJoin newJoin = new NativeJoin(getBuilder(), this, attribute, type);
			this.joins.add(newJoin);
			return newJoin;
		}
		public NativeJoin joinWithCustomOnClause(Class clazz, JoinType type) {
			NativeJoin newJoin = new NativeJoin(getBuilder(), clazz, type);
			this.joins.add(newJoin);
			return newJoin;
		}
	}
	private class NativeRoot extends NativeFrom {
		public NativeRoot(NativeBuilder builder, Class clazz) {
			super(builder, clazz);
			this.attribute = clazz.getSimpleName();
			this.pathJoinsNumbers = "0";
			this.alias = this.attribute + this.pathJoinsNumbers;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append("\n FROM ");
			tableExpr.build(stringBuilder);
			stringBuilder.append(" AS ").append(alias);
			if(joins.size()>0) {
				for(NativeJoin join : joins) {
					join.build(stringBuilder);
				}
			}
			//stringBuilder.append(" --root ").append(alias).append("\n");
		}
	}
	
	private class NativeJoin extends NativeFrom {
		JoinType type;
		NativeExpression on;
		private NativeJoin(NativeBuilder builder, Class clazz, JoinType type) {
			super(builder, clazz);
			this.type = type;
			this.attribute = clazz.getSimpleName();
			this.pathJoinsNumbers = "0";
			this.alias = this.attribute + this.pathJoinsNumbers;
		}
		private NativeJoin(NativeBuilder builder, NativeFrom path, String attribute, JoinType type) throws NoSuchFieldException {
			super(builder, path, attribute);
			if(this.attrObject == null || !this.attrObject.isAssociation() && !this.attrObject.isCollection()) {
				throw new BasicPathUsageException("BasicPathUsageException while joining "+attribute+" to "+path.alias, attrObject);
			}
			this.type = type;
			this.pathJoinsNumbers = path.pathJoinsNumbers + "_" + path.joins.size();
			this.alias = attribute + this.pathJoinsNumbers;
			if(attrObject != null && attrObject.isCollection()) {
				Class cls = path.getClazz();
				try {
					String mappedBy = FieldUtils.getField(cls, attribute).getAnnotation(OneToMany.class).mappedBy();
					logger.trace("Joining collection "+attribute+" mapped by "+mappedBy);
					this.on = getBuilder().equal(path.getId(), this.get(mappedBy));
				} catch (IllegalStateException e) {
					logger.error("IllegalStateException while joining collection "+e.getMessage()+" for class "+cls.getSimpleName()+" by field "+attribute);
					throw e;
				}
			} else if(attrObject != null && attrObject.isAssociation()) {
				logger.trace("Joining association "+attribute);
				this.on = getBuilder().equal(path.get(attribute), this.getId());
			} else {
				logger.error("Required join attribute is neither collection nor association");
			}
		}
		public NativeJoin on(NativeExpression restriction) {
			if(this.on != null) {
				this.on = getBuilder().and(this.on, restriction);
			} else {
				this.on = restriction;
			}
			return this;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			if(type == JoinType.INNER) { stringBuilder.append("\n INNER JOIN "); }
			else if(type == JoinType.LEFT) { stringBuilder.append("\n LEFT JOIN "); }
			else if(type == JoinType.RIGHT) stringBuilder.append("\n RIGHT JOIN ");
			tableExpr.build(stringBuilder);
			stringBuilder.append(" AS ").append(alias);
			if(on != null) {
				stringBuilder.append(" ON ");
				on.build(stringBuilder);
			}
			if(joins.size()>0) {
				for(NativeJoin join : joins) {
					join.build(stringBuilder);
				}
			}
			//stringBuilder.append(" --join ").append(alias).append("\n");
		}
	}
	private abstract class NativePredicate extends NativeExpression {
		public NativePredicate(NativeBuilder builder) {
			super(builder);
		}
	}
	private class NativeXFYPredicate extends NativePredicate {
		NativeExpression x;
		String string;
		NativeExpression y;
		public NativeXFYPredicate(NativeBuilder builder, NativeExpression x, String string, NativeExpression y) {
			super(builder);
			this.x = x;
			this.string = string;
			this.y = y;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append('(');
			x.build(stringBuilder);
			stringBuilder.append(") ");
			stringBuilder.append(string);
			stringBuilder.append(" (");
			y.build(stringBuilder);
			stringBuilder.append(')');
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativeXFPredicate extends NativePredicate {
		NativeExpression x;
		String string;
		public NativeXFPredicate(NativeBuilder builder, NativeExpression x, String string) {
			super(builder);
			this.x = x;
			this.string = string;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append('(');
			x.build(stringBuilder);
			stringBuilder.append(") ");
			stringBuilder.append(string);
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
		
	}
	private class NativeVFXFYPredicate extends NativePredicate {
		NativeExpression v;
		NativeExpression x;
		String string;
		NativeExpression y;
		String string2;
		public NativeVFXFYPredicate(NativeBuilder builder, NativeExpression v, String string, NativeExpression x, String string2,
				NativeExpression y) {
			super(builder);
			this.v = v;
			this.x = x;
			this.string = string;
			this.y = y;
			this.string2 = string2;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append('(');
			v.build(stringBuilder);
			stringBuilder.append(") ");
			stringBuilder.append(string);
			stringBuilder.append(" (");
			x.build(stringBuilder);
			stringBuilder.append(") ");
			stringBuilder.append(string2);
			stringBuilder.append(" (");
			y.build(stringBuilder);
			stringBuilder.append(')');
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativeFXPredicate extends NativePredicate {
		NativeExpression x;
		String string;
		public NativeFXPredicate(NativeBuilder builder, String string, NativeExpression x) {
			super(builder);
			this.x = x;
			this.string = string;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append(string);
			stringBuilder.append(" (");
			x.build(stringBuilder);
			stringBuilder.append(')');
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativeFSPredicate extends NativePredicate {
		NativeSubquery x;
		String string;
		public NativeFSPredicate(NativeBuilder builder, String string, NativeSubquery x) {
			super(builder);
			this.x = x;
			this.string = string;
		}
		@Override
		public void build(StringBuilder stringBuilder) {
			stringBuilder.append(string);
			stringBuilder.append(" (");
			x.build(stringBuilder);
			stringBuilder.append(')');
			if(alias != null) {
				stringBuilder.append(" AS ").append(alias);
			}
		}
	}
	private class NativeOrder {
		NativeExpression expr;
		boolean isAsc;
		public NativeOrder(NativeExpression expr,boolean isAsc) {
			this.expr = expr;
			this.isAsc = isAsc;
		}
		public void build(StringBuilder stringBuilder) {
			expr.build(stringBuilder);
			if(isAsc) {
				stringBuilder.append(" ASC");
			} else {
				stringBuilder.append(" DESC");
			}
		}
	}
	
	private NativeExpression aclRestriction(NativeBuilder builder, NativeQuery query, NativeFrom join, 
			NativeParameterExpression paramFalse, NativeParameterExpression paramTrue, NativeParameterExpression paramCurrentUser, Map<NativeParameterExpression, Object> paramMap) throws NoSuchFieldException {
		//if someone has explicit ALLOW read access, entity is assumed private
		NativeSubquery subquery_somebody_has_true = query.subquery(Boolean.class);
		NativeRoot root_somebody_has_true = subquery_somebody_has_true.from(AccessControl.class);
		subquery_somebody_has_true.where(builder.and(
				builder.equal(root_somebody_has_true.get("deleted"),paramFalse),
				builder.equal(root_somebody_has_true.get("commonRecordId"), join.getId()), //for this entity
				builder.equal(root_somebody_has_true.get("read"), paramTrue) //ALLOW read permission
			)
		);
		subquery_somebody_has_true.select(root_somebody_has_true.get("read"));
		
		//the user has explicit ALLOW read access
		NativeSubquery subquery_user_ALLOW_read_perm = query.subquery(Boolean.class);
		NativeRoot root_user_ALLOW_read_perm = subquery_user_ALLOW_read_perm.from(AccessControl.class);
		subquery_user_ALLOW_read_perm.where(builder.and(
				builder.equal(root_user_ALLOW_read_perm.get("deleted"),paramFalse),
				builder.equal(root_user_ALLOW_read_perm.get("commonRecordId"), join.getId()), //for this entity
				builder.equal(root_user_ALLOW_read_perm.get("read"), paramTrue) //ALLOW read permission
			)
		);
		NativeJoin joinSecUserALLOW = root_user_ALLOW_read_perm.join("secUser",JoinType.INNER);
		joinSecUserALLOW.on(builder.and(
				builder.equal(joinSecUserALLOW.get("deleted"),paramFalse),
				builder.equal(joinSecUserALLOW.get("name"), paramCurrentUser) //for current user
			)
		);
		subquery_user_ALLOW_read_perm.select(root_user_ALLOW_read_perm.get("read"));
		

		//the user has explicit DENY read access
		NativeSubquery subquery_user_DENY_read_perm = query.subquery(Boolean.class);
		NativeRoot root_user_DENY_read_perm = subquery_user_DENY_read_perm.from(AccessControl.class);
		subquery_user_DENY_read_perm.where(builder.and(
				builder.equal(root_user_DENY_read_perm.get("deleted"),paramFalse),
				builder.equal(root_user_DENY_read_perm.get("commonRecordId"), join.getId()), //for this entity
				builder.equal(root_user_DENY_read_perm.get("read"), paramFalse) //ALLOW read permission
			)
		);
		NativeJoin joinSecUserDENY = root_user_DENY_read_perm.join("secUser",JoinType.INNER);
		joinSecUserDENY.on(builder.and(
				builder.equal(joinSecUserDENY.get("deleted"),paramFalse),
				builder.equal(joinSecUserDENY.get("name"), paramCurrentUser) //for current user
			)
		);
		subquery_user_DENY_read_perm.select(root_user_DENY_read_perm.get("read"));

		NativeParameterExpression parameterExpression = builder.parameter(String.class,"paramRole"+join.getJavaType().getSimpleName().toUpperCase()+paramMap.size());
		paramMap.put(parameterExpression, "ROLE_GET_ALL_"+join.getJavaType().getSimpleName().toUpperCase());
		//the user has GET_ALL permission
		NativeSubquery subquery_user_get_all = query.subquery(String.class);
		NativeRoot root_user_get_all = subquery_user_get_all.from(SecUser.class);
		NativeJoin joinUserToRole = root_user_get_all.join("roles",JoinType.INNER);
		NativeJoin joinRole = joinUserToRole.join("role",JoinType.INNER);
		NativeJoin joinRoleToPerm = joinRole.join("permissions",JoinType.INNER);
		NativeJoin joinPermission = joinRoleToPerm.join("permission",JoinType.INNER);
		subquery_user_get_all.where(builder.and(
			builder.equal(root_user_get_all.get("deleted"),paramFalse),
			builder.equal(joinUserToRole.get("deleted"),paramFalse),
			builder.equal(joinRole.get("deleted"),paramFalse),
			builder.equal(joinRoleToPerm.get("deleted"),paramFalse),
			builder.equal(joinPermission.get("deleted"),paramFalse),
			builder.equal(root_user_get_all.get("name"), paramCurrentUser), //for current user
			builder.equal(builder.upper(joinPermission.get("code")), parameterExpression)
		));
		subquery_user_get_all.select(joinPermission.get("code"));
		
		return builder.or(
				builder.exists(subquery_user_get_all), //has GET_ALL permission
				builder.exists(subquery_user_ALLOW_read_perm), //has explicit ALLOW permission
				builder.not(builder.or(
					builder.exists(subquery_somebody_has_true), //entity is private
					builder.exists(subquery_user_DENY_read_perm) //has explicit DENY permission
				))
			);
	}
	
	private boolean hasGetPermissionForEntity(Class entityClass) {
		final RepositoryResourceMappings repositoryResourceMappings = appContext.getBean(RepositoryResourceMappings.class);
		if(repositoryResourceMappings == null) {
			throw new RuntimeException("Cannot get RepositoryResourceMappings bean");
		}
		ResourceMetadata resMetadata = repositoryResourceMappings.getMetadataFor(entityClass);
		if(resMetadata == null) {
			throw new RuntimeException("Cannot get ResourceMetadata for "+entityClass.getSimpleName());
		}
		String rel = resMetadata.getRel().value();
		String role = "ROLE_GET_"+rel;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		for(GrantedAuthority authority: authentication.getAuthorities()) {
			if(((SimpleGrantedAuthority)authority).getAuthority().equals(role)) {
				logger.debug("Granted GET permission for entity: "+role);
				return true;
			}
			if(((SimpleGrantedAuthority)authority).getAuthority().equals("ROLE_GET_ANY")) {
				logger.debug("Granted GET permission for entity: "+"ROLE_GET_ANY");
				return true;
			}
		}
		logger.debug("No GET permission for entity: "+role);
		new org.springframework.security.access.AccessDeniedException("User doesn't have authority: "+role);
		return false;
	}

	private NativePath getFromsForPath(NativeFrom[] lastJoins /*out*/, String selectedEntity, Map<String,NativeFrom[]> joinsCache, NativeBuilder builder, NativeQuery query0,
			NativeParameterExpression paramFalse, NativeParameterExpression paramTrue, NativeParameterExpression paramCurrentUser, Map<NativeParameterExpression, Object> paramMap) throws NoSuchFieldException {
		String[] selectedEntityPath = selectedEntity.split("\\."); //dot-path
		String path = "";
		logger.trace("Full path has "+selectedEntityPath.length+" tokens");
		for(int pathIndex = 0; pathIndex < selectedEntityPath.length; pathIndex++) {
			if(pathIndex == 0) {
				path = selectedEntityPath[pathIndex];
			} else {
				path = path + "." + selectedEntityPath[pathIndex];
			}
			logger.trace("path: "+path);
			NativeFrom[] joins = joinsCache.get(path);
			if(joins == null) {
				try {
					logger.trace("Creating new joins: "+selectedEntityPath[pathIndex]);
					NativeJoin leftJoin = lastJoins[0].join(selectedEntityPath[pathIndex],JoinType.LEFT);
					//check access permissions
					hasGetPermissionForEntity(leftJoin.getClazz());
					leftJoin.on(aclRestriction(builder, query0, leftJoin, paramFalse, paramTrue, paramCurrentUser, paramMap));
					
					joins = new NativeJoin[]{ leftJoin };
					joinsCache.put(path, joins);
				} catch(BasicPathUsageException|MappingException e) {
					//not a table join, but field - return it
					return lastJoins[0].get(selectedEntityPath[pathIndex]);
				}
			}
			//one by one because we want to send it as out param
			lastJoins[0] = joins[0];
		}
		return null;
	}
	
	private NativeExpression[] getExpressionFromParam(NativeBuilder builder, NativeRoot root, NativeQuery criteriaQuery, Map<String, NativeFrom[]> joinsCache,
			String[] aggregation, NativeParameterExpression paramFalse, NativeParameterExpression paramTrue, NativeParameterExpression paramCurrentUser, Map<NativeParameterExpression, Object> paramMap) throws NoSuchFieldException {
		if(aggregation[0].length() > 0) {
			String aggregationField = "";
			String operation = "";
			int openBracketPos = aggregation[0].indexOf("(");
			int commaPos = aggregation[0].indexOf(";");
			int closeBracketPos = aggregation[0].indexOf(")");
			if(openBracketPos >= 0 && (commaPos < 0 || openBracketPos < commaPos) ) {
				//comma is not first and not a field (we have bracket), it is openBracket or closeBracket
				if(closeBracketPos < 0 || openBracketPos < closeBracketPos) {
					//openBracketPos is first
					aggregationField = aggregation[0].substring(openBracketPos+1, aggregation[0].length());
					operation = aggregation[0].substring(0, openBracketPos);
					
					logger.trace("Expression with operation: "+operation+" and rest is: "+aggregationField);
					String[] rest = {aggregationField};
					NativeExpression[] field = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
					logger.trace("Expression with operation: "+operation+" and after first parameter is: "+rest[0]);
					if(operation.equals("sum")) {
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.sum(field[0])};
						return result;
					}
					if(operation.equals("count")) {
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.count(field[0])};
						return result;
					}
					if(operation.equals("avg")) {
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.avg(field[0])};
						return result;
					}
					if(operation.equals("min")) {
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.min(field[0])};
						return result;
					}
					if(operation.equals("max")) {
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.max(field[0])};
						return result;
					}
					if(operation.equals("/")) {
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						NativeExpression[] param2 = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.quot(field[0],param2[0])};
						return result;
					}
					if(operation.equals("*")) {
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						NativeExpression[] param2 = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.prod(field[0],param2[0])};
						return result;
					}
					if(operation.equals("plus")) {
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						NativeExpression[] param2 = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.sum(field[0],param2[0])};
						return result;
					}
					if(operation.equals("minus")) {
						if(!rest[0].startsWith(";")) logger.error("Expected ; was not found: "+rest[0]);
						rest[0] = rest[0].substring(1, rest[0].length()); //consume the comma
						NativeExpression[] param2 = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
						if(!rest[0].startsWith(")")) logger.error("Expected ) was not found: "+rest[0]);
						aggregation[0] = rest[0].substring(1, rest[0].length()); //consume the closeBracket
						NativeExpression[] result = {builder.diff(field[0],param2[0])};
						return result;
					}
				} else {
					//closeBracket is first
					String[] rest = {aggregation[0].substring(0, closeBracketPos)};
					NativeExpression[] field = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
					aggregation[0] = aggregation[0].substring(closeBracketPos, aggregation[0].length());
					return field;
				}
			} else {
				//openBracket is not first, it is comma, closeBracket or field
				if(closeBracketPos >= 0 && (commaPos < 0 || closeBracketPos < commaPos) ) {
					//closeBracket is first
					String[] rest = {aggregation[0].substring(0, closeBracketPos)};
					logger.trace("Calculating till closeBracket: "+rest[0]);
					NativeExpression[] field = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
					aggregation[0] = aggregation[0].substring(closeBracketPos, aggregation[0].length());
					return field;
				} else {
					//comma is first or field
					if(commaPos >= 0) {
						//comma is first
						String[] rest = {aggregation[0].substring(0, commaPos)};
						logger.trace("Calculating till comma: "+rest[0]);
						NativeExpression[] field = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, rest, paramFalse, paramTrue, paramCurrentUser, paramMap);
						aggregation[0] = aggregation[0].substring(commaPos, aggregation[0].length());
						return field;
					} else {
						//it is field or literal
						logger.trace("Expression field or literal: "+aggregation[0]);
						aggregationField = aggregation[0];
						NativeExpression[] field = {null};
						try {
							NativeParameterExpression parameterExpression = builder.parameter(Double.class,"extraParam"+paramMap.size());
							paramMap.put(parameterExpression, Double.parseDouble(aggregationField));
							field[0] = parameterExpression;
							logger.trace("Expression is double literal: "+aggregationField);
						} catch(NumberFormatException e) {
							if(aggregationField.equals("*")) {
								field[0] = builder.starExpression(null);
							} else {
								NativeFrom[] lastJoins = new NativeFrom[]{ root };
								field[0] = getFromsForPath(lastJoins, aggregationField, joinsCache, builder, criteriaQuery, paramFalse, paramTrue, paramCurrentUser, paramMap);
							}
						}
						aggregation[0] = "";
						return field;
					}
				}
			}
		}
		return null;
	}
	
	public Page<ObjectNode> nativeReport(Pageable pageable, MultiValueMap<String, String> params) throws NoSuchFieldException {
		Class<?> entityClass;
		try {
			//build the query
			Metamodel metamodel = entityManager.getMetamodel();
			NativeBuilder builder = new NativeBuilder();
			NativeQuery criteriaQuery = builder.createNativeQuery();
			
			//set from
			entityClass = ReportsRepository.getClassFromRestUriOrClassName(mappings, params.getFirst("from"));
			NativeRoot root = criteriaQuery.from( entityClass );

			//check access permissions
			hasGetPermissionForEntity(entityClass);
			
			NativeParameterExpression paramFalse = builder.parameter(Boolean.class,"paramFalse");
			NativeParameterExpression paramTrue = builder.parameter(Boolean.class,"paramTrue");
			NativeParameterExpression paramCurrentUser = builder.parameter(String.class,"paramCurrentUser");
			Map<NativeParameterExpression, Object> paramMap = new HashMap<NativeParameterExpression, Object>();
			
			//filter by company by inner-joining it
			if(CompanyRecord.class.isAssignableFrom(entityClass)) {
				NativeJoin rootCompanyUser = root.join("company",JoinType.INNER).join("users",JoinType.INNER);
				rootCompanyUser.on(builder.equal(rootCompanyUser.get("name"), paramCurrentUser));
			}
			
			//set joins
			Map<String,NativeFrom[]> joinsCache = new HashMap<String,NativeFrom[]>();
			joinsCache.put(params.getFirst("from"), new NativeFrom[]{ root });
			
			//set selected expressions
			List<NativeSelection> selections = new LinkedList<NativeSelection>();
			List<String> fieldAliases = new ArrayList<String>();
			List<NativeExpression> groupByExpressions = new LinkedList<NativeExpression>();
			logger.trace("Start selecting expressions and setting joins");
			Map<String,NativeExpression[]> aliasCache = new HashMap<String,NativeExpression[]>();
			String selectParam = params.getFirst("select");
			if(selectParam != null) {
				for(String selectedEntity : selectParam.split(",")) { //comma-separated list of paths 
					logger.trace("selectedEntity: "+selectedEntity);
					if(selectedEntity.length() > 0) {
						String alias = selectedEntity.replaceAll("\\.", "_dot_");
						if(selectedEntity.contains("(")) { //bracket -> aggregate
							int colonPos = selectedEntity.lastIndexOf(':');
							String expr;
							if(colonPos >= 0) {
								expr = selectedEntity.substring(0,colonPos);
								alias = selectedEntity.substring(colonPos+1);
							} else {
								expr = selectedEntity;
								alias = selectedEntity;
							}
							String[] param = {expr};
							NativeExpression[] aggregateSelection = getExpressionFromParam(builder, root, criteriaQuery, joinsCache, param, paramFalse, paramTrue, paramCurrentUser, paramMap);
							if(aggregateSelection != null) {
								aggregateSelection[0].alias = alias;
								selections.add(aggregateSelection[0]);
								fieldAliases.add(alias);
								logger.trace("Cache alias: "+alias+"; "+expr);
								aliasCache.put(alias,aggregateSelection);
							}
						} else {
							NativeFrom[] lastJoins = new NativeFrom[]{ root };
							NativePath field = getFromsForPath(lastJoins, selectedEntity, joinsCache, builder, criteriaQuery, paramFalse, paramTrue, paramCurrentUser, paramMap);
							if(field == null) {
								//it was a table join (entity select)
								//selections.add(lastJoins[0].alias(alias)); //instead of this select each attribute one by one
								//logger.trace("Cache alias: "+alias+"; "+lastJoins[0].getClass());
								//aliasCache.put(alias,lastJoins); //put only aggregates or else find how to mark which is aggregate (check the only aliasCache.get in this code!)
								
								//now set the group-by expressions for this join
								EntityType joinEntityType = metamodel.entity(lastJoins[0].getClazz());
								for(Object attr : joinEntityType.getAttributes()) {
									if(attr instanceof SingularAttribute) {
										logger.trace("SingularAttribute: "+((SingularAttribute) attr).getName());
										NativePath selection = lastJoins[0].get((SingularAttribute) attr);
										selection.alias = alias+"_dot_"+((SingularAttribute) attr).getName();
										selections.add(selection);
										fieldAliases.add(selectedEntity+"."+((SingularAttribute) attr).getName());
										groupByExpressions.add(lastJoins[0].get((SingularAttribute) attr));
									} else if(attr instanceof PluralAttribute) {
										logger.trace("Skipping PluralAttribute: "+((PluralAttribute) attr).getName());
									} else {
										logger.trace("Skipping attribute: "+attr);
									}
								}
								
								//this should be done only if the entity is annotated as inherited with union strategy
								SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
								EntityPersister entityPersister = sessionFactory.getEntityPersister( joinEntityType.getBindableJavaType().getName() );
								if(JoinedSubclassEntityPersister.class.isInstance(entityPersister)) {
									for(EntityType et : metamodel.getEntities()) {
										if(joinEntityType.equals(et.getSupertype())) {
											logger.trace("Inherited type: "+joinEntityType.getName()+" -> "+et.getName());
											for(Object attr : et.getAttributes()) {
												if(attr instanceof SingularAttribute) {
													logger.trace("Inherited SingularAttribute: "+((SingularAttribute) attr).getName());
													NativePath selection = lastJoins[0].get((SingularAttribute) attr);
													selection.alias = alias;
													selections.add(selection);
													fieldAliases.add(selectedEntity+"."+((SingularAttribute) attr).getName());
													groupByExpressions.add(lastJoins[0].get((SingularAttribute) attr));
												}
											}
											groupByExpressions.add(lastJoins[0].type());
										}
									}
								}
							} else {
								//field selected
								field.alias = alias;
								selections.add(field);
								fieldAliases.add(selectedEntity);
								groupByExpressions.add(field);
							}
						}
					}
				}
			}
			
			//set where clause
			NativeExpression criteriaWhere = aclRestriction(builder, criteriaQuery, root, paramFalse, paramTrue, paramCurrentUser, paramMap);
			//and having clause
			NativeExpression having = null;
			
			for(Entry<String, List<String>> param : params.entrySet()) {
				String key = param.getKey();
				logger.trace("Where clause?: "+key);
				String field = key;
				NativeFrom[] lastJoins = new NativeFrom[]{ root };
				int dotPos = key.lastIndexOf(".");
				int bracketPos = key.lastIndexOf("(");
				//if this field is has a path that needs to be joined
				if(dotPos >= 0 && bracketPos == -1) { // dot -> it has path; bracket -> it is calculation/aggregation, don't touch it
					getFromsForPath(lastJoins, key, joinsCache, builder, criteriaQuery, paramFalse, paramTrue, paramCurrentUser, paramMap);
					field = key.substring(dotPos+1);
				}
				logger.trace("Where clause field?: "+field);
				if(lastJoins != null) {
					logger.trace("joins != null");
					try {
						NativeExpression paramRestriction = null;
						NativeExpression[] cachedAlias = aliasCache.get(key);
						if(field.equalsIgnoreCase("dtype")) {
							logger.trace("Special filter: dtype");
							try {
								SessionFactoryImpl sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactoryImpl.class);
								EntityPersister entityPersister = sessionFactory.getEntityPersister( Task.class.getPackage().getName()+"."+param.getValue().get(0) );
								int clazz_ = 0;
								if(UnionSubclassEntityPersister.class.isInstance(entityPersister)) {
									clazz_ = (Integer) ((UnionSubclassEntityPersister) entityPersister).getDiscriminatorValue();
									NativeParameterExpression parameterExpression = builder.parameter(Integer.class,"paramDTYPE"+paramMap.size());
									paramMap.put(parameterExpression, clazz_);
									paramRestriction = builder.equal(lastJoins[0].type(), parameterExpression);
								} else if(JoinedSubclassEntityPersister.class.isInstance(entityPersister)) {
									clazz_ = (Integer) ((JoinedSubclassEntityPersister) entityPersister).getDiscriminatorValue();
									NativeParameterExpression parameterExpression = builder.parameter(Integer.class,"paramDTYPE"+paramMap.size());
									paramMap.put(parameterExpression, clazz_);
									paramRestriction = builder.equal(lastJoins[0].type(), parameterExpression);
								} else {
									Class dtypeClass = ReportsRepository.getClassFromRestUriOrClassName(mappings, param.getValue().get(0));
									NativeParameterExpression parameterExpression = builder.parameter(Class.class,"paramDTYPEClass"+paramMap.size());
									paramMap.put(parameterExpression, dtypeClass);
									paramRestriction = builder.equal(lastJoins[0].type(), parameterExpression);
								}
							} catch(MappingException|ClassCastException|ClassNotFoundException e) {
								logger.error("Filter DTYPE ignored: "+key+" reason: "+e.getMessage());
							}
						} else {
							NativeExpression fieldPath;
							if(cachedAlias == null) {
								fieldPath = lastJoins[0].get(field);
							} else {
								logger.trace("Using cached expression for alias: "+key);
								fieldPath = cachedAlias[0];
							}
							Class fieldClass = fieldPath.getJavaType();
							logger.trace("Param type: "+fieldClass);
							if(param.getValue().get(0).isEmpty()) {
								logger.trace("Filter for NULLs: "+key);
								paramRestriction = builder.isNull(fieldPath);
							} else if(Boolean.class == fieldClass || fieldClass == boolean.class) {
								NativeParameterExpression parameterExpression = builder.parameter(Boolean.class,"extraParam"+paramMap.size());
								paramMap.put(parameterExpression, Boolean.parseBoolean(param.getValue().get(0)));
								paramRestriction = builder.equal(fieldPath, parameterExpression);
							} else if(fieldClass == String.class) {
								NativeParameterExpression parameterExpression = builder.parameter(String.class,"extraParam"+paramMap.size());
								paramMap.put(parameterExpression, "%"+param.getValue().get(0).toUpperCase()+"%");
								paramRestriction = builder.like(builder.upper(fieldPath), parameterExpression);
							} else if(fieldClass == Long.class || fieldClass == Integer.class || fieldClass == int.class) {
								NativeParameterExpression parameterExpression = builder.parameter(Long.class,"extraParam"+paramMap.size());
								paramMap.put(parameterExpression, Long.parseLong(param.getValue().get(0)));
								paramRestriction = builder.equal(fieldPath, parameterExpression);
							} else if(fieldClass == Date.class || fieldClass == Calendar.class) {
								List<String> stringParams = param.getValue();
								SimpleDateFormat formatter=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z"); //2018/06/01 00:00:00 +0300
								Date date1 = formatter.parse(param.getValue().get(0));
								NativeParameterExpression parameterExpression1 = builder.parameter(Date.class,"extraParam"+paramMap.size());
								paramMap.put(parameterExpression1, date1);
								if(stringParams.size() > 1) {
									Date date2 = formatter.parse(param.getValue().get(1));
									NativeParameterExpression parameterExpression2 = builder.parameter(Date.class,"extraParam"+paramMap.size());
									paramMap.put(parameterExpression2, date2);
									if(date1.before(date2)) {
										paramRestriction = builder.between(fieldPath, parameterExpression1, parameterExpression2);
									} else {
										paramRestriction = builder.between(fieldPath, parameterExpression2, parameterExpression1);
									}
								} else {
									paramRestriction = builder.equal(fieldPath, parameterExpression1);
								}
							} else {
								logger.error("Filter parameter type not supported for "+field);
							}
						}
						if(cachedAlias == null) {
							if(paramRestriction != null) {
								criteriaWhere = builder.and(criteriaWhere, paramRestriction);
							} else {
								logger.error("Filter is null!");
							}
						} else {
							if(paramRestriction != null) {
								having = having == null ? paramRestriction : builder.and(having, paramRestriction);
							} else {
								logger.error("Filter is null!");
							}
						}
						logger.trace("Where clause added!");
					} catch (MappingException e) {
						logger.debug("Filter ignored (MappingException): "+key);
					} catch (IllegalStateException e) {
						logger.debug("Filter ignored (IllegalStateException): "+key);
					} catch (IllegalArgumentException e) {
						logger.debug("Filter ignored (IllegalArgumentException): "+key);
					} catch (ParseException e) {
						logger.error("Filter for date cannot be parsed from string: "+e.getMessage());
					}
				}
			}

			for(NativeFrom[] joinCouple : joinsCache.values()) {
				criteriaWhere = builder.and(criteriaWhere, builder.or(builder.isNull(joinCouple[0].getId()),builder.equal(joinCouple[0].get("deleted"),paramFalse)));
			}
			
			criteriaQuery.where(criteriaWhere);
			
			//set summary aggregates
//			List<Selection<?>> aggregations = new LinkedList<Selection<?>>();
//			aggregations.add(builder.literal("_summaryRow"));
//			aggregations.add(builder.count(rootCount));
//			String summaryParam = params.getFirst("summary");
//			if(summaryParam != null) {
//				for(String aggregation : summaryParam.split(",")) { //comma-separated list of paths
//					int colonPos = aggregation.lastIndexOf(':');
//					String expr;
//					if(colonPos >= 0) {
//						expr = aggregation.substring(0,colonPos);
//					} else {
//						expr = aggregation;
//					}
//					String[] param = {expr};
//					Selection[] aggregateSelection = getExpressionFromParam(builder,root,rootCount, criteriaQuery, countQuery,joinsCache,param, paramFalse, paramTrue, paramCurrentUser, paramMap);
//					if(aggregateSelection != null) {
//						if(colonPos >= 0) {
//							aggregateSelection[1].alias(aggregation.substring(colonPos+1));
//						}
//						aggregations.add(aggregateSelection[1]);
//					}
//				}
//			}
			
			criteriaQuery.multiselect(selections);
			
			criteriaQuery.groupBy(groupByExpressions);
			
			if(having != null) {
				criteriaQuery.having(having);
			}
			
			if(pageable.getSort() != null) {
				List<NativeOrder> orderList = new LinkedList<NativeOrder>();
				for(org.springframework.data.domain.Sort.Order order : pageable.getSort()){
					String key = order.getProperty();
					if(key != null) {
						String field = key;
						NativeFrom[] joins = new NativeFrom[]{ root };
						int dotPos = key.lastIndexOf(".");
						if(dotPos >= 0) {
							joins = joinsCache.get(key.substring(0,dotPos));
							field = key.substring(dotPos+1);
						}
						try {
							if(order.isAscending()) {
								orderList.add(builder.asc(joins[0].get(field)));
							} else {
								orderList.add(builder.desc(joins[0].get(field)));
							}
						} catch(java.lang.IllegalArgumentException e) {
							logger.debug("Sort order ignored: "+key);
						}
					}
				}
				criteriaQuery.orderBy(orderList);
			}
			
			StringBuilder stringBuilder = new StringBuilder();
			criteriaQuery.build(stringBuilder);
			String sql = stringBuilder.toString();
			logger.debug("Query SQL: \n"+sql);
			Query query = entityManager.createNativeQuery(sql);
			query.setParameter(paramFalse.getName(), false);
			query.setParameter(paramTrue.getName(), true);
			//get the logged user name
			String currentUser = null;
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				currentUser = authentication.getPrincipal().toString();
			}
			query.setParameter(paramCurrentUser.getName(), currentUser);
			for(NativeParameterExpression param : paramMap.keySet()) {
				logger.trace("Set param: "+param.getName()+" = "+paramMap.get(param));
				query.setParameter(param.getName(), paramMap.get(param));
			}
			
			// pagination
			//TODO a solution to get all rows in one page
			query.setFirstResult((int) pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
			
			Query count = entityManager.createNativeQuery("SELECT COUNT(*) FROM ("+sql+") summary");
			count.setParameter(paramFalse.getName(), false);
			count.setParameter(paramTrue.getName(), true);
			count.setParameter(paramCurrentUser.getName(), currentUser);
			for(NativeParameterExpression param : paramMap.keySet()) {
				logger.trace("Set param for count query: "+param.getName());
				count.setParameter(param.getName(),paramMap.get(param));
			}
			
			List<Object[]> result = query.getResultList();
			List<Object[]> summaryList = count.getResultList();
			//TODO solution to get summary when there is groupBy
//			if(summaryList.size() > 0) {
//				Object[] summary = summaryList.get(0);
//				//export as paging specific attributes
//				if(summaryParam != null) {
//					result.add(summary);
//				}
//			}

			List<ObjectNode> rowSet = new LinkedList<ObjectNode>();
			ObjectMapper mapper = new ObjectMapper();
			for(Object[] row : result) {
				ObjectNode newEntityNode = mapper.createObjectNode();
				for(int index = 0; index < fieldAliases.size(); index++) {
					if(row[index] != null) {
						newEntityNode.put(fieldAliases.get(index), row[index].toString());
					}
				}
				rowSet.add(newEntityNode);
			}
			return PageableExecutionUtils.getPage(rowSet, pageable, () -> (Long) summaryList.get(0)[0]);
			//return result;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void traceAbstractEntityPersister(AbstractEntityPersister persisterImpl, String attribute) {
			logger.trace("getDiscriminatorColumnName: "+persisterImpl.getDiscriminatorColumnName());
			logger.trace("getDiscriminatorColumnReaders: "+persisterImpl.getDiscriminatorColumnReaders());
			logger.trace("getDiscriminatorColumnReaderTemplate: "+persisterImpl.getDiscriminatorColumnReaderTemplate());
			logger.trace("getDiscriminatorSQLValue: "+persisterImpl.getDiscriminatorSQLValue());
			logger.trace("getEntityName: "+persisterImpl.getEntityName());
			logger.trace("getIdentifierPropertyName: "+persisterImpl.getIdentifierPropertyName());
			logger.trace("getIdentitySelectString: "+persisterImpl.getIdentitySelectString());
			logger.trace("getMappedSuperclass: "+persisterImpl.getMappedSuperclass());
			logger.trace("getName: "+persisterImpl.getName());
			logger.trace("getRootEntityName: "+persisterImpl.getRootEntityName());
			logger.trace("getRootTableName: "+persisterImpl.getRootTableName());
			logger.trace("getTableName: "+persisterImpl.getTableName());
			logger.trace("getVersionColumnName: "+persisterImpl.getVersionColumnName());
			logger.trace("getConstraintOrderedTableNameClosure: "+Arrays.toString(persisterImpl.getConstraintOrderedTableNameClosure()));
			logger.trace("getContraintOrderedTableKeyColumnClosure: "+Arrays.toString(persisterImpl.getContraintOrderedTableKeyColumnClosure()));
			logger.trace("getIdentifierColumnNames: "+Arrays.toString(persisterImpl.getIdentifierColumnNames()));
			logger.trace("getIdentifierColumnReaders: "+Arrays.toString(persisterImpl.getIdentifierColumnReaders()));
			logger.trace("getIdentifierColumnReaderTemplates: "+Arrays.toString(persisterImpl.getIdentifierColumnReaderTemplates()));
			logger.trace("getKeyColumnNames: "+Arrays.toString(persisterImpl.getKeyColumnNames()));
			logger.trace("getPropertyNames: "+Arrays.toString(persisterImpl.getPropertyNames()));
			logger.trace("getRootTableIdentifierColumnNames: "+Arrays.toString(persisterImpl.getRootTableIdentifierColumnNames()));
			logger.trace("getRootTableKeyColumnNames: "+Arrays.toString(persisterImpl.getRootTableKeyColumnNames()));
			logger.trace("getSubclassColumnReaderTemplateClosure: "+Arrays.toString(persisterImpl.getSubclassColumnReaderTemplateClosure()));
			logger.trace("getSubclassPropertyColumnReaderClosure: "+Arrays.toString(persisterImpl.getSubclassPropertyColumnReaderClosure()));
			logger.trace("getSubclassPropertyColumnReaderTemplateClosure: "+Arrays.toString(persisterImpl.getSubclassPropertyColumnReaderTemplateClosure()));
			logger.trace("getSubclassPropertyFormulaTemplateClosure: "+Arrays.toString(persisterImpl.getSubclassPropertyFormulaTemplateClosure()));
			logger.trace("getDiscriminatorAlias: "+persisterImpl.getDiscriminatorAlias(attribute));
		try {
			logger.trace("getPropertyIndex: "+persisterImpl.getPropertyIndex(attribute));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getPropertyTableName: "+persisterImpl.getPropertyTableName(attribute));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getRootTableAlias: "+persisterImpl.getRootTableAlias(attribute));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getSelectByUniqueKeyString: "+persisterImpl.getSelectByUniqueKeyString(attribute));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getSubclassPropertyTableNumber: "+persisterImpl.getSubclassPropertyTableNumber(attribute));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getIdentifierAliases: "+Arrays.toString(persisterImpl.getIdentifierAliases(attribute)));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getPropertyColumnNames: "+Arrays.toString(persisterImpl.getPropertyColumnNames(attribute)));
		} catch(Exception e) {logger.trace(e.getMessage());}
		try {
			logger.trace("getSubclassPropertyColumnNames: "+Arrays.toString(persisterImpl.getSubclassPropertyColumnNames(attribute)));
		} catch(Exception e) {logger.trace(e.getMessage());}
	}
}
