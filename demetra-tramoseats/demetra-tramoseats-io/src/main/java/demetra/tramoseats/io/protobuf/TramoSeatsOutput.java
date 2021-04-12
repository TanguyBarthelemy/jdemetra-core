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
package demetra.tramoseats.io.protobuf;

import demetra.processing.ProcessingLog;
import demetra.tramoseats.TramoSeatsSpec;
import java.util.Map;
import jdplus.tramoseats.TramoSeatsResults;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public class TramoSeatsOutput {
    TramoSeatsResults result;

    @lombok.NonNull
    TramoSeatsSpec estimationSpec;
    
    TramoSeatsSpec resultSpec;

    @lombok.Singular
    Map<String, Object> details;
    
    ProcessingLog logs;
    
    public TramoSeatsProtos.TramoSeatsOutput convert(){
        TramoSeatsProtos.TramoSeatsOutput.Builder builder = 
                TramoSeatsProtos.TramoSeatsOutput.newBuilder()
                .setEstimationSpec(TramoSeatsProto.convert(estimationSpec));
        
        if (result != null){
            builder.setResult(TramoSeatsResultsProto.convert(result))
                    .setResultSpec(TramoSeatsProto.convert(resultSpec));
        }
        // TODO detail and logs
        
        return builder.build();
    }
}
