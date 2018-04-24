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
package demetra.modelling.regression;

import demetra.data.Range;
import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.timeseries.TsException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.Value
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
@lombok.Builder
public class InterventionVariableDefinition {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder implements IBuilder<InterventionVariableDefinition> {
        
        private String name;
        private double delta, deltaSeasonal;
        private List<Range<LocalDateTime>> sequences = new ArrayList<>();
        private Double coefficient;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder coefficient(double cfixed) {
            this.coefficient = cfixed;
            return this;
        }
        
        public Builder delta(double delta) {
            this.delta = delta;
            return this;
        }
        
        public Builder deltaSeasonal(double delta) {
            this.deltaSeasonal = delta;
            return this;
        }
        
        public Builder add(LocalDateTime start, LocalDateTime end) {
            this.sequences.add( Range.of(start, end));
            return this;
        }
        
        @Override
        public InterventionVariableDefinition build() {
            if (sequences.isEmpty()) {
                throw new TsException(TsException.INVALID_DEFINITION);
            }
            return new InterventionVariableDefinition(name, delta, deltaSeasonal,
                    Collections.unmodifiableList(sequences), coefficient);
        }
    }
    
    private String name;
    private double delta, deltaSeasonal;
    private List<Range<LocalDateTime>> sequences;
    private Double coefficient;
    
    public boolean isFixed() {
        return coefficient != null;
    }
}