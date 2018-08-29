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

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RankWeightUtilsTest {
    @Test
    public void testComputeWeightNonExtendDecimalPlaces() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeightNonExtendDecimalPlaces(Long.MIN_VALUE));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeightNonExtendDecimalPlaces(0));

        assertEquals(new BigDecimal("0.1"), RankWeightUtils.computeWeightNonExtendDecimalPlaces(1));
        assertEquals(new BigDecimal("0.9223372036854775807"), RankWeightUtils.computeWeightNonExtendDecimalPlaces(Long.MAX_VALUE));
    }

    @Test
    public void testComputeWeightByDefaultDecimalPlaces() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeightByDefaultDecimalPlaces(Long.MIN_VALUE));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeightByDefaultDecimalPlaces(0));

        assertEquals(new BigDecimal("0.001"), RankWeightUtils.computeWeightByDefaultDecimalPlaces(1));
        assertEquals(new BigDecimal("0.009223372036854775807"), RankWeightUtils.computeWeightByDefaultDecimalPlaces(Long.MAX_VALUE));
    }

    @Test
    public void testComputeWeight() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeight(Long.MIN_VALUE, 1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeight(0, 1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeight(1, -1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeWeight(1, 11));

        assertEquals(new BigDecimal("0.01"), RankWeightUtils.computeWeight(1, 1));
        assertEquals(new BigDecimal("0.09"), RankWeightUtils.computeWeight(9, 1));
        assertEquals(new BigDecimal("0.01000000000001"), RankWeightUtils.computeWeight(1000_000_000_001L, 1));
        assertEquals(new BigDecimal("0.09999999999999"), RankWeightUtils.computeWeight(9999_999_999_999L, 1));
        assertEquals(new BigDecimal("0.09223372036854775807"), RankWeightUtils.computeWeight(Long.MAX_VALUE, 1));
//        System.out.println(new BigDecimal("9.223372036854775807E-11").stripTrailingZeros().toPlainString());
        assertEquals(new BigDecimal("0.00000000009223372036854775807"), RankWeightUtils.computeWeight(Long.MAX_VALUE, 10));
    }

    @Test
    public void testComputeReverseWeightNonExtendDecimalPlaces() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeightNonExtendDecimalPlaces(Long.MIN_VALUE));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeightNonExtendDecimalPlaces(0));

        assertEquals(new BigDecimal("0.9"), RankWeightUtils.computeReverseWeightNonExtendDecimalPlaces(1));
        assertEquals(new BigDecimal("0.0776627963145224193"), RankWeightUtils.computeReverseWeightNonExtendDecimalPlaces(Long.MAX_VALUE));
    }

    @Test
    public void testComputeReverseWeightByDefaultDecimalPlaces() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeightByDefaultDecimalPlaces(Long.MIN_VALUE));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeightByDefaultDecimalPlaces(0));

        assertEquals(new BigDecimal("0.009"), RankWeightUtils.computeReverseWeightByDefaultDecimalPlaces(1));
        assertEquals(new BigDecimal("0.000776627963145224193"), RankWeightUtils.computeReverseWeightByDefaultDecimalPlaces(Long.MAX_VALUE));
    }

    @Test
    public void testComputeReverseWeight() {
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeight(Long.MIN_VALUE, 1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeight(0, 1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeight(1, -1));
        assertEquals(BigDecimal.ZERO, RankWeightUtils.computeReverseWeight(1, 11));

        assertEquals(new BigDecimal("0.09"), RankWeightUtils.computeReverseWeight(1, 1));
        assertEquals(new BigDecimal("0.01"), RankWeightUtils.computeReverseWeight(9, 1));
        assertEquals(new BigDecimal("0.08999999999999"), RankWeightUtils.computeReverseWeight(1000_000_000_001L, 1));
        assertEquals(new BigDecimal("0.00000000000001"), RankWeightUtils.computeReverseWeight(9999_999_999_999L, 1));
        assertEquals(new BigDecimal("0.00776627963145224193"), RankWeightUtils.computeReverseWeight(Long.MAX_VALUE, 1));
//        System.out.println(new BigDecimal("7.76627963145224193E-12").stripTrailingZeros().toPlainString());
        assertEquals(new BigDecimal("0.00000000000776627963145224193"), RankWeightUtils.computeReverseWeight(Long.MAX_VALUE, 10));

        assertEquals(BigDecimal.ONE.movePointLeft(10), new BigDecimal("9.223372036854775807E-11").add(new BigDecimal("7.76627963145224193E-12")).stripTrailingZeros());
    }

    @Test
    public void testGetDefaultDecimalPlaces() {
        assertEquals(2, RankWeightUtils.getDefaultDecimalPlaces());
    }
}
