/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import demetra.data.DoubleSeq;
import jdplus.arima.AutoCovarianceFunction;
import jdplus.maths.polynomials.Polynomial;
import jdplus.msts.ArInterpreter;
import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.ssf.SsfComponent;
import jdplus.arima.ssf.SsfAr;
import java.util.Collections;
import java.util.List;
import jdplus.msts.ParameterInterpreter;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.StateComponent;

/**
 *
 * @author palatej
 */
public class SaeItem extends StateItem {

    private final ArInterpreter ar;
    private final int lag;
    private final boolean zeroinit;

    public SaeItem(String name, double[] ar, boolean fixedar, int lag, boolean zeroinit) {
        super(name);
        this.ar = new ArInterpreter(name + ".sae", ar, fixedar);
        this.lag = lag;
        this.zeroinit = zeroinit;
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(ar);
        mapping.add((p, builder) -> {
            int nar = ar.getDomain().getDim();
            double[] par = p.extract(0, nar).toArray();
            // compute the "normalized" covariance
            double[] car = new double[par.length + 1];
            double[] lpar = new double[par.length * lag];
            car[0] = 1;
            for (int i = 0, j = lag - 1; i < par.length; ++i, j += lag) {
                lpar[j] = par[i];
                car[i + 1] = -par[i];
            }
            AutoCovarianceFunction acf = new AutoCovarianceFunction(Polynomial.ONE, Polynomial.ofInternal(car), 1);
            SsfComponent cmp = SsfAr.of(lpar, 1 / acf.get(0), lpar.length, zeroinit);
            builder.add(name, cmp);
            return nar;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Collections.singletonList(ar);
    }

    @Override
    public StateComponent build(DoubleSeq p) {
        int nar = ar.getDomain().getDim();
        double[] par = p.extract(0, nar).toArray();
        // compute the "normalized" covariance
        double[] car = new double[par.length + 1];
        double[] lpar = new double[par.length * lag];
        car[0] = 1;
        for (int i = 0, j = lag - 1; i < par.length; ++i, j += lag) {
            lpar[j] = par[i];
            car[i + 1] = -par[i];
        }
        AutoCovarianceFunction acf = new AutoCovarianceFunction(Polynomial.ONE, Polynomial.ofInternal(car), 1);
        return SsfAr.stateComponent(lpar, 1 / acf.get(0), lpar.length, zeroinit);
    }

    @Override
    public int parametersCount() {
        return ar.getDomain().getDim();
    }

    @Override
    public ISsfLoading defaultLoading(int m) {
        return m > 0 ? null : SsfAr.loading();
    }

    @Override
    public int defaultLoadingCount() {
        return 1;
    }

}
