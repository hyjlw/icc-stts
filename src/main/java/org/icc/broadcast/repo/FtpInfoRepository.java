/**
 *
 */
package org.icc.broadcast.repo;

import org.icc.broadcast.entity.FtpInfo;
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
public class FtpInfoRepository extends AbstractRepository<FtpInfo> {
    public FtpInfo findOneBy(Criteria c) {
        return this.mongoTemplate.findOne(new Query(c).with(Sort.by(Sort.Order.desc("createDate"))), FtpInfo.class);
    }

    public List<FtpInfo> findBy(Criteria c) {
        return this.mongoTemplate.find(new Query(c), FtpInfo.class);
    }

    public void remove(String id) {
        this.mongoTemplate.remove(new Query(Criteria.where("_id").is(id)), FtpInfo.class);
    }
}
