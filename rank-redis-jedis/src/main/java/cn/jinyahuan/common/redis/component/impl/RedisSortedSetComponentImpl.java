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

package cn.jinyahuan.common.redis.component.impl;

import cn.jinyahuan.common.redis.component.RedisSortedSetComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RedisSortedSetComponentImpl implements RedisSortedSetComponent {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Boolean zAdd(String key, String member, double score) {
        if (Boolean.logicalOr(Objects.isNull(key), Objects.isNull(member))) {
            return null;
        }
        return (Boolean) stringRedisTemplate.execute(
                (RedisCallback) connection -> connection.zAdd(key.getBytes(), score, member.getBytes()));
    }

    @Override
    public Double zIncrBy(String key, String member, double score) {
        if (Boolean.logicalOr(Objects.isNull(key), Objects.isNull(member))) {
            return null;
        }
        return (Double) stringRedisTemplate.execute(
                (RedisCallback) connection -> connection.zIncrBy(key.getBytes(), score, member.getBytes()));
    }

    @Override
    public Double zScore(String key, String member) {
        if (Boolean.logicalOr(Objects.isNull(key), Objects.isNull(member))) {
            return null;
        }
        return (Double) stringRedisTemplate.execute(
                (RedisCallback) connection -> connection.zScore(key.getBytes(), member.getBytes()));
    }

    @Override
    public Long zRevrank(String key, String member) {
        if (Boolean.logicalOr(Objects.isNull(key), Objects.isNull(member))) {
            return null;
        }
        return (Long) stringRedisTemplate.execute(
                (RedisCallback) connection -> connection.zRevRank(key.getBytes(), member.getBytes()));
    }

    @Override
    public Set<RedisZSetCommands.Tuple> zRevRangeWithScores(String key, long start, long stop) {
        if (Objects.isNull(key)) {
            return null;
        }
        return (Set<RedisZSetCommands.Tuple>) stringRedisTemplate.execute(
                (RedisCallback) connection -> connection.zRevRangeWithScores(key.getBytes(), start, stop));
    }
}
