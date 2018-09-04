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

import static org.junit.Assert.*;

public class RankWeightComponentTest extends BaseSpringIntegrationTest {
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private RankWeightComponent rankWeightComponent;

    @Test
    public void testGetRankOperationNumber() {
        String rankName1 = "rankOpt";
        String rankOperationNumberKey1 = RankWeightComponent.getRankOperationNumberKey(rankName1);
        redisComponent.del(rankOperationNumberKey1);
        assertEquals(0, rankWeightComponent.getRankOperationNumber(null));
        assertEquals(0, rankWeightComponent.getRankOperationNumber(""));

        assertEquals(0, rankWeightComponent.getRankOperationNumber(rankName1));

        redisComponent.incrBy(rankOperationNumberKey1, 100);
        assertEquals(100, rankWeightComponent.getRankOperationNumber(rankName1));

        redisComponent.del(rankOperationNumberKey1);
    }

    @Test
    public void testLogRankOperationNumber() {
        String rankName1 = "logRankOpt";
        String rankOperationNumberKey1 = RankWeightComponent.getRankOperationNumberKey(rankName1);
        redisComponent.del(rankOperationNumberKey1);
        assertEquals(0, rankWeightComponent.logRankOperationNumber(null));
        assertEquals(0, rankWeightComponent.logRankOperationNumber(""));
        assertEquals(1, rankWeightComponent.logRankOperationNumber(rankName1));
        assertEquals(2, rankWeightComponent.logRankOperationNumber(rankName1));
        assertEquals(3, rankWeightComponent.logRankOperationNumber(rankName1));
        redisComponent.del(rankOperationNumberKey1);
    }

    @Test
    public void testGetRankOperationNumberKey() {
        assertEquals(null, RankWeightComponent.getRankOperationNumberKey(null));
        assertEquals(null, RankWeightComponent.getRankOperationNumberKey(""));
        assertEquals("rank:age:operationCount", RankWeightComponent.getRankOperationNumberKey("age"));
    }
}
