/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa.diagnostics;

import demetra.stats.OneWayAnova;
import demetra.stats.StatisticalTest;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
public class CombinedSeasonalityTest {
     /**
     * 
     */
    public static enum IdentifiableSeasonality
    {
        /**
         * No identifiable seasonality
         */
        None,
        /**
         * Probably no identifiable seasonality
         */
        ProbablyNone,
        /**
         * Identifiable seasonality present
         */
        Present
    }
   
	IdentifiableSeasonality seasonality;
        
	OneWayAnova stableSeasonality, evolutiveSeasonality;
	StatisticalTest kruskalWallis;
}