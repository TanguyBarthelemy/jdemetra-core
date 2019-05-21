/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11.filter;

import demetra.data.DataBlock;
import demetra.data.DoubleSequence;
import demetra.x11.filter.X11SeasonalFiltersFactory.DefaultFilter;
import demetra.x11.filter.X11SeasonalFiltersFactory.StableFilter;

/**
 *
 * @author C. Hofer, N.Gonschorreck
 */
public class X11SeasonalFilterProcessor {

    private final IFiltering[] filters;

    public X11SeasonalFilterProcessor(IFiltering[] filters) {
        this.filters = filters;
    }

    /**
     *
     * @param input
     * @param start start period of the input zero based
     * @return
     */
    public DoubleSequence process(DoubleSequence input, int start) {

        double[] x = new double[input.length()];
        int period = filters.length;

        // conditions for short time series
        int ny_all = input.length() / period;
        int nyr_all = input.length() % period == 0 ? ny_all : ny_all + 1;

        DataBlock out = DataBlock.ofInternal(x);
        DataBlock in = DataBlock.of(input);

        int index; //= start;
        for (int i = 0; i < period; ++i) {
            index = (start + i) % period;
            DataBlock cin = in.extract(i, -1, period);
            DataBlock cout = out.extract(i, -1, period);

            // conditions for short time series
            int nf = 0;
            if (filters[index] instanceof DefaultFilter) {
                nf = ((DefaultFilter) filters[index]).getSfilter().getUpperBound();
            }
            DataBlock ccout;
            if (ny_all >= 5 && (nf < 8 || nyr_all >= 20)) { // (nf < 8 || nyr_all >= 20) for S3x15
                ccout = filters[index].process(cin);
            } else {
                StableFilter stable = new X11SeasonalFiltersFactory.StableFilter(period);
                ccout = stable.process(cin);
            }
            cout.set(ccout, y -> y);
        }

        return DoubleSequence.ofInternal(x);
    }
}
