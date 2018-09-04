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

    public long saveRank(String rankName, String memberName, Long score, BigDecimal weight) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        if (StringUtils.isEmpty(memberName)
                || Objects.isNull(score)
                || (Objects.isNull(weight) || weight.compareTo(BigDecimal.ZERO) < 0)) {
            return 0;
        }

        double additiveScore = weight.doubleValue() + score;

        Double totalScore = redisComponent.zIncrBy(key, memberName, additiveScore);
        return totalScore.longValue();
    }

    public double saveRank(String rankName, String memberName, Double score, BigDecimal weight) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return 0d;
        }
        if (StringUtils.isEmpty(memberName)
                || Objects.isNull(score)
                || (Objects.isNull(weight) || weight.compareTo(BigDecimal.ZERO) < 0)) {
            return 0d;
        }

        BigDecimal additiveScore = new BigDecimal(score.toString())
                .setScale(RankWeightUtils.getDefaultDecimalPlaces(), DEFAULT_ROUNDING_MODE)
                .add(weight);

        return redisComponent.zIncrBy(key, memberName, additiveScore.doubleValue());
    }

    /**
     * 获取{@code member}在{@code rankName}榜的分数。
     * <p>
     * 注意：有个副作用，当 score 为负数时，返回 0。
     * </p>
     *
     * @param rankName null return null
     * @param member   null or empty return null
     * @return {@code null}, if params is null; {@code -1}, if rank not exist or {@code member} not in rank;
     * otherwise return {@code member} real rank score
     */
    public Double getRankScore(String rankName, String member) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key) || StringUtils.isEmpty(member)) {
            return null;
        }
        Double score = redisComponent.zScore(key, member);
        return Objects.isNull(score)
                ? -1
                : score > 0 ? score : 0;
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

        } else {
            return Collections.EMPTY_LIST;
        }
    }

    static String getRankKey(String rankName) {
        if (StringUtils.isEmpty(rankName)) {
            return null;
        }
        return KEY_RANK_PREFIX + rankName;
    }
}
