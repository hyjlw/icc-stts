/**
 *
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.icc.broadcast.entity.BroadcastSession;
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
public class BroadcastSessionRepository extends AbstractRepository<BroadcastSession> {

    public BroadcastSession findById(ObjectId id) {
        return this.findById(BroadcastSession.class, id);
    }

    public BroadcastSession findOneBy(Criteria c) {
        return this.mongoTemplate.findOne(new Query(c).with(Sort.by(Sort.Order.desc("_id"))), BroadcastSession.class);
    }

    public List<BroadcastSession> findBy(Criteria c) {
        return this.mongoTemplate.find(new Query(c), BroadcastSession.class);
    }

    public List<BroadcastSession> findBy(Criteria c, int start, int limit) {
        return this.mongoTemplate.find(new Query(c).skip(start).limit(limit), BroadcastSession.class);
    }

    public long count(Criteria c) {
        Query query = new Query(c);
        return this.mongoTemplate.count(query, BroadcastSession.class);
    }

    public void updateStarted(ObjectId id, boolean started) {
        Query q = new Query(Criteria.where("_id").is(id));

        this.mongoTemplate.updateFirst(q, new Update().set("started", started), BroadcastSession.class);
    }

    public void updateTime(ObjectId id, Date dateTime) {
        Query q = new Query(Criteria.where("_id").is(id));

        this.mongoTemplate.updateFirst(q, new Update().set("updateTime", dateTime), BroadcastSession.class);
    }

    public void remove(String id) {
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), BroadcastSession.class);
    }
}
