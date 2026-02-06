/**
 *
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.icc.broadcast.entity.BroadcastMachine;
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
public class BroadcastMachineRepository extends AbstractRepository<BroadcastMachine> {

    public BroadcastMachine findById(ObjectId id) {
        return this.findById(BroadcastMachine.class, id);
    }

    public BroadcastMachine findOneBy(Criteria c) {
        return this.mongoTemplate.findOne(new Query(c).with(Sort.by(Sort.Order.desc("_id"))), BroadcastMachine.class);
    }

    public List<BroadcastMachine> findBy(Criteria c) {
        return this.mongoTemplate.find(new Query(c), BroadcastMachine.class);
    }

    public List<BroadcastMachine> findBy(Criteria c, int start, int limit) {
        return this.mongoTemplate.find(new Query(c).skip(start).limit(limit), BroadcastMachine.class);
    }

    public long count(Criteria c) {
        Query query = new Query(c);
        return this.mongoTemplate.count(query, BroadcastMachine.class);
    }

    public void updateStarted(ObjectId id, boolean started) {
        Query q = new Query(Criteria.where("_id").is(id));

        this.mongoTemplate.updateFirst(q, new Update().set("started", started).set("updateTime", new Date()), BroadcastMachine.class);
    }

    public void updateTime(ObjectId id, Date updateTime) {
        Query q = new Query(Criteria.where("_id").is(id));

        this.mongoTemplate.updateFirst(q, new Update().set("updateTime", updateTime), BroadcastMachine.class);
    }

    public void remove(String id) {
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), BroadcastMachine.class);
    }
}
