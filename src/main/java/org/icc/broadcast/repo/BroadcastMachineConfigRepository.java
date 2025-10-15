/**
 *
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.icc.broadcast.entity.BroadcastMachineConfig;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author LL
 *
 */
@Repository
public class BroadcastMachineConfigRepository extends AbstractRepository<BroadcastMachineConfig> {

    public BroadcastMachineConfig findById(ObjectId id) {
        return this.findById(BroadcastMachineConfig.class, id);
    }

    public BroadcastMachineConfig findOneBy(Criteria c) {
        return this.mongoTemplate.findOne(new Query(c).with(Sort.by(Sort.Order.desc("_id"))), BroadcastMachineConfig.class);
    }

    public List<BroadcastMachineConfig> findBy(Criteria c) {
        return this.mongoTemplate.find(new Query(c), BroadcastMachineConfig.class);
    }

    public List<BroadcastMachineConfig> findBy(Criteria c, int start, int limit) {
        return this.mongoTemplate.find(new Query(c).skip(start).limit(limit), BroadcastMachineConfig.class);
    }

    public long count(Criteria c) {
        Query query = new Query(c);
        return this.mongoTemplate.count(query, BroadcastMachineConfig.class);
    }

    public void updateSession(ObjectId id, ObjectId sessionId) {
        Query q = new Query(Criteria.where("_id").is(id));

        this.mongoTemplate.updateFirst(q, new Update().set("sessionId", sessionId).set("updateTime", new Date()), BroadcastMachineConfig.class);
    }

    public void remove(String id) {
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), BroadcastMachineConfig.class);
    }
}
