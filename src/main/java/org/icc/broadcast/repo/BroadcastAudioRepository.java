/**
 *
 */
package org.icc.broadcast.repo;

import org.bson.types.ObjectId;
import org.icc.broadcast.entity.BroadcastAudio;
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
public class BroadcastAudioRepository extends AbstractRepository<BroadcastAudio> {

    public BroadcastAudio findById(ObjectId id) {
        return this.findById(BroadcastAudio.class, id);
    }

    public BroadcastAudio findOneBy(Criteria c) {
        return this.mongoTemplate.findOne(new Query(c).with(Sort.by(Sort.Order.desc("_id"))), BroadcastAudio.class);
    }

    public List<BroadcastAudio> findBy(Criteria c) {
        return this.mongoTemplate.find(new Query(c), BroadcastAudio.class);
    }

    public List<BroadcastAudio> findBy(Criteria c, int start, int limit) {
        return this.mongoTemplate.find(new Query(c).skip(start).limit(limit), BroadcastAudio.class);
    }
}
