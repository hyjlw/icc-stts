/**
 *
 */
package org.icc.broadcast.repo;

import cn.hutool.core.collection.CollectionUtil;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.icc.broadcast.entity.AudioMeta;
import org.icc.broadcast.entity.BroadcastAudio;
import org.icc.broadcast.entity.ProcessTime;
import org.icc.broadcast.entity.TtsTime;
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
@Slf4j
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

    public void addAudioMetas(long serialId, List<AudioMeta> audioMetas) {
        if(CollectionUtil.isEmpty(audioMetas)) {
            return;
        }

        Query query = new Query(Criteria.where("serialId").is(serialId));
        Update update = new Update();
        update.push("audioMetas").each(audioMetas.toArray());
        update.set("updateTime", new Date());

        UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, BroadcastAudio.class);
        log.info("update audioMetas {} ", updateResult.getMatchedCount());
    }

    public void addProcessTimes(long serialId, List<ProcessTime> times) {
        if(CollectionUtil.isEmpty(times)) {
            return;
        }

        Query query = new Query(Criteria.where("serialId").is(serialId));
        Update update = new Update();
        update.push("times").each(times.toArray());
        update.set("updateTime", new Date());

        this.mongoTemplate.updateFirst(query, update, BroadcastAudio.class);
    }

    public void addTtsTimes(long serialId, List<TtsTime> times) {
        if(CollectionUtil.isEmpty(times)) {
            return;
        }

        Query query = new Query(Criteria.where("serialId").is(serialId));
        Update update = new Update();
        update.push("ttsTimes").each(times.toArray());
        update.set("updateTime", new Date());

        UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, BroadcastAudio.class);
        log.info("update ttsTimes {} ", updateResult.getMatchedCount());
    }
}
