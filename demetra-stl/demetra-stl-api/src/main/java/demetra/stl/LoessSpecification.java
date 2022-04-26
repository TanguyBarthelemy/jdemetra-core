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
import java.util.function.DoubleUnaryOperator;

/**
 * Defines a Loess filter. The specification contains 
 * - window: the length of the estimation window (should be odd)
 * - degree: the degree of the interpolating polynomial (0 for constant, 1 for linear trend)
 * - jump (optimization option): the number of jumps between two successive estimations
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder")
public class LoessSpecification {

    /**
     * The length of the full(two-sided) estimation window (should be odd)
     */
    private int window;
    /**
     * The degree of the regression polynomial (0 for constant, 1 for linear trend)
     */
    private int degree;
    /**
     * The number of jumps between two successive estimations
     */
    private int jump;
    private WeightFunction loessFunction;
    
    public static final WeightFunction WEIGHTS= WeightFunction.TRICUBE;
    
    /**
     * 
     * @param period periodicity of the series
     * @param swindow the length of the seasonal filter
     * @return 
     */
    public static LoessSpecification defaultTrend(int period, int swindow){
        int win=(int)Math.ceil((1.5 * period) / (1 - 1.5 / swindow));
        if (win%2 == 0)
            ++win;
        return of(win);
    }

    /**
     * The window is the smallest odd integer greater than or equal to period
     * @param period periodicity of the series
     * @return 
     */
    public static LoessSpecification defaultLowPass(int period){
        int win=period;
        if (win%2 == 0)
            ++win;
        return of(win);
    }

    /**
     * By default, win is the smaller odd number greater or equal to swin,
     * degree = 0 and jump is the first integer higher then .1*win
     * @param swin The seasonal window. In normal use, should be odd and at least 7.
     * @return 
     */
    public static LoessSpecification defaultSeasonal(int swin){
        if (swin%2 == 0)
            ++swin;
        return of(swin, 0);
    }

    public static LoessSpecification of(int window) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, 1, 0, null);
    }

    public static LoessSpecification of(int window, int degree) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, 0, null);
    }

    public static LoessSpecification of(int window, int degree, int jump, WeightFunction fn) {
        if (window < 2 || window % 2 != 1) {
            throw new IllegalArgumentException("STL");
        }
        if (degree < 0 || degree > 1) {
            throw new IllegalArgumentException("STL");
        }
        if (jump < 1) {
            throw new IllegalArgumentException("STL");
        }
        return new LoessSpecification(window, degree, jump, fn);
    }

    private LoessSpecification(int window, int degree, int jump, WeightFunction fn) {
        this.window = window;
        this.degree = degree;
        this.jump = jump;
        this.loessFunction=fn == null ? WEIGHTS : fn;
    }

    /**
     * @return the window
     */
    public int getWindow() {
        return window;
    }

    /**
     * @return the degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @return the jump
     */
    public int getJump() {
        return jump;
    }

    /**
     * @return the loessFunction
     */
    public DoubleUnaryOperator weights() {
        return loessFunction.asFunction();
    }

}
