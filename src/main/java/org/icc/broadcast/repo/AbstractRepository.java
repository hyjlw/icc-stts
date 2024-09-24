/**
 * 
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * @author IT
 *
 */
public abstract class AbstractRepository<T> {
	@Autowired
	@Qualifier("mongoTemplate")
	protected MongoTemplate mongoTemplate;

	public T findById(Class<T> clazz, String id) {
		return this.mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), clazz);
	}

	public T findById(Class<T> clazz, ObjectId id) {
		return this.mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), clazz);
	}

	public List<T> findAll(Class<T> clazz) {
		return this.mongoTemplate.findAll(clazz);
	}

	public void remove(T obj) {
		this.mongoTemplate.remove(obj);
	}

	public void add(T obj) {
		this.mongoTemplate.save(obj);
	}

	public void addAll(List<T> objs) {
		this.mongoTemplate.insertAll(objs);
	}
	
	public void removeAll(Class<T> clazz) {
		this.mongoTemplate.dropCollection(clazz);
	}
}
