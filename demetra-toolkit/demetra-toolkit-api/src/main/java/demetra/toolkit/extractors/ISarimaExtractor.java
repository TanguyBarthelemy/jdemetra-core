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
package demetra.toolkit.extractors;

import demetra.arima.ISarimaModel;
import demetra.data.DoubleSeq;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class ISarimaExtractor extends InformationMapping<ISarimaModel> {

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
            PERIOD = "period", NAME = "name";

    public ISarimaExtractor() {
        set(NAME, String.class, source -> source.getName());
        set(P, Integer.class, source -> source.getP());
        set(D, Integer.class, source -> source.getD());
        set(Q, Integer.class, source -> source.getQ());
        set(PERIOD, Integer.class, source -> source.getPeriod());
        set(BP, Integer.class, source -> source.getBp());
        set(BD, Integer.class, source -> source.getBd());
        set(BQ, Integer.class, source -> source.getBq());
        set(PARAMETERS, double[].class, source -> source.parameters().toArray());
        set(PARAMETERS2, double[].class, source -> {
            double[] z = source.parameters().toArray();
            int p = source.getP() + source.getBp();
            for (int i = 0; i < p; ++i) {
                z[i] -= z[i];
            }
            return z;
        }
        );
        set(PHI, double[].class, source -> source.getPhi().toArray());
        set(BPHI, double[].class, source -> source.getBphi().toArray());
        set(THETA, double[].class, source -> source.getTheta().toArray());
        set(BTHETA, double[].class, source -> source.getBtheta().toArray());
        setArray(PHI, 1, 4, Double.class, (source, i) -> {
            DoubleSeq p = source.getPhi();
            if (i > p.length()) {
                return null;
            } else {
                return p.get(i - 1);
            }
        });
        setArray(BPHI, 1, 2, Double.class, (source, i) -> {
            DoubleSeq p = source.getBphi();
            if (i > p.length()) {
                return null;
            } else {
                return p.get(i - 1);
            }
        });
        setArray(THETA, 1, 4, Double.class, (source, i) -> {
            DoubleSeq p = source.getTheta();
            if (i > p.length()) {
                return null;
            } else {
                return p.get(i - 1);
            }
        });
        setArray(BTHETA, 1, 2, Double.class, (source, i) -> {
            DoubleSeq p = source.getBtheta();
            if (i > p.length()) {
                return null;
            } else {
                return p.get(i - 1);
            }
        });
    }

    @Override
    public Class getSourceClass() {
        return ISarimaModel.class;
    }

}