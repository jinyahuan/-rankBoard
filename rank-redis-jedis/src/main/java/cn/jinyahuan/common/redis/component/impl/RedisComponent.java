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

import cn.jinyahuan.common.redis.component.RedisConnectionComponent;
import cn.jinyahuan.common.redis.component.RedisSortedSetComponent;
import cn.jinyahuan.common.redis.component.RedisStringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RedisComponent {
    @Autowired
    private RedisStringComponent redisStringComponent;
    @Autowired
    private RedisSortedSetComponent redisSortedSetComponent;
    @Autowired
    private RedisConnectionComponent redisConnectionComponent;

    public String get(String key) {
        return redisStringComponent.get(key);
    }

    public Long incr(String key) {
        return redisStringComponent.incr(key);
    }

    public Double zIncrBy(String key, String member, double score) {
        return redisSortedSetComponent.zIncrBy(key, member, score);
    }

    public Double zScore(String key, String member) {
        return redisSortedSetComponent.zScore(key, member);
    }

    public Long zRevrank(String key, String member) {
        return redisSortedSetComponent.zRevrank(key, member);
    }

    public Set<RedisZSetCommands.Tuple> zRevRangeWithScores(String key, long start, long stop) {
        return redisSortedSetComponent.zRevRangeWithScores(key, start, stop);
    }

    public String ping() {
        return redisConnectionComponent.ping();
    }
}
