/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tramoseats.io.protobuf;

import demetra.tramo.EstimateSpec;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class EstimateProto {
    public void fill(EstimateSpec spec, TramoSpec.EstimateSpec.Builder builder) {
        builder.setSpan(ToolkitProtosUtility.convert(spec.getSpan()))
                .setTol(spec.getTol())
                .setMl(spec.isMaximumLikelihood())
                .setUbp(spec.getUbp());
    }

    public TramoSpec.EstimateSpec convert(EstimateSpec spec) {
        TramoSpec.EstimateSpec.Builder builder = TramoSpec.EstimateSpec.newBuilder();
        fill(spec, builder);
        return builder.build();
    }

    public EstimateSpec convert(TramoSpec.EstimateSpec spec) {
        return EstimateSpec.builder()
                .span(ToolkitProtosUtility.convert(spec.getSpan()))
                .tol(spec.getTol())
                .maximumLikelihood(spec.getMl())
                .ubp(spec.getUbp())
                .build();
    }

}
