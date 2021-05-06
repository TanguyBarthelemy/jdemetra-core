/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.sts.r;

import demetra.data.Doubles;
import demetra.toolkit.extractors.LikelihoodStatisticsExtractor;
import demetra.information.InformationMapping;
import jdplus.math.functions.IFunctionDerivatives;
import jdplus.math.functions.IFunctionPoint;
import jdplus.math.functions.NumericalDerivatives;
import jdplus.likelihood.DiffuseConcentratedLikelihood;
import jdplus.ssf.dk.DkToolkit;
import jdplus.ssf.univariate.DefaultSmoothingResults;
import jdplus.ssf.univariate.SsfData;
import jdplus.sts.BasicStructuralModel;
import demetra.sts.BsmEstimationSpec;
import demetra.sts.BsmSpec;
import demetra.sts.Component;
import demetra.sts.ComponentUse;
import demetra.sts.SeasonalModel;
import jdplus.sts.SsfBsm;
import jdplus.sts.internal.BsmMonitor;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import java.util.LinkedHashMap;
import java.util.Map;
import demetra.processing.ProcResults;
import jdplus.math.matrices.Matrix;
import static jdplus.timeseries.simplets.TsDataToolkit.add;
import static jdplus.timeseries.simplets.TsDataToolkit.subtract;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class StsEstimation {

    @lombok.Value
    @lombok.Builder
    public static class Results implements ProcResults {

        TsData y, t, s, i;
        BasicStructuralModel bsm;
        DiffuseConcentratedLikelihood likelihood;
        Matrix parametersCovariance;
        double[] score;
        int nparams;

        @Override
        public boolean contains(String id) {
            return MAPPING.contains(id);
        }

        @Override
        public Map<String, Class> getDictionary() {
            Map<String, Class> dic = new LinkedHashMap<>();
            MAPPING.fillDictionary(null, dic, true);
            return dic;
        }

        @Override
        public <T> T getData(String id, Class<T> tclass) {
            return MAPPING.getData(this, id, tclass);
        }

        static final String Y = "y", T = "t", S = "s", I = "i", SA = "sa",
                UCM = "ucm", UCARIMA = "ucarima", BSM = "bsm",
                LVAR = "levelvar", SVAR = "slopevar", SEASVAR = "seasvar", CVAR = "cyclevar", NVAR = "noisevar",
                CDUMP = "cycledumpingfactor", CLENGTH = "cyclelength",
                LL = "likelihood", PCOV = "pcov", SCORE = "score";

        public static final InformationMapping<Results> getMapping() {
            return MAPPING;
        }

        private static final InformationMapping<Results> MAPPING = new InformationMapping<>(Results.class);

        static {
            MAPPING.set(LVAR, Double.class, source -> source.variance(Component.Level));
            MAPPING.set(SVAR, Double.class, source -> source.variance(Component.Slope));
            MAPPING.set(CVAR, Double.class, source -> source.variance(Component.Cycle));
            MAPPING.set(SEASVAR, Double.class, source -> source.variance(Component.Seasonal));
            MAPPING.set(NVAR, Double.class, source -> source.variance(Component.Noise));
            MAPPING.set(CDUMP, Double.class, source -> source.getBsm().getCyclicalDumpingFactor());
            MAPPING.set(CLENGTH, Double.class, source -> source.getBsm().getCyclicalPeriod() / (6 * source.getBsm().getPeriod()));
            MAPPING.set(Y, TsData.class, source -> source.getY());
            MAPPING.set(T, TsData.class, source -> source.getT());
            MAPPING.set(S, TsData.class, source -> source.getS());
            MAPPING.set(I, TsData.class, source -> source.getI());
            MAPPING.set(SA, TsData.class, source -> subtract(source.getY(), source.getS()));
            MAPPING.delegate(LL, LikelihoodStatisticsExtractor.getMapping(), r -> r.getLikelihood().stats(0, r.getNparams()));
            MAPPING.set(PCOV, Matrix.class, source -> source.getParametersCovariance());
            MAPPING.set(SCORE, double[].class, source -> source.getScore());
        }

        private double variance(Component cmp) {
            double v = bsm.getVariance(cmp);
            if (v > 0) {
                v *= likelihood.sigma();
            }
            return v;
        }
    }

    public Results process(TsData y, int level, int slope, int cycle, int noise, String seasmodel) {
        SeasonalModel sm = SeasonalModel.valueOf(seasmodel);
        BsmSpec mspec = new BsmSpec();
        mspec.setLevelUse(of(level));
        mspec.setSlopeUse(of(slope));
        mspec.setCycleUse(of(cycle));
        mspec.setNoiseUse(of(noise));
        mspec.setSeasonalModel(sm);

        BsmMonitor monitor = new BsmMonitor();
        monitor.setSpecification(mspec);
        BsmEstimationSpec espec = new BsmEstimationSpec();
        if (!monitor.process(y.getValues(), y.getTsUnit().ratioOf(TsUnit.YEAR))) {
            return null;
        }

        BasicStructuralModel bsm = monitor.getResult();
        SsfBsm ssf = SsfBsm.of(bsm);
        DefaultSmoothingResults sr = DkToolkit.sqrtSmooth(ssf, new SsfData(y.getValues()), true, true);

        TsData t = null, c = null, s = null, seas = null, n = null;
        TsPeriod start = y.getStart();
        mspec=bsm.specification();
        if (mspec.hasLevel()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Level);
            t = TsData.of(start, Doubles.of(sr.getComponent(pos)));
        }
        if (mspec.hasSlope()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Slope);
            s = TsData.of(start, Doubles.of(sr.getComponent(pos)));
        }
        if (mspec.hasCycle()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Cycle);
            c = TsData.of(start, Doubles.of(sr.getComponent(pos)));
        }
        if (mspec.hasSeasonal()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Seasonal);
            seas = TsData.of(start, Doubles.of(sr.getComponent(pos)));
        }
        if (mspec.hasNoise()) {
            int pos = SsfBsm.searchPosition(bsm, Component.Noise);
            n = TsData.of(start, Doubles.of(sr.getComponent(pos)));
        }

        IFunctionPoint ml = monitor.maxLikelihoodFunction();
        IFunctionDerivatives derivatives = new NumericalDerivatives(ml, false);
        int ndf = y.length();
        double objective = ml.getValue();
        Matrix hessian = derivatives.hessian();
        double[] score = derivatives.gradient().toArray();
        hessian.mul((.5 * ndf) / objective);
        for (int i = 0; i < score.length; ++i) {
            score[i] *= (-.5 * ndf) / objective;
        }

        return Results.builder()
                .likelihood(monitor.getLikelihood())
                .bsm(bsm)
                .y(y)
                .t(t)
                .s(seas)
                .i(add(n, c))
                .parametersCovariance(hessian)
                .score(score)
                .nparams(score.length)
                .build();
    }

    private ComponentUse of(int p) {
        if (p == 0) {
            return ComponentUse.Fixed;
        } else if (p > 0) {
            return ComponentUse.Free;
        } else {
            return ComponentUse.Unused;
        }
    }
}