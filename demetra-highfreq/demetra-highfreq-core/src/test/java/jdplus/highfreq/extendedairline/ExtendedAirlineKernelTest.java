/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.highfreq.extendedairline;

import jdplus.highfreq.regarima.HighFreqRegArimaModel;
import demetra.data.Data;
import demetra.data.MatrixSerializer;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import demetra.highfreq.ExtendedAirlineModellingSpec;
import demetra.highfreq.ExtendedAirlineSpec;
import demetra.modelling.highfreq.HolidaysSpec;
import demetra.modelling.highfreq.OutlierSpec;
import demetra.modelling.highfreq.RegressionSpec;
import demetra.modelling.highfreq.TransformSpec;
import java.io.InputStream;
import demetra.math.matrices.Matrix;
import demetra.modelling.TransformationType;
import demetra.processing.ProcessingLog;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.HolidaysOption;
import demetra.timeseries.regression.ModellingContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class ExtendedAirlineKernelTest {

    public ExtendedAirlineKernelTest() {
    }

    @Test
    public void testDaily() throws IOException {
        InputStream stream = Data.class.getResourceAsStream("/edf.txt");
        Matrix edf = MatrixSerializer.read(stream);
        Holiday[] france = france();
        ModellingContext context=new ModellingContext();
        context.getCalendars().set("FR", new Calendar(france));
        // daily time series
        TsData EDF=TsData.of(TsPeriod.daily(1996, 1, 1) , edf.column(0));
        
        // build the psec
        ExtendedAirlineModellingSpec spec=ExtendedAirlineModellingSpec.builder()
                .transform(TransformSpec.builder()
                        .function(TransformationType.Log)
                        .build())
                .stochastic(ExtendedAirlineSpec.DEFAULT_WD)
                .outlier(OutlierSpec.builder()
                        .criticalValue(8)
                        .ao(true)
                        .build())
                .regression(RegressionSpec.builder()
                        .holidays(HolidaysSpec.builder()
                                        .holidays("FR")
                                        .holidaysOption(HolidaysOption.Skip)
                                        .single(false)
                                        .build())
                        .build())
                .build();
        ExtendedAirlineKernel kernel=ExtendedAirlineKernel.of(spec, context);
        HighFreqRegArimaModel rslt = kernel.process(EDF, ProcessingLog.dummy());
        assertTrue(rslt != null);
        Map<String, Class> dictionary = rslt.getDictionary();
//        dictionary.keySet().forEach(v->System.out.println(v));
    }

    private static void addDefault(List<Holiday> holidays) {
        holidays.add(FixedDay.NEWYEAR);
        holidays.add(FixedDay.MAYDAY);
        holidays.add(FixedDay.ASSUMPTION);
        holidays.add(FixedDay.ALLSAINTSDAY);
        holidays.add(FixedDay.CHRISTMAS);
        holidays.add(EasterRelatedDay.EASTERMONDAY);
        holidays.add(EasterRelatedDay.ASCENSION);
        holidays.add(EasterRelatedDay.WHITMONDAY);
    }

    public static Holiday[] france() {
        List<Holiday> holidays = new ArrayList<>();
        addDefault(holidays);
        holidays.add(new FixedDay(5, 8));
        holidays.add(new FixedDay(7, 14));
        holidays.add(FixedDay.ARMISTICE);
        return holidays.stream().toArray(i -> new Holiday[i]);
    }
}
