/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.x13.regarima;

import internal.jdplus.arima.ModifiedLjungBoxFilter;
import nbbrd.design.Development;
import jdplus.arima.estimation.ResidualsComputer;
import jdplus.data.DataBlock;
import jdplus.regarima.outlier.CriticalValueComputer;
import jdplus.sarima.SarimaModel;
import jdplus.regsarima.RegSarimaComputer;
import jdplus.regarima.IRegArimaComputer;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
@lombok.experimental.UtilityClass
public class X13Utility {

    public static final double MINCV = 2.8;

    public double calcCv(int nobs) {
        return Math.max(CriticalValueComputer.advancedComputer().applyAsDouble(nobs), MINCV);
    }

    public IRegArimaComputer<SarimaModel> processor(boolean ml, double precision) {
        return RegSarimaComputer.builder()
                .useMaximumLikelihood(ml)
                .precision(precision)
                .startingPoint(RegSarimaComputer.StartingPoint.Multiple)
                .build();
    }

    public static ResidualsComputer mlComputer() {
        return (arma, y) -> {
            ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
            int n = y.length();
            int nf = f.prepare(arma, n);
            DataBlock fres = DataBlock.make(nf);
            f.apply(y, fres);
            return nf == n ? fres : fres.drop(nf - n, 0);
        };
    }

}
