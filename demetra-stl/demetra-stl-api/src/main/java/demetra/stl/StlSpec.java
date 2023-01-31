/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.stl;

import demetra.data.WeightFunction;
import demetra.processing.AlgorithmDescriptor;
import demetra.processing.ProcSpecification;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class StlSpec implements ProcSpecification {
    
    public static final int DEF_SWINDOW=11;

    private boolean multiplicative;
    private LoessSpec trendSpec;
    private SeasonalSpec seasonalSpec;
    private int innerLoopsCount, outerLoopsCount;
    private double robustWeightThreshold;

    private WeightFunction robustWeightFunction;

    public static final double RWTHRESHOLD = 0.001;
    public static final WeightFunction RWFUNCTION = WeightFunction.BIWEIGHT;

    public static Builder robustBuilder() {
        return new Builder()
                .innerLoopsCount(1)
                .outerLoopsCount(15)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    public static Builder builder() {
        return new Builder()
                .innerLoopsCount(2)
                .outerLoopsCount(0)
                .robustWeightFunction(RWFUNCTION)
                .robustWeightThreshold(RWTHRESHOLD);
    }

    /**
     * Creates a default specification for a series that has a given periodicity
     *
     * @param period The periodicity of the series
     * @param mul
     * @param robust True for robust filtering, false otherwise.
     * @return
     */
    public static StlSpec createDefault(int period, boolean mul, boolean robust) {
        return createDefault(period, DEF_SWINDOW, mul, robust);
    }

    /**
     * Given the length of the seasonal window, creates a default specification
     * for a series that has a given periodicity
     *
     * @param period
     * @param swindow
     * @param mul
     * @param robust
     * @return
     */
    public static StlSpec createDefault(int period, int swindow, boolean mul, boolean robust) {

        if (robust) {
            return robustBuilder()
                    .trendSpec(LoessSpec.defaultTrend(period, swindow, true))
                    .seasonalSpec(new SeasonalSpec(period, swindow, true))
                    .multiplicative(mul)
                    .build();
        } else {
            return builder()
                    .trendSpec(LoessSpec.defaultTrend(period, swindow, true))
                    .seasonalSpec(new SeasonalSpec(period, swindow, true))
                    .multiplicative(mul)
                    .build();
        }
    }

    public static final String METHOD = "stlplus";
    public static final String FAMILY = "Seasonal adjustment";
    public static final String VERSION = "0.1.0.0";

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return new AlgorithmDescriptor(FAMILY, METHOD, VERSION);
    }

    @Override
    public String display() {
        return "Extended airline";
    }
}
