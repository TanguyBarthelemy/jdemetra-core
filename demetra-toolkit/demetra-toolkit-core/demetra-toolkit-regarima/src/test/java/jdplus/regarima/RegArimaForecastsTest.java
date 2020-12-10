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
package jdplus.regarima;

import demetra.data.Data;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.Variable;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.Regression;
import jdplus.regsarima.RegSarimaProcessor;
import jdplus.regsarima.regular.ModelDescription;
import jdplus.regsarima.regular.ModelEstimation;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author PALATEJ
 */
public class RegArimaForecastsTest {
    
    public RegArimaForecastsTest() {
    }

    @Test
    public void testSomeMethod() {
        ModelDescription model = new ModelDescription(Data.TS_PROD, null);
        model.setAirline(true);
        model.setLogTransformation(true);
        model.setPreadjustment(LengthOfPeriodType.LeapYear);
        GenericTradingDaysVariable td = new GenericTradingDaysVariable(GenericTradingDays.contrasts(DayClustering.TD3));
        model.addVariable(Variable.variable("td", td));
        EasterVariable easter = EasterVariable.builder()
                .duration(6)
                .meanCorrection(EasterVariable.Correction.Theoretical)
                .build();
        model.addVariable(Variable.variable("easter", easter));
        ModelEstimation rslt = ModelEstimation.of(model, RegSarimaProcessor.PROCESSOR);
        
        model.variables();
        TsDomain fdom=TsDomain.of(model.getEstimationDomain().getEndPeriod(), 20);
        Matrix matrix = Regression.matrix(fdom, td, easter);
        
        RegArimaForecasts.Result f = RegArimaForecasts.calcForecast(rslt.getModel(), rslt.getConcentratedLikelihood(), matrix, true, 2);
    }
    
}
