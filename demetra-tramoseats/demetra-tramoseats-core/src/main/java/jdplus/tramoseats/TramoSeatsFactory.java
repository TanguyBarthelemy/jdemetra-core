/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats;

import demetra.arima.SarimaSpec;
import demetra.processing.AlgorithmDescriptor;
import demetra.sa.EstimationPolicyType;
import demetra.sa.SaDiagnosticsFactory;
import demetra.sa.SaManager;
import demetra.sa.SaProcessor;
import demetra.sa.SaSpecification;
import demetra.seats.DecompositionSpec;
import demetra.tramo.TramoSpec;
import demetra.tramoseats.TramoSeatsSpec;
import nbbrd.service.ServiceProvider;
import demetra.sa.SaProcessingFactory;
import demetra.timeseries.TsDomain;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.tramoseats.TramoSeatsDictionaries;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import jdplus.modelling.GeneralLinearModel;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsConfiguration;
import jdplus.sa.diagnostics.AdvancedResidualSeasonalityDiagnosticsFactory;
import jdplus.sa.diagnostics.CoherenceDiagnostics;
import jdplus.sa.diagnostics.CoherenceDiagnosticsConfiguration;
import jdplus.sa.diagnostics.CoherenceDiagnosticsFactory;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutliersDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsConfiguration;
import jdplus.sa.diagnostics.ResidualTradingDaysDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutOfSampleDiagnosticsFactory;
import jdplus.sa.diagnostics.SaOutliersDiagnosticsFactory;
import jdplus.sa.diagnostics.SaResidualsDiagnosticsFactory;
import jdplus.seats.diagnostics.SeatsDiagnosticsConfiguration;
import jdplus.seats.diagnostics.SeatsDiagnosticsFactory;
import jdplus.tramo.TramoFactory;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(SaProcessingFactory.class)
public final class TramoSeatsFactory implements SaProcessingFactory<TramoSeatsSpec, TramoSeatsResults> {

    public static TramoSeatsFactory getInstance() {
        return (TramoSeatsFactory) SaManager.processors().stream().filter(x -> x instanceof TramoSeatsFactory).findAny().orElse(new TramoSeatsFactory());
    }

    private final List<SaDiagnosticsFactory<?, TramoSeatsResults>> diagnostics = new CopyOnWriteArrayList<>();

    public TramoSeatsFactory() {
        diagnostics.addAll(defaultDiagnostics());
    }

    public static List<SaDiagnosticsFactory<?, TramoSeatsResults>> defaultDiagnostics() {
        CoherenceDiagnosticsFactory<TramoSeatsResults> coherence
                = new CoherenceDiagnosticsFactory<>(CoherenceDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> {
                            return new CoherenceDiagnostics.Input(r.getFinals().getMode(), r);
                        }
                );
        SaOutOfSampleDiagnosticsFactory<TramoSeatsResults> outofsample
                = new SaOutOfSampleDiagnosticsFactory<>(OutOfSampleDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics().getGenericDiagnostics().forecastingTest());
        SaResidualsDiagnosticsFactory<TramoSeatsResults> residuals
                = new SaResidualsDiagnosticsFactory<>(ResidualsDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SaOutliersDiagnosticsFactory<TramoSeatsResults> outliers
                = new SaOutliersDiagnosticsFactory<>(OutliersDiagnosticsConfiguration.getDefault(),
                        r -> r.getPreprocessing());
        SeatsDiagnosticsFactory<TramoSeatsResults> seats
                = new SeatsDiagnosticsFactory<>(SeatsDiagnosticsConfiguration.getDefault(),
                        r -> r.getDiagnostics().getSpecificDiagnostics());

        AdvancedResidualSeasonalityDiagnosticsFactory<TramoSeatsResults> advancedResidualSeasonality
                = new AdvancedResidualSeasonalityDiagnosticsFactory<>(AdvancedResidualSeasonalityDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> r.getDiagnostics().getGenericDiagnostics()
                );

        ResidualTradingDaysDiagnosticsFactory<TramoSeatsResults> residualTradingDays
                = new ResidualTradingDaysDiagnosticsFactory<>(ResidualTradingDaysDiagnosticsConfiguration.getDefault(),
                        (TramoSeatsResults r) -> r.getDiagnostics().getGenericDiagnostics().residualTradingDaysTests()
                );

        List<SaDiagnosticsFactory<?, TramoSeatsResults>> all = new ArrayList<>();

        all.add(coherence);
        all.add(residuals);
        all.add(outofsample);
        all.add(outliers);
        all.add(seats);
        all.add(advancedResidualSeasonality);
        all.add(residualTradingDays);
        return all;
    }

    @Override
    public AlgorithmDescriptor descriptor() {
        return TramoSeatsSpec.DESCRIPTOR_V3;
    }

    @Override
    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, TramoSeatsResults estimation) {
        return generateSpec(spec, estimation.getPreprocessing().getDescription());
    }

    public TramoSeatsSpec generateSpec(TramoSeatsSpec spec, GeneralLinearModel.Description<SarimaSpec> desc) {

        TramoSpec ntspec = TramoFactory.getInstance().generateSpec(spec.getTramo(), desc);
        DecompositionSpec nsspec = update(spec.getSeats());

        return spec.toBuilder()
                .tramo(ntspec)
                .seats(nsspec)
                .build();
    }

    @Override
    public TramoSeatsSpec refreshSpec(TramoSeatsSpec currentSpec, TramoSeatsSpec domainSpec, EstimationPolicyType policy, TsDomain domain) {
        // NOT COMPLETE
        if (policy == EstimationPolicyType.None) {
            return currentSpec;
        }
        TramoSpec ntspec = TramoFactory.getInstance().refreshSpec(currentSpec.getTramo(), domainSpec.getTramo(), policy, domain);
        return currentSpec.toBuilder()
                .tramo(ntspec)
                .build();
    }

    private DecompositionSpec update(DecompositionSpec seats) {
        // Nothing to do (for the time being)
        return seats;
    }

    @Override
    public SaProcessor processor(TramoSeatsSpec spec) {
        return (s, cxt, log) -> TramoSeatsKernel.of(spec, cxt).process(s, log);
    }

    @Override
    public TramoSeatsSpec decode(SaSpecification spec) {
        if (spec instanceof TramoSeatsSpec) {
            return (TramoSeatsSpec) spec;
        } else {
            return null;
        }
    }

    @Override
    public boolean canHandle(SaSpecification spec) {
        return spec instanceof TramoSeatsSpec;
    }

    @Override
    public List<SaDiagnosticsFactory<?, TramoSeatsResults>> diagnosticFactories() {
        return Collections.unmodifiableList(diagnostics);
    }

    public void addDiagnostics(SaDiagnosticsFactory<?, TramoSeatsResults> diag) {
        diagnostics.add(diag);
    }

    public void replaceDiagnostics(SaDiagnosticsFactory<?, TramoSeatsResults> olddiag, SaDiagnosticsFactory<?, TramoSeatsResults> newdiag) {
        int idx = diagnostics.indexOf(olddiag);
        if (idx < 0) {
            diagnostics.add(newdiag);
        } else {
            diagnostics.set(idx, newdiag);
        }
    }

    @Override
    public void resetDiagnosticFactories(List<SaDiagnosticsFactory<?, TramoSeatsResults>> factories) {
        diagnostics.clear();
        diagnostics.addAll(factories);
    }

    @Override
    public Dictionary outputDictionary() {
        return TramoSeatsDictionaries.TRAMOSEATSDICTIONARY;
    }

}
