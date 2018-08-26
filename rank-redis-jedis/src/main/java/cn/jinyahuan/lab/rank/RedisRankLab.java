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
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 排行榜 redis 实现的实验室。
 * <pre>
 * 默认精度的情况下（2位小数）,支持至少千亿级的 score。
 * </pre>
 *
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RedisRankLab {
    // TODO 设计排序方式 JinYahuan

    private static final String KEY_RANK_PREFIX = "rank:";
    private static final String KEY_TEMPLATE_RANK_OPERATION_COUNT = KEY_RANK_PREFIX + "%s:operationCount";

    /**
     * 默认的分值的有效小数位数。
     */
    public static final int DEFAULT_SCORE_DECIMAL_PLACES = 2;

    @Autowired
    private RedisComponent redisComponent;

    public long saveRank(String rankName, String memberName, Long score) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        if (StringUtils.isEmpty(memberName) || Objects.isNull(score)) {
            return 0;
        }

        // 计算排行榜的排序权重
        long rankOperationNumber = logRankOperationNumber(rankName);
        BigDecimal weight = RankWeightUtils.computeWeightByDefaultDecimalPlaces(rankOperationNumber);

        double additiveScore = weight.doubleValue() + score;

        Double totalScore = redisComponent.zIncrBy(key, memberName, additiveScore);

        return totalScore.longValue();
    }

    // 分数相同时 后达标的排在前面
    public double saveRank(String rankName, String member, BigDecimal score) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return 0d;
        }
        if (StringUtils.isEmpty(member) || Objects.isNull(score)) {
            return 0d;
        }

        // 计算排行榜的排序权重
        long rankOperationNumber = logRankOperationNumber(rankName);
        BigDecimal weight = RankWeightUtils.computeWeightByDefaultDecimalPlaces(rankOperationNumber);

        BigDecimal additiveScore = score.setScale(DEFAULT_SCORE_DECIMAL_PLACES, RoundingMode.DOWN)
                .add(weight);

        return redisComponent.zIncrBy(key, member, additiveScore.doubleValue());
    }

    public Double getScore(String rankName, String member) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key) || StringUtils.isEmpty(member)) {
            return 0d;
        }
        Double score = redisComponent.zScore(key, member);
        return Objects.isNull(score) ? 0d : score;
    }

    public Long getRank(String rankName, String member) {
        return null;
    }

    /**
     * @param rankName
     * @param start    查询的排行榜开始的名次，从1开始
     * @param end      查询的排行榜结束的名次
     * @return
     */
    public List<Map<String, Object>> getRank(String rankName, int start, int end) {
        String key = getRankKey(rankName);
        if (Objects.isNull(key)) {
            return Collections.EMPTY_LIST;
        }

        Set<RedisZSetCommands.Tuple> rank = redisComponent.zRevRangeWithScores(key, start - 1, end - 1);
        if (Objects.nonNull(rank) && !rank.isEmpty()) {
            List<Map<String, Object>> resultList = new ArrayList<>(rank.size() + 1);

            for (RedisZSetCommands.Tuple item : rank) {
                String memberName = new String(item.getValue());
                String score = new BigDecimal(item.getScore().toString()).setScale(DEFAULT_SCORE_DECIMAL_PLACES, RoundingMode.DOWN).toPlainString();

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

    public long getRankOperationNumber(String rankName) {
        String key = getRankOperationNumberKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        String cacheNumber = redisComponent.get(key);
        return NumberUtils.toLong(cacheNumber, 0);
    }

    private long logRankOperationNumber(String rankName) {
        String key = getRankOperationNumberKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        return redisComponent.incr(key);
    }

    String getRankKey(String rankName) {
        if (StringUtils.isEmpty(rankName)) {
            return null;
        }
        return KEY_RANK_PREFIX + rankName;
    }

    String getRankOperationNumberKey(String rankName) {
        if (StringUtils.isEmpty(rankName)) {
            return null;
        }
        return String.format(KEY_TEMPLATE_RANK_OPERATION_COUNT, rankName);
    }
}
