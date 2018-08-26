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

import java.math.BigDecimal;

/**
 * 排行榜权重工具类。
 *
 * @author JinYahuan
 * @since 1.0.0
 */
public final class RankWeightUtils {
    private RankWeightUtils() {
    }

    /**
     * 默认精度。
     */
    public static final int DEFAULT_DECIMAL_PLACES = 2;

    /**
     * 计算权重（无需额外的主体精度，即权重值即小数位）。
     *
     * @param val
     * @return
     */
    public static BigDecimal computeWeightNonExtendDecimalPlaces(long val) {
        return computeWeight(val, 0);
    }

    /**
     * 计算权重（精度为2位小数，即权重值在2位小数之后）。
     *
     * @param val
     * @return
     */
    public static BigDecimal computeWeightByDefaultDecimalPlaces(long val) {
        return computeWeight(val, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 计算权重。
     * <p>
     * 说明：如果主体值有精度时，将精度值设置为主体的精度值。
     * </p>
     *
     * @param weightValue   权重值。weightValue <= 0 时返回 0
     * @param decimalPlaces 有效值的小数位（精度）。取值范围为：0 < decimalPlaces <= 10，非此区间返回 0
     * @return not null, 一定大于 0
     */
    public static BigDecimal computeWeight(long weightValue, int decimalPlaces) {
        if (weightValue <= 0
                || (decimalPlaces < 0 || decimalPlaces > 10)) {
            return BigDecimal.ZERO;
        }

        Double dContraryDecimalPlaces = Math.pow(10, decimalPlaces + 1);
        BigDecimal bContraryDecimalPlaces = new BigDecimal(dContraryDecimalPlaces.toString());

        String sDecimalPlaces = BigDecimal.ONE.divide(bContraryDecimalPlaces).stripTrailingZeros().toPlainString();
        String decimalPlacesPrefix = sDecimalPlaces.substring(0, sDecimalPlaces.length() - 1);

        return new BigDecimal(decimalPlacesPrefix + weightValue);
    }
}
