/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.calendar.r;

import com.google.protobuf.InvalidProtocolBufferException;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.Calendar;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.Easter;
import demetra.timeseries.calendars.GenericTradingDays;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.regression.EasterVariable;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays;
import demetra.timeseries.regression.ModellingContext;
import demetra.toolkit.io.protobuf.CalendarProtosUtility;
import demetra.toolkit.io.protobuf.ToolkitProtos;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.modelling.regression.GenericTradingDaysFactory;
import jdplus.modelling.regression.HolidaysCorrectionFactory;
import jdplus.modelling.regression.Regression;
import jdplus.timeseries.calendars.HolidaysUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Calendars {

    public byte[] calendarToBuffer(Calendar calendar) {
        return CalendarProtosUtility.convert(calendar).toByteArray();
    }

    public Calendar calendarOf(byte[] buffer) {
        try {
            ToolkitProtos.Calendar cal = ToolkitProtos.Calendar.parseFrom(buffer);
            return CalendarProtosUtility.convert(cal);
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
    }

    public MatrixType td(TsDomain domain, int[] groups, boolean contrasts) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            GenericTradingDaysFactory.FACTORY.fill(gtd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount());
            GenericTradingDaysFactory.FACTORY.fill(gtd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public MatrixType htd(Calendar calendar, TsDomain domain, int[] groups, boolean contrasts) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, true);
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(calendar, false);
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount());
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public String[] easter(int y0, int y1, boolean julian) {
        String[] rslt = new String[y1 - y0 + 1];
        for (int y = y0, i = 0; y <= y1; ++y, ++i) {
            LocalDate e;
            if (julian) {
                e = Easter.julianEaster(y, true);
            } else {
                e = Easter.easter(y);
            }
            rslt[i] = e.format(DateTimeFormatter.ISO_DATE);
        }
        return rslt;
    }

    public double[] easter(TsDomain domain, int duration, int endpos, String corr) {
        EasterVariable.Correction correction = EasterVariable.Correction.valueOf(corr);
        EasterVariable easter = EasterVariable.builder()
                .duration(duration)
                .endPosition(endpos)
                .meanCorrection(correction)
                .build();

        DataBlock x = Regression.x(domain, easter);
        return x.toArray();
    }

    public MatrixType longTermMean(Calendar calendar, int period) {
        double[][] ltm;
        if (calendar != null) {
            ltm = HolidaysUtility.longTermMean(calendar.getHolidays(), period);
        } else {
            ltm = new double[period][];
        }
        double[] means = GenericTradingDaysFactory.meanDays(period);
        Matrix M = Matrix.make(period, 7);
        for (int i = 0; i < period; ++i) {
            DataBlock row = M.row(i);
            row.set(means[i]);
            if (ltm[i] != null) {
                DataBlock C = DataBlock.of(ltm[i]);
                row.sub(C);
                row.add(6, C.sum());
            }
        }
        return M.unmodifiable();
    }

    public MatrixType longTermMean(Calendar calendar, int period, int[] groups) {
        DayClustering dc = DayClustering.of(groups);
        Matrix M = Matrix.make(period, dc.getGroupsCount());
        MatrixType m = longTermMean(calendar, period);
        for (int i = 0; i < M.getColumnsCount(); ++i) {
            DataBlock col = M.column(i);
            for (int j = 0; j < 7; ++j) {
                if (groups[j] == i) {
                    col.add(m.column(j));
                }
            }
        }
        return M.unmodifiable();
    }

    public MatrixType htd(ModellingContext ctxt, String name, TsDomain domain, int[] groups, boolean contrasts, boolean meanCorrection) {
        DayClustering dc = DayClustering.of(groups);
        if (contrasts) {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(name, ctxt.getCalendars(), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.contrasts(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount() - 1);
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        } else {
            HolidaysCorrectedTradingDays.HolidaysCorrector corrector = HolidaysCorrectionFactory.corrector(name, ctxt.getCalendars(), meanCorrection);
            GenericTradingDays gtd = GenericTradingDays.raw(dc);
            HolidaysCorrectedTradingDays htd = new HolidaysCorrectedTradingDays(gtd, corrector);
            Matrix m = Matrix.make(domain.getLength(), dc.getGroupsCount());
            HolidaysCorrectionFactory.FACTORY.fill(htd, domain.getStartPeriod(), m);
            return m.unmodifiable();
        }
    }

    public MatrixType holidays(Calendar calendar, String date, int length, int[] nonworking, String type) {
        LocalDate start = LocalDate.parse(date);
        Holiday[] elements = calendar.getHolidays();
        Matrix m = Matrix.make(length, elements.length);
        switch (type) {
            case "Skip":
                HolidaysUtility.fillDays(elements, m, start, nonworking, true);
                break;
            case "NextWorkingDay":
                HolidaysUtility.fillNextWorkingDays(elements, m, start, nonworking);
                break;
            case "PreviousWorkingDay":
                HolidaysUtility.fillPreviousWorkingDays(elements, m, start, nonworking);
                break;
            default:
                HolidaysUtility.fillDays(elements, m, start, nonworking, false);
        }
        return m.unmodifiable();
    }

}