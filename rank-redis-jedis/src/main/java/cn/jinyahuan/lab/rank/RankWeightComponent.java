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
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RankWeightComponent {
    static final String KEY_TEMPLATE_WEIGHT = RedisRankLab.KEY_RANK_PREFIX + "%s:weight";

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 检视当前的权重值。
     *
     * @param rankName
     * @return
     */
    public long peek(String rankName) {
        Objects.requireNonNull(rankName, "rankName must not be null");
        String cachedValue = redisComponent.get(getKey(rankName));
        return safeToLong(cachedValue, 0);
    }

    /**
     * 重新初始化权重值。
     *
     * @param rankName
     * @param initValue
     * @return
     */
    public void init(String rankName, long initValue) {
        redisComponent.set(getKey(rankName), String.valueOf(initValue));
    }

    /**
     * 提供一个权重值。
     *
     * @param rankName
     * @return
     */
    public long offer(String rankName) {
        return redisComponent.incr(getKey(rankName));
    }

    static String getKey(String rankName) {
        return String.format(KEY_TEMPLATE_WEIGHT, rankName);
    }

    static long safeToLong(String str, long defaultValue) {
        if (Objects.isNull(str)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (Throwable ex) {
            return defaultValue;
        }
    }
}
