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

import cn.jinyahuan.common.redis.component.RedisStringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RedisStringComponentImpl implements RedisStringComponent {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String get(String key) {
        if (Objects.isNull(key)) {
            return null;
        }
        return (String) stringRedisTemplate.execute((RedisCallback) connection -> {
            byte[] temp = connection.get(key.getBytes());
            return Objects.isNull(temp) ? null : new String(temp);
        });
    }

    @Override
    public void set(String key, String value) {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            return;
        }
        stringRedisTemplate.execute((RedisCallback) connection -> {
            connection.set(key.getBytes(), value.getBytes());
            return null;
        });
    }

    @Override
    public Long incr(String key) {
        if (Objects.isNull(key)) {
            return null;
        }
        return (Long) stringRedisTemplate.execute((RedisCallback) connection -> connection.incr(key.getBytes()));
    }

    @Override
    public Long incrBy(String key, long increment) {
        if (Objects.isNull(key)) {
            return null;
        }
        return (Long) stringRedisTemplate.execute((RedisCallback) connection -> connection.incrBy(key.getBytes(), increment));
    }
}
