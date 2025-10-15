/**
 *
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.icc.broadcast.entity.BroadcastSession;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

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
}
