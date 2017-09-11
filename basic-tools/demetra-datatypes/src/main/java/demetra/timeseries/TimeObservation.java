/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.timeseries;

import demetra.data.Range;
import java.time.LocalDateTime;

/**
 *
 * @author Philippe Charles
 * @param <P> period type
 * @param <V> value type
 */
public interface TimeObservation<P extends Range<LocalDateTime>, V> {

    P getPeriod();

    interface OfDouble<P extends Range<LocalDateTime>> extends TimeObservation<P, Double> {

        double getValue();
    }
}