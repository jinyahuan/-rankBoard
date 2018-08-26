/*
 * Copyright (c) 2018 The Rank Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.jinyahuan.lab.rank;

import cn.jinyahuan.common.BaseSpringIntegrationTest;
import cn.jinyahuan.common.redis.component.impl.RedisComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RedisRankLabTest extends BaseSpringIntegrationTest {
    @Autowired
    private RedisRankLab redisRankLab;
    @Autowired
    private RedisComponent redisComponent;

    @Test
    public void saveRank() {
        String rankName = "test";
        String rankKey = redisRankLab.getRankKey(rankName);
        String rankOptKey = redisRankLab.getRankOperationNumberKey(rankName);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        BigDecimal score = new BigDecimal("100");

        String memberName1 = "jin_1";
        redisRankLab.saveRank(rankName, memberName1, score);

        String memberName2 = "jin_2";
        assertEquals(new Long(9), redisComponent.incrBy(rankOptKey, 8));
        redisRankLab.saveRank(rankName, memberName2, score);

        String memberName3 = "jin_3";
        assertEquals(new Long(99), redisComponent.incrBy(rankOptKey, 89));
        redisRankLab.saveRank(rankName, memberName3, score);

        List<Map<String, Object>> rankList = redisRankLab.getRank(rankName, 1, 10);
        String result = "[{member=jin_3, score=100.10}, {member=jin_2, score=100.10}, {member=jin_1, score=100.10}]";
        assertEquals(result, rankList + "");

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }
}
