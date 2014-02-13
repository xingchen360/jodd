// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.db.oom.dao;

import jodd.bean.BeanUtil;
import jodd.db.DbQuery;
import jodd.db.oom.DbEntityDescriptor;
import jodd.db.oom.DbOomException;
import jodd.db.oom.DbOomManager;
import jodd.db.oom.sqlgen.DbEntitySql;

import java.util.Collection;
import java.util.List;

import static jodd.db.oom.DbOomQuery.query;
import static jodd.db.oom.sqlgen.DbEntitySql.findByColumn;
import static jodd.db.oom.sqlgen.DbEntitySql.insert;
import static jodd.db.oom.sqlgen.DbEntitySql.updateAll;

/**
 * Generic DAO. Contains many convenient wrappers.
 */
public class GenericDao {

	// ---------------------------------------------------------------- config

	protected boolean keysGeneratedByDatabase = true;

	/**
	 * Returns <code>true</code> if keys are auto-generated by database.
	 * Otherwise, keys are generated manually.
	 */
	public boolean isKeysGeneratedByDatabase() {
		return keysGeneratedByDatabase;
	}

	/**
	 * Specifies how primary keys are generated.
	 */
	public void setKeysGeneratedByDatabase(boolean keysGeneratedByDatabase) {
		this.keysGeneratedByDatabase = keysGeneratedByDatabase;
	}

	// ---------------------------------------------------------------- store

	/**
	 * Returns <code>true</code> if entity is persistent.
	 */
	protected boolean isPersistent(DbEntityDescriptor ded, Object entity) {
		Object key = ded.getIdValue(entity);

		if (key == null) {
			return false;
		}
		if (key instanceof Number) {
			long value = ((Number)key).longValue();

			if (value == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets new ID value for entity.
	 */
	protected void setEntityId(DbEntityDescriptor ded, Object entity, long newValue) {
		ded.setIdValue(entity, Long.valueOf(newValue));
	}

	/**
	 * Generates next id for given type.
	 */
	protected long generateNextId(DbEntityDescriptor ded) {
		throw new UnsupportedOperationException("Use Joy");
	}

	/**
	 * Saves or updates entity. If ID is not <code>null</code>, entity will be updated.
	 * Otherwise, entity will be inserted into the database.
	 */
	public <E> E store(E entity) {
		DbOomManager dboom = DbOomManager.getInstance();
		Class type = entity.getClass();
		DbEntityDescriptor ded = dboom.lookupType(type);

		if (ded == null) {
			throw new DbOomException("Not an entity: " + type);
		}
		if (isPersistent(ded, entity) == false) {
			DbQuery q;
			if (keysGeneratedByDatabase == true) {
				q = query(insert(entity));
				q.setGeneratedKey();
				q.executeUpdate();
				long nextId = q.getGeneratedKey();
				setEntityId(ded, entity, nextId);
			} else {
				long nextId = generateNextId(ded);
				setEntityId(ded, entity, nextId);
				q = query(insert(entity));
				q.executeUpdate();
			}
			q.close();
		} else {
			query(updateAll(entity)).executeUpdateAndClose();
		}
		return entity;
	}

	/**
	 * Simply inserts object into the database.
	 */
	public void save(Object entity) {
		DbQuery q = query(insert(entity));
		q.executeUpdateAndClose();
	}

	/**
	 * Inserts bunch of objects into the database.
	 * @see #save(Object)
	 */
	public void saveAll(Collection entities) {
		for (Object entity: entities) {
			save(entity);
		}
	}

	// ---------------------------------------------------------------- update

	/**
	 * Updates single property. Also modifies the entity bean.
	 */
	public <E> E updateProperty(E entity, String name, Object value) {
		query(DbEntitySql.updateColumn(entity, name, value)).executeUpdateAndClose();
		BeanUtil.setDeclaredProperty(entity, name, value);
		return entity;
	}

	// ---------------------------------------------------------------- find

	/**
	 * Finds single entity by its id.
	 */
	public <E> E findById(Class<E> entityType, long id) {
		return query(DbEntitySql.findById(entityType, Long.valueOf(id))).findAndClose(entityType);
	}

	/**
	 * Finds single entity by matching property.
	 */
	public <E> E findOneByProperty(Class<E> entityType, String name, Object value) {
		return query(findByColumn(entityType, name, value)).findAndClose(entityType);
	}

	/**
	 * Finds one entity for given criteria.
	 */
	@SuppressWarnings({"unchecked"})
	public <E> E findOne(E criteria) {
		return (E) query(DbEntitySql.find(criteria)).findAndClose(criteria.getClass());
	}

	/**
	 * Finds list of entities matching given criteria.
	 */
	@SuppressWarnings({"unchecked"})
	public <E> List<E> find(Object criteria) {
		return query(DbEntitySql.find(criteria)).listAndClose(criteria.getClass());
	}

	/**
	 * Finds list of entities matching given criteria.
	 */
	public <E> List<E> find(Class<E> entityType, Object criteria) {
		return query(DbEntitySql.find(entityType, criteria)).listAndClose(entityType);
	}

	// ---------------------------------------------------------------- delete

	/**
	 * Deleted single entity by its id.
	 */
	public void deleteById(Class entityType, long id) {
		query(DbEntitySql.deleteById(entityType, Long.valueOf(id))).executeUpdateAndClose();
	}

	/**
	 * Delete single object by its id.
	 */
	public void deleteById(Object entity) {
		if (entity != null) {
			query(DbEntitySql.deleteById(entity)).executeUpdateAndClose();
		}
	}

	/**
	 * Deletes all objects by their id.
	 */
	public void deleteAllById(Collection objects) {
		for (Object entity : objects) {
			deleteById(entity);
		}
	}

	// ---------------------------------------------------------------- count

	/**
	 * Counts number of all entities.
	 */
	public <E> long count(Class<E> entityType) {
		return query(DbEntitySql.count(entityType)).executeCountAndClose();
	}

	// ---------------------------------------------------------------- related

	/**
	 * Finds related entity.
	 */
	public <E> List<E> findRelated(Class<E> target, Object source) {
		return query(DbEntitySql.findForeign(target, source)).listAndClose(target);
	}

	// ---------------------------------------------------------------- list

	/**
	 * List all entities.
	 */
	public <E> List<E> listAll(Class<E> target) {
		return query(DbEntitySql.from(target)).listAndClose(target);
	}

}