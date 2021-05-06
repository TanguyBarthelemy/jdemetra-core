/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.modelling.ComponentInformation;
import demetra.sa.ComponentType;
import demetra.sa.DefaultSaDiagnostics;
import demetra.sa.StationaryVarianceDecomposition;
import demetra.timeseries.TsData;
import jdplus.regsarima.regular.RegSarimaModel;
import jdplus.sa.StationaryVarianceComputer;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnostics;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnostics;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.seats.SeatsResults;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TramoSeatsDiagnostics {

    private DefaultSaDiagnostics saDiagnostics;

    public static TramoSeatsDiagnostics of(TramoSeatsResults rslts) {
        RegSarimaModel preprocessing = rslts.getPreprocessing();
        SeatsResults srslts = rslts.getDecomposition();
        DefaultSaDiagnostics.Builder sadiags = DefaultSaDiagnostics.builder()
                .varianceDecomposition(varDecomposition(preprocessing, srslts));
        boolean mul = preprocessing.getDescription().isLogTransformation();
        TsData sa = srslts.getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        TsData i = srslts.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
        AdvancedResidualSeasonalityDiagnostics.Input input = new AdvancedResidualSeasonalityDiagnostics.Input(mul, sa, i);
        AdvancedResidualSeasonalityDiagnostics rseas = AdvancedResidualSeasonalityDiagnostics.of(AdvancedResidualSeasonalityDiagnosticsConfiguration.DEFAULT, input);
        if (rseas != null) {
            sadiags.seasonalFTestOnI(rseas.FTestOnI())
                    .seasonalFTestOnSa(rseas.FTestOnSa())
                    .seasonalQsTestOnI(rseas.QsTestOnI())
                    .seasonalQsTestOnSa(rseas.QsTestOnSa());
        }
        ResidualTradingDaysDiagnostics.Input tdinput = new ResidualTradingDaysDiagnostics.Input(mul, sa, i);
        ResidualTradingDaysDiagnostics rtd = ResidualTradingDaysDiagnostics.of(ResidualTradingDaysDiagnosticsConfiguration.DEFAULT, tdinput);
        if (rtd != null) {
            sadiags.tdFTestOnI(rtd.FTestOnI())
                    .tdFTestOnSa(rtd.FTestOnSa());
        }
        return new TramoSeatsDiagnostics(sadiags.build());
    }

    private static StationaryVarianceDecomposition varDecomposition(RegSarimaModel preprocessing, SeatsResults srslts) {
        StationaryVarianceComputer var = new StationaryVarianceComputer();
        boolean mul = preprocessing.getDescription().isLogTransformation();
        TsData y = preprocessing.interpolatedSeries(false),
                t = srslts.getFinalComponents().getSeries(ComponentType.Trend, ComponentInformation.Value),
                seas = srslts.getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value),
                irr = srslts.getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value),
                cal = preprocessing.getCalendarEffect(y.getDomain());

        TsData others;
        if (mul) {
            TsData all = TsData.multiply(t, seas, irr, cal);
            others = TsData.divide(y, all);
        } else {
            TsData all = TsData.add(t, seas, irr, cal);
            others = TsData.subtract(y, all);
        }
        return var.build(y, t, seas, irr, cal, others, mul);
    }
}