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
package demetra.x13;

import demetra.design.Algorithm;
import nbbrd.design.Development;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class X13 {

    private final X13Loader.Processor ENGINE = new X13Loader.Processor();
    private final AtomicReference<Processor> LEGACYENGINE=new AtomicReference<Processor>();

    public void setEngine(Processor algorithm) {
        ENGINE.set(algorithm);
    }

    public Processor getEngine() {
        return ENGINE.get();
    }

    public X13Results process(TsData series, X13Spec spec, ModellingContext context, List<String> addtionalItems) {
        return ENGINE.get().process(series, spec, context, addtionalItems);
    }

    public void setLegacyEngine(Processor algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Processor getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public X13Results processLegacy(TsData series, X13Spec spec, ModellingContext context, List<String> addtionalItems) {
        Processor cp = LEGACYENGINE.get();
        if (cp == null)
            throw new X13Exception("No legacy engine");
        return cp.process(series, spec, context, addtionalItems);
    }

    @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT)
    @FunctionalInterface
    public static interface Processor {

        public X13Results process(TsData series, X13Spec spec, ModellingContext context, List<String> addtionalItems);

    }
}