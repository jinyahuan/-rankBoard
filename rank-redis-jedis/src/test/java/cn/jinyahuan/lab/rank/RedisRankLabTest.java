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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RedisRankLabTest extends BaseSpringIntegrationTest {
    @Autowired
    private RedisRankLab redisRankLab;
    @Autowired
    private RankWeightComponent rankWeightComponent;
    @Autowired
    private RedisComponent redisComponent;

    @Test
    public void testSaveRankByLong() {
        final String rankName = "saveRankByLong";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getRankOperationNumberKey(rankName);
        final int faultDecimalPlaces = 2;
        final long score = 100L;

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        String memberName1 = "jin_1";
        LocalDateTime dateTime = LocalDateTime.now();
        BigDecimal weight1 = RankWeightUtils.computeWeight(dateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(dateTime)).toEpochMilli(), faultDecimalPlaces);
        assertEquals(score, redisRankLab.saveRank(rankName, memberName1, score, weight1));
        redisComponent.del(rankKey);

        String memberName2 = "jin_2";
        long rankOptNum = rankWeightComponent.logRankOperationNumber(rankName);
        BigDecimal weight2 = RankWeightUtils.computeWeight(rankOptNum, faultDecimalPlaces);
        assertEquals(score, redisRankLab.saveRank(rankName, memberName2, score, weight2));

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

    @Test
    public void testSaveRankByDouble() {
        final String rankName = "saveRankByDouble";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getRankOperationNumberKey(rankName);
        final int faultDecimalPlaces = 2;
        final double score = 100.01;

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        String memberName1 = "jin_1";
        LocalDateTime dateTime = LocalDateTime.now();
        BigDecimal weight1 = RankWeightUtils.computeWeight(dateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(dateTime)).toEpochMilli(), faultDecimalPlaces);
        assertEquals(score, redisRankLab.saveRank(rankName, memberName1, score, weight1), Math.pow(10, faultDecimalPlaces + 1));
        redisComponent.del(rankKey);

        String memberName2 = "jin_2";
        long rankOptNum = rankWeightComponent.logRankOperationNumber(rankName);
        BigDecimal weight2 = RankWeightUtils.computeWeight(rankOptNum, faultDecimalPlaces);
        assertEquals(score, redisRankLab.saveRank(rankName, memberName2, score, weight2), Math.pow(10, faultDecimalPlaces + 1));

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

    @Test
    public void testGetRankScore() {
        String rankName = "rankScore";
        String rankKey = redisRankLab.getRankKey(rankName);
        String memberName = "jin_score";
        String rankOptKey = rankWeightComponent.getRankOperationNumberKey(rankName);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        assertEquals(new Double(-1), redisRankLab.getRankScore(rankName, memberName));
    }

    @Test
    public void testGetRankNumber() {
        String rankName = "rankNumber";
        String rankKey = redisRankLab.getRankKey(rankName);
        String memberName = "jin_number";
        String rankOptKey = rankWeightComponent.getRankOperationNumberKey(rankName);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        assertEquals(new Long(-1), redisRankLab.getRankNumber(rankName, memberName));
    }

    @Test
    public void testGetRanks() {
        assertEquals(Collections.EMPTY_LIST, redisRankLab.getRanks(null, 1, 10));

        final String rankName = "ranks";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getRankOperationNumberKey(rankName);
        final int faultDecimalPlaces = 2;
        final long score = 100L;

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        String memberName1 = "jin_1";
        long rankOptNum1 = rankWeightComponent.logRankOperationNumber(rankName);
        BigDecimal weight1 = RankWeightUtils.computeWeight(rankOptNum1, faultDecimalPlaces);
        redisRankLab.saveRank(rankName, memberName1, score, weight1);

        String memberName2 = "jin_2";
        long rankOptNum2 = rankWeightComponent.logRankOperationNumber(rankName);
        BigDecimal weight2 = RankWeightUtils.computeWeight(rankOptNum2, faultDecimalPlaces);
        redisRankLab.saveRank(rankName, memberName2, score, weight2);

        String memberName3 = "jin_3";
        long rankOptNum3 = rankWeightComponent.logRankOperationNumber(rankName);
        BigDecimal weight3 = RankWeightUtils.computeWeight(rankOptNum3, faultDecimalPlaces);
        redisRankLab.saveRank(rankName, memberName3, score, weight3);

        assertEquals(
                "[{member=jin_3, score=100.00}, {member=jin_2, score=100.00}, {member=jin_1, score=100.00}]",
                redisRankLab.getRanks(rankName, 1, 10) + "");

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

    @Test
    public void testGetRankKey() {
        assertEquals(null, RedisRankLab.getRankKey(null));
        assertEquals(null, RedisRankLab.getRankKey(""));

        assertEquals("rank:age", RedisRankLab.getRankKey("age"));
    }
}
