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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Component;

import java.io.Serializable;
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
     * <p>当{@code score}值过大时，分值会变得不太精确，且权重也会变得不稳定（即同分值排名）。
     *
     * <p>当{@code weight}数值过大时，可能会进入到分值，建议保留第一位精度(0.0xxx...)，也就是实际精度的设置比待设置的精度大1。
     *
     * <p>当{@code weight}位数过多时，可能会丢失进度，从而导致权重变得不稳定，
     * 建议位数小于16位(其中1位保留，即15位为有效权重值，0.0xxxxxxxxxxxxxxx)，实测那15个x小于{@code 1L << 33}时很稳定。
     *
     * <p>建议总数位14位进行自行设计分值位数及权重位数。当需要权重时，建议至少2位（其中1位位保留精度位，即0.0x）。
     * 推荐分值位9位(值小于2^31-1也行)，权重位6位（权重需要循环使用了），经测试很稳定。
     *
     * @param rankName
     * @param memberName
     * @param score      分值
     * @param weight     同分时排名的权重，取值范围为(-1,1)
     * @return 上一次的分值：
     * {@code null}, if rank not exist or {@code member} not in rank;
     * otherwise return {@code member} real rank score
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Long joinRank(String rankName, String memberName, long score, BigDecimal weight) {
        checkParamsForJoinRank(rankName, memberName, score, weight);

        final String rankKey = getRankKey(rankName);

        double finalAdditiveScore = (double) score;
        if (weight.doubleValue() != 0) {
            // 需要加上的分值（真实分值+权重值）
            BigDecimal dScore = BigDecimal.valueOf(score).add(weight);

            final double oldScoreWeight = getScoreWeight(doGetRankScore(rankName, memberName));
            if (oldScoreWeight > 0) {
                // 扣除上一次的分值的权重
                dScore = dScore.subtract(BigDecimal.valueOf(oldScoreWeight));
            }
            finalAdditiveScore = dScore.doubleValue();
        }

        Double totalScore = redisComponent.zIncrBy(rankKey, memberName, finalAdditiveScore);
        return Objects.isNull(totalScore) ? null : totalScore.longValue();
    }

    /**
     * 获取{@code memberName}在{@code rankName}榜的分数。
     *
     * @param rankName   null return null
     * @param memberName null or empty return null
     * @return {@code null}, if rank not exist or {@code memberName} not in rank list;
     * otherwise return {@code memberName} rank score
     * @throws NullPointerException
     */
    public Long getRankScore(String rankName, String memberName) {
        Double score = doGetRankScore(rankName, memberName);
        return Objects.isNull(score) ? null : score.longValue();
    }

    /**
     * 获取{@code member}在{@code rankName}榜的排名。
     *
     * @param rankName   null return null
     * @param memberName null or empty return null
     * @return {@code null}, if rank not exist or {@code memberName} not in rank list;
     * otherwise return {@code memberName} real rank number
     * @throws NullPointerException
     */
    public Long getRankNumber(String rankName, String memberName) {
        Objects.requireNonNull(rankName, "rankName must not be null");
        Objects.requireNonNull(memberName, "memberName must not be null");

        Long rankNum = redisComponent.zRevrank(getRankKey(rankName), memberName);
        return Objects.isNull(rankNum) ? null : rankNum + 1;
    }

    /**
     * 获取排行榜。
     *
     * @param rankName
     * @param start    查询的排行榜开始的名次，从1开始
     * @param end      查询的排行榜结束的名次
     * @return
     */
    public List<RankMember> getRankList(String rankName, int start, int end) {
        Objects.requireNonNull(rankName, "rankName must not be null");

        Set<RedisZSetCommands.Tuple> rank = redisComponent.zRevRangeWithScores(
                getRankKey(rankName),
                start - 1,
                end - 1
        );
        return mappingForRankList(rank);
    }

    /**
     * 获取{@code member}在{@code rankName}榜的分数。
     *
     * @param rankName   null return null
     * @param memberName null or empty return null
     * @return {@code null}, if rank not exist or {@code member} not in rank list;
     * otherwise return {@code member} rank score
     * @throws NullPointerException
     */
    Double doGetRankScore(String rankName, String memberName) {
        Objects.requireNonNull(rankName, "rankName must not be null");
        Objects.requireNonNull(memberName, "memberName must not be null");
        return redisComponent.zScore(getRankKey(rankName), memberName);
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
        if (!((dWeight >= 0 && dWeight < 1) || (dWeight < 0 && dWeight > -1))) {
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

    private static List<RankMember> mappingForRankList(Set<RedisZSetCommands.Tuple> rank) {
        if (Objects.nonNull(rank) && !rank.isEmpty()) {
            List<RankMember> resultList = new ArrayList<>(rank.size());
            for (RedisZSetCommands.Tuple item : rank) {
                String memberName = new String(item.getValue());
                Long score = item.getScore().longValue();

                resultList.add(new RankMember(memberName, score));
            }
            return resultList;
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    public static class RankMember implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private Long score;

        public RankMember() {}

        public RankMember(String name, Long score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getScore() {
            return score;
        }

        public void setScore(Long score) {
            this.score = score;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RankMember that = (RankMember) o;
            return name.equals(that.name) &&
                    score.equals(that.score);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, score);
        }

        @Override
        public String toString() {
            return "RankMember{" +
                    "name='" + name + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
}
