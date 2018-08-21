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

package cn.jinyahuan.common.redis.component;

import org.springframework.data.redis.connection.RedisZSetCommands;

import java.util.Set;

/**
 * @author JinYahuan
 * @since 0.1.0
 */
public interface RedisSortedSetComponent {
    Boolean zAdd(String key, String member, double score);

    Double zIncrBy(String key, String member, double score);

    Double zScore(String key, String member);

    Long zRevrank(String key, String member);

    Set<RedisZSetCommands.Tuple> zRevRangeWithScores(String key, long start, long stop);
}
