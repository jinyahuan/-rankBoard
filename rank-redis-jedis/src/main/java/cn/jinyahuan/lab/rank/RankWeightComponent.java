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
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author JinYahuan
 * @since 1.0.0
 */
@Component
public class RankWeightComponent {
    static final String KEY_TEMPLATE_RANK_OPERATION_COUNT = RedisRankLab.KEY_RANK_PREFIX + "%s:operationCount";

    @Autowired
    private RedisComponent redisComponent;

    public long getRankOperationNumber(String rankName) {
        String key = getRankOperationNumberKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        String cacheNumber = redisComponent.get(key);
        return NumberUtils.toLong(cacheNumber, 0);
    }

    public long logRankOperationNumber(String rankName) {
        String key = getRankOperationNumberKey(rankName);
        if (Objects.isNull(key)) {
            return 0;
        }
        return redisComponent.incr(key);
    }

    static String getRankOperationNumberKey(String rankName) {
        if (StringUtils.isEmpty(rankName)) {
            return null;
        }
        return String.format(KEY_TEMPLATE_RANK_OPERATION_COUNT, rankName);
    }
}
