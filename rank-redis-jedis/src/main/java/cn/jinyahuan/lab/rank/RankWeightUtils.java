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
    private static final int DEFAULT_DECIMAL_PLACES = 2;

    /**
     * 计算权重（无需额外保留小数位，即所有小数位都可以为权重）。
     *
     * @param weightValue 权重值，该值越大最终的权重越大。weightValue <= 0 时返回 0
     * @return not null
     * @see #computeWeightByDefaultDecimalPlaces(long)
     * @see #computeWeight(long, int)
     * @see #computeReverseWeightNonExtendDecimalPlaces(long)
     */
    public static BigDecimal computeWeightNonExtendDecimalPlaces(long weightValue) {
        return computeWeight(weightValue, 0);
    }

    /**
     * 计算权重（保留的小数位位数为{@link #getDefaultDecimalPlaces() 默认值}）。
     *
     * @param weightValue 权重值，该值越大最终的权重越大。weightValue <= 0 时返回 0
     * @return not null
     * @see #computeWeightNonExtendDecimalPlaces(long)
     * @see #computeWeight(long, int)
     * @see #computeReverseWeightByDefaultDecimalPlaces(long)
     */
    public static BigDecimal computeWeightByDefaultDecimalPlaces(long weightValue) {
        return computeWeight(weightValue, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 计算权重。
     *
     * @param weightValue   权重值，该值越大最终的权重越大。weightValue <= 0 时返回 0
     * @param decimalPlaces 需要保留的小数位，即该小数位数不能用于计算权重。取值范围为：0 < decimalPlaces <= 10，非此区间返回 0
     * @return not null
     * @see #computeWeightByDefaultDecimalPlaces(long)
     * @see #computeWeightNonExtendDecimalPlaces(long)
     * @see #computeReverseWeight(long, int)
     */
    public static BigDecimal computeWeight(long weightValue, int decimalPlaces) {
        if (weightValue <= 0
                || (decimalPlaces < 0 || decimalPlaces > 10)) {
            return BigDecimal.ZERO;
        }

        BigDecimal originWeight = new BigDecimal(weightValue);
        return originWeight.movePointLeft(originWeight.toPlainString().length() + decimalPlaces);
    }

    /**
     * 计算与{@link #computeWeight(long, int)}互补十进制进一位的权重（无需额外保留小数位，即所有小数位都可以为权重）。
     *
     * @param weightValue 权重值，该值越大最终的权重越小。weightValue <= 0 时返回 0
     * @return not null
     * @see #computeReverseWeightByDefaultDecimalPlaces(long)
     * @see #computeReverseWeight(long, int)
     * @see #computeWeightNonExtendDecimalPlaces(long)
     */
    public static BigDecimal computeReverseWeightNonExtendDecimalPlaces(long weightValue) {
        return computeReverseWeight(weightValue, 0);
    }

    /**
     * 计算与{@link #computeWeight(long, int)}互补十进制进一位的权重（保留的小数位位数为{@link #getDefaultDecimalPlaces() 默认值}）。
     *
     * @param weightValue 权重值，该值越大最终的权重越小。weightValue <= 0 时返回 0
     * @return not null
     * @see #computeReverseWeightNonExtendDecimalPlaces(long)
     * @see #computeReverseWeight(long, int)
     * @see #computeWeightByDefaultDecimalPlaces(long)
     */
    public static BigDecimal computeReverseWeightByDefaultDecimalPlaces(long weightValue) {
        return computeReverseWeight(weightValue, DEFAULT_DECIMAL_PLACES);
    }

    /**
     * 计算与{@link #computeWeight(long, int)}互补十进制进一位的权重。
     *
     * @param weightValue   权重值，该值越大最终的权重越小。weightValue <= 0 时返回 0
     * @param decimalPlaces 需要保留的小数位，即该小数位数不能用于计算权重。取值范围为：0 < decimalPlaces <= 10，非此区间返回 0
     * @return not null
     * @see #computeReverseWeightByDefaultDecimalPlaces(long)
     * @see #computeReverseWeightNonExtendDecimalPlaces(long)
     * @see #computeWeight(long, int)
     */
    public static BigDecimal computeReverseWeight(long weightValue, int decimalPlaces) {
        BigDecimal weight = computeWeight(weightValue, decimalPlaces);
        if (!BigDecimal.ZERO.equals(weight)) {
            return BigDecimal.ONE.movePointLeft(decimalPlaces).subtract(weight);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取默认的小数位（精度）值。
     *
     * @return
     */
    public static int getDefaultDecimalPlaces() {
        return DEFAULT_DECIMAL_PLACES;
    }
}
