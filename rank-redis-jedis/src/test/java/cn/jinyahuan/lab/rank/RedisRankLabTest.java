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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RedisRankLabTest extends BaseSpringIntegrationTest {
    @Autowired
    private RedisRankLab redisRankLab;
    @Autowired
    private RankWeightComponent rankWeightComponent;
    @Autowired
    private RedisComponent redisComponent;

    @Test
    public void testJoinRank() {
        final String rankName = "saveRankByLong";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getKey(rankName);
        final int faultDecimalPlaces = 2;
        final Long score = Long.valueOf(100);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        String memberName1 = "jin_1";
        assertEquals(score, redisRankLab.joinRank(rankName, memberName1, score, BigDecimal.ZERO));
        redisComponent.del(rankKey);

        String memberName2 = "jin_2";
        long weight = rankWeightComponent.offer(rankName);
        BigDecimal finalWeight2 = RankWeightUtils.computeWeight(weight, faultDecimalPlaces);
        assertEquals(score, redisRankLab.joinRank(rankName, memberName2, score, finalWeight2));

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

//    @Ignore
    @Test
    public void testRankNo() {
        // 测试的次数
        final int testCount = 100;
        // 同分值member数
        final int memberCount = 4;

        final String rankName = "testSameScoreRankNo";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getKey(rankName);
        final Long incrementScore = 1L;
        final Long initScore = ((long) Integer.MAX_VALUE) - (incrementScore * testCount);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        final long initWeightValue = 99_999 - (testCount * memberCount);
        rankWeightComponent.init(rankName, initWeightValue);
        assertEquals(initWeightValue, rankWeightComponent.peek(rankName));

        for (int i = 0; i < testCount; i++) {
            // 第一次进行初始化
            if (i == 0) {
                for (int j = 1; j <= memberCount; j++) {
                    long weight = rankWeightComponent.offerCircular(rankName);
                    BigDecimal finalWeight = RankWeightUtils.computeWeight(weight, 1);
                    redisRankLab.joinRank(rankName, "m" + j, initScore, finalWeight);
                }
            }
            // 非第一次循环进行累加
            else {
                // 偶数正向加
                if ((i & 1) == 0) {
                    for (int j = 1; j <= memberCount; j++) {
                        long weight = rankWeightComponent.offerCircular(rankName);
                        BigDecimal finalWeight = RankWeightUtils.computeWeight(weight, 1);
                        redisRankLab.joinRank(rankName, "m" + j, incrementScore, finalWeight);
                    }
                }
                // 奇数反向加
                else {
                    for (int j = memberCount; j > 0; j--) {
                        long weight = rankWeightComponent.offerCircular(rankName);
                        BigDecimal finalWeight = RankWeightUtils.computeWeight(weight, 1);
                        redisRankLab.joinRank(rankName, "m" + j, incrementScore, finalWeight);
                    }
                }
            }

            System.out.println(redisRankLab.getRankList(rankName, 1, memberCount));
        }

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

    @Test
    public void testGetRankScore() {
        String rankName = "rankScore";
        String rankKey = redisRankLab.getRankKey(rankName);
        String memberName = "jin_score";
        String rankOptKey = rankWeightComponent.getKey(rankName);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        assertEquals(null, redisRankLab.getRankScore(rankName, memberName));
    }

    @Test
    public void testGetRankNumber() {
        String rankName = "rankNumber";
        String rankKey = redisRankLab.getRankKey(rankName);
        String memberName = "jin_number";
        String rankOptKey = rankWeightComponent.getKey(rankName);

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        assertEquals(null, redisRankLab.getRankNumber(rankName, memberName));
    }

    @Test
    public void testGetRanks() {
        final String rankName = "ranks";
        final String rankKey = redisRankLab.getRankKey(rankName);
        final String rankOptKey = rankWeightComponent.getKey(rankName);
        final int faultDecimalPlaces = 2;
        final long score = 100L;

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);

        String memberName1 = "jin_1";
        long rankOptNum1 = rankWeightComponent.offer(rankName);
        BigDecimal weight1 = RankWeightUtils.computeWeight(rankOptNum1, faultDecimalPlaces);
        redisRankLab.joinRank(rankName, memberName1, score, weight1);

        String memberName2 = "jin_2";
        long rankOptNum2 = rankWeightComponent.offer(rankName);
        BigDecimal weight2 = RankWeightUtils.computeWeight(rankOptNum2, faultDecimalPlaces);
        redisRankLab.joinRank(rankName, memberName2, score, weight2);

        String memberName3 = "jin_3";
        long rankOptNum3 = rankWeightComponent.offer(rankName);
        BigDecimal weight3 = RankWeightUtils.computeWeight(rankOptNum3, faultDecimalPlaces);
        redisRankLab.joinRank(rankName, memberName3, score, weight3);

        assertEquals(
                "[RankMember{name='jin_3', score=100}, RankMember{name='jin_2', score=100}, RankMember{name='jin_1', score=100}]",
                redisRankLab.getRankList(rankName, 1, 10) + "");

        redisComponent.del(rankKey);
        redisComponent.del(rankOptKey);
    }

    @Test
    public void testGetRankKey() {
        assertEquals("rank:null", RedisRankLab.getRankKey(null));
        assertEquals("rank:", RedisRankLab.getRankKey(""));

        assertEquals("rank:age", RedisRankLab.getRankKey("age"));
    }

    @Test
    public void testGetScoreWeight() {
        assertEquals(0, RedisRankLab.getScoreWeight(null), 0);
        assertEquals(0, RedisRankLab.getScoreWeight(0d), 0);
        assertEquals(0, RedisRankLab.getScoreWeight(1d), 0);

        final int floatTestCount = 16;
        for (int i = 1; i <= floatTestCount; i++) {
            // 浮点数
            final double score_f1 = BigDecimal.ONE.movePointLeft(i).doubleValue();
            final double score_f9 = BigDecimal.ONE.subtract(BigDecimal.ONE.movePointLeft(i)).doubleValue();
            try {
                assertEquals(score_f1, RedisRankLab.getScoreWeight(score_f1), 0);
                assertEquals(score_f9, RedisRankLab.getScoreWeight(score_f9), 0);
            } catch (Throwable ex) {
                System.out.println(i + " score_1=" + score_f1 + " score_f9=" + score_f9);
                throw ex;
            }
        }

        final int integerTestCount = 18;
        for (int i = 1; i <= integerTestCount; i++) {
            // 浮点整数
            final double score_i9 = BigDecimal.ONE.movePointRight(i).subtract(BigDecimal.ONE).doubleValue();
            try {
                assertEquals(0, RedisRankLab.getScoreWeight(score_i9), 0);
            } catch (Throwable ex) {
                System.out.println(i + " score_i9=" + score_i9);
                throw ex;
            }
        }
    }

    @Test
    public void testRangeIncludeZeroAndExcludeOne() {
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.1));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.01));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.0001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.00001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.0000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.0000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.00000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1.000000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(1));

        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.999999999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.99999999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.9999999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.999999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.99999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.9999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.999));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.99));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.9));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.1));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.01));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.0001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.00001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.000001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.0000001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.00000001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.00000001));
        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0.000000001));

        assertTrue(RedisRankLab.rangeIncludeZeroAndExcludeOne(0));

        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.000000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.00000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.0000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.00001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.0001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.01));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.1));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.9));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.99));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.9999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.99999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.999999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.9999999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.99999999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-0.999999999));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.1));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.01));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.0001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.00001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.0000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.0000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.00000001));
        assertFalse(RedisRankLab.rangeIncludeZeroAndExcludeOne(-1.000000001));
    }

    @Test
    public void testRangeExcludeNegativeOneAndIncludeZero() {
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.1));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.01));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.0001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.00001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.0000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.0000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.00000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1.000000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(1));

        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.999999999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.99999999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.9999999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.999999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.99999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.9999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.999));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.99));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.9));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.1));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.01));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.0001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.00001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.0000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.00000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.00000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0.000000001));

        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(0));

        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.1));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.01));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.0001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.00001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.000001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.0000001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.00000001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.000000001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.0000000001));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.9));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.99));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.9999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.99999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.999999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.9999999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.99999999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.999999999));
        assertTrue(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-0.9999999999));

        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.1));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.01));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.0001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.00001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.0000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.0000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.00000001));
        assertFalse(RedisRankLab.rangeExcludeNegativeOneAndIncludeZero(-1.000000001));
    }
}
