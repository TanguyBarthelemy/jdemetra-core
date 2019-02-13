/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.descriptors.stats;

import demetra.information.InformationMapping;
import demetra.likelihood.Likelihood;
import demetra.maths.MatrixType;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class LikelihoodDescriptor {

    private final String LL = "ll", LDET="ldet", SSQ = "ssqerr", SER = "ser", SIGMA = "sigma", RES="residuals";

    private final InformationMapping<Likelihood> MAPPING = new InformationMapping<>(Likelihood.class);

    static {
        MAPPING.set(SER, Double.class, source -> source.ser());
        MAPPING.set(SIGMA, Double.class, source -> source.sigma());
        MAPPING.set(LL, Double.class, source -> source.logLikelihood());
        MAPPING.set(LDET, Double.class, source -> source.logDeterminant());
        MAPPING.set(SSQ, Double.class, source -> source.ssq());
        MAPPING.set(RES, double[].class, source -> source.e().toArray());
    }

    public InformationMapping<Likelihood> getMapping() {
        return MAPPING;
    }
}
