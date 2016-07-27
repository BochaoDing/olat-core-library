/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.ims.qti;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Initial Date: 15.01.2015 <br>
 *
 * Tests for OLAT-7129: error at calculating test score due to using float instead of double for the sum/total.
 *
 * @author lavinia
 */

public class ScoreCalculatorTest {

    /**
     * 4.0f + (2.0f / 3) + (2.0f / 3) + (2.0f / 3) == 5.9999995f
     */
    @Test
    public void floatSum_threeTimes() {
        float sum = 4.0f + (2.0f / 3) + (2.0f / 3) + (2.0f / 3);
        System.out.println("floatSum: " + sum);

        assertTrue(sum == 5.9999995f);
        assertFalse(sum == 4.0f + 3 * (2.0f / 3));
    }

    /**
     * 4.0 + (2.0f / 3) + (2.0f / 3) + (2.0f / 3) = 6.00000005960464
     */
    @Test
    public void doubleSum_threeTimes() {
        double sum = 4.0 + (2.0f / 3) + (2.0f / 3) + (2.0f / 3);
        System.out.println("doubleSum: " + sum);

        assertTrue(sum == 6.000000059604645);

        double total = 4.0 + 3 * (2.0f / 3);
        System.out.println("total: " + total);

        assertTrue(total == 6.0);

        assertFalse(sum == total);
    }

    /**
     * If we use floats as vars: (4.0f + 0.6666667f) = 4.6666665f == 4.6666667f
     */
    @Test
    public void floatSum_once() {
        float total = 4.0f;
        float itemScore = 0.6666667f;
        total += itemScore;
        System.out.println("total: " + total);

        assertTrue(total == 4.6666665f);
        assertTrue(4.6666667f == 4.6666665f); // comparing floats
        assertTrue(total == 4.6666667f);
        assertFalse(4.6666667 == 4.6666665); // comparing doubles
    }

    /**
     * If we use double for total
     */
    @Test
    public void doubleSum_once() {
        double total = 4.0;
        float itemScore = 0.6666667f;
        total += itemScore;
        System.out.println("total: " + total);
        assertTrue(total == 4.666666686534882);
    }

    /**
     * This is how the score was calculated before the fix for OLAT-7129. <br>
     *
     */
    @Test
    public void calcScore_withFloatTotal_before_OLAT_7129_fix() {
        float total = 14 * 2.0f; // previous computed partial score
        float itemScore = 2.0f / 3; // question score: 0.6666667f;

        int iterations = 3;
        for (int i = 0; i < iterations; i++) {
            total += itemScore;
            System.out.println("total: " + total);
        }

        assertTrue(total < 30);
    }

    @Test
    public void calcScore_withDoubleTotal_OLAT_7129() {
        double total = 14 * 2.0f; // previous computed partial score
        float itemScore = 2.0f / 3; // question score: 0.6666667f;

        int iterations = 3;
        for (int i = 0; i < iterations; i++) {
            total += itemScore;
            System.out.println("total: " + total);
        }

        assertTrue(total >= 30);
    }

    /**
     * Iterate 120 additions and subtractions.
     */
    @Test
    public void calcScore_more_doubles() {
        double total = 4.0;
        float itemScore = 0.6666667f; // this is float in org.olat.lms.ims.qti.container.Variable
        int iterations = 120;
        for (int i = 0; i < iterations; i++) {
            total += itemScore;
        }
        System.out.println("calcScore_more_doubles - additions total: " + total);
        assertTrue(total > 84.0);

        for (int i = 0; i < iterations; i++) {
            total -= itemScore;
        }
        System.out.println("calcScore_more_doubles - subtractions total: " + total);
        assertTrue(total == 4.0);
    }

}
