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

import cn.jinyahuan.common.redis.component.impl.RedisComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 排行榜 redis 实现的实验室。
 *
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RedisRankLab {
    static final String KEY_RANK_PREFIX = "rank:";

    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * <p>由于 zset 中 score 是以双精度的浮点数存储，相当于 java 中的{@link Double}。
     *
     * <p>所以当{@code score}值过大时，分值会变得不太精确，且权重也会变得不稳定（即同分值排名），建议不大于{@code 1L << 52}。
     *
     * <p>所以当{@code weight}数值过大时，可能会进入到分值，建议保留第一位精度(0.0xxx...)，也就是实际精度的设置比待设置的精度大1。
     *
     * <p>所以当{@code weight}位数过多时，可能会丢失进度，从而导致权重变得不稳定，
     * 建议位数小于16位(其中1位保留，即15位为有效权重值，0.0xxxxxxxxxxxxxxx)。
     *
     * @param rankName
     * @param memberName
     * @param score      分值
     * @param weight     同分时排名的权重，值为大于0且小于1的小数
     * @return 上一次的分值：
     * {@code null}, if rank not exist or {@code member} not in rank;
     * otherwise return {@code member} real rank score
     */
    public Long joinRank(String rankName, String memberName, long score, BigDecimal weight) {
        checkParamsForJoinRank(rankName, memberName, score, weight);

        final String rankKey = getRankKey(rankName);

        // 需要加上的分值（真实分值+权重值）
        BigDecimal additiveScore = BigDecimal.valueOf(score).add(weight);

        final double oldScoreWeight = getScoreWeight(getRankScore(rankName, memberName));
        // 扣除上一次的分值的权重
        if (oldScoreWeight > 0) {
            additiveScore = additiveScore.subtract(BigDecimal.valueOf(oldScoreWeight));
        }

        Double totalScore = redisComponent.zIncrBy(rankKey, memberName, additiveScore.doubleValue());
        return Objects.isNull(totalScore) ? null : totalScore.longValue();
    }

    /**
     * 获取{@code member}在{@code rankName}榜的分数。
     * <p>
     * 注意：有个副作用，当 score 为负数时，返回 0。
     * </p>
     *
     * @param rankName   null return null
     * @param memberName null or empty return null
     * @return {@code null}, if rank not exist or {@code member} not in rank list;
     * otherwise return {@code member} rank score
     * @throws NullPointerException
     */
    public Double getRankScore(String rankName, String memberName) {
        Objects.requireNonNull(rankName, "rankName must not be null");
        Objects.requireNonNull(memberName, "memberName must not be null");
        return redisComponent.zScore(getRankKey(rankName), memberName);
    }

    /**
     * 获取{@code member}在{@code rankName}榜的排名。
     *
     * @param rankName null return null
     * @param member   null or empty return null
     * @return {@code null}, if params is null; {@code -1}, if rank not exist or {@code member} not in rank;
     * otherwise return {@code member} real rank number
     */
    public Long getRankNumber(String rankName, String member) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key) || StringUtils.isEmpty(member)) {
            return null;
        }
        Long rankNum = redisComponent.zRevrank(key, member);
        return Objects.nonNull(rankNum) ? rankNum + 1 : -1;
    }

    /**
     * @param rankName
     * @param start    查询的排行榜开始的名次，从1开始
     * @param end      查询的排行榜结束的名次
     * @return
     */
    public List<Map<String, Object>> getRanks(String rankName, int start, int end) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return Collections.EMPTY_LIST;
        }

        Set<RedisZSetCommands.Tuple> rank = redisComponent.zRevRangeWithScores(key, start - 1, end - 1);
        if (Objects.nonNull(rank) && !rank.isEmpty()) {
            List<Map<String, Object>> resultList = new ArrayList<>(rank.size() + 1);

            for (RedisZSetCommands.Tuple item : rank) {
                String memberName = new String(item.getValue());
                String score = new BigDecimal(item.getScore().toString())
                        .setScale(RankWeightUtils.getDefaultDecimalPlaces(), DEFAULT_ROUNDING_MODE)
                        .toPlainString();

                Map<String, Object> eRank = new HashMap<>(4);
                eRank.put("member", memberName);
                eRank.put("score", score);
                resultList.add(eRank);
            }
            return resultList;

        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    static String getRankKey(String rankName) {
        return KEY_RANK_PREFIX + rankName;
    }

    /**
     * @param rankName
     * @param memberName
     * @param score
     * @param weight
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    static void checkParamsForJoinRank(String rankName, String memberName, long score, BigDecimal weight) {
        Objects.requireNonNull(rankName, "rankName must not be null");
        Objects.requireNonNull(memberName, "memberName must not be null");
        Objects.requireNonNull(weight, "weight must not be null");

        final double dWeight = weight.doubleValue();
        if (!((dWeight > 0 && dWeight < 1) || (dWeight < 0 && dWeight > -1))) {
            throw new IllegalArgumentException("weight value range must in [-1 < weight < 1]");
        }
    }

    /**
     * 获取分值中的权重值。
     *
     * @param rankScore
     * @return
     */
    static double getScoreWeight(Double rankScore) {
        double diff = 0;
        if (Objects.nonNull(rankScore) && rankScore != 0) {
            final double oldRankScoreTemp = Math.abs(rankScore);
            // 权重
            diff = oldRankScoreTemp - (long) oldRankScoreTemp;
        }
        return diff;
    }

    public static void main(String[] args) {
        System.out.println(1L << 52);
        System.out.println(Long.MAX_VALUE);
    }
}
