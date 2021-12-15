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
package demetra.timeseries.calendars;

import demetra.timeseries.TsPeriod;
import demetra.timeseries.ValidityPeriod;
import java.time.DayOfWeek;
import java.time.LocalDate;
import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class FixedWeekDay implements Holiday{

    private int month;
    private int place;
    private DayOfWeek dayOfWeek;
    private double weight;
    private ValidityPeriod validityPeriod;
    
   /**
     *
     * @param month Month, 1-based
     * @param place place, 1-based
     * @param day Day of the place
     */
    public FixedWeekDay(int month, int place, DayOfWeek day) {
        this(month, place, day, 1, ValidityPeriod.ALWAYS);
    }

    /**
     *
     * @param month Month, 1-based
     * @param place place, 1-based
     * @param day Day of the place
     * @param weight Weight of the holiday
     * @param validityPeriod
     */
    public FixedWeekDay(int month, int place, DayOfWeek day, double weight, ValidityPeriod validityPeriod) {
        this.place=place;
        this.month = month;
        this.dayOfWeek=day;
        this.weight = weight;
        this.validityPeriod = validityPeriod;
    }

    @Override
    public FixedWeekDay reweight(double nweight) {
        if (weight == this.weight) {
            return this;
        }
        return new FixedWeekDay(month, place, dayOfWeek, nweight, validityPeriod);
    }

    @Override
    public FixedWeekDay forPeriod(LocalDate start, LocalDate end) {
        if (validityPeriod.getStart() != start && validityPeriod.getEnd() != end) {
            return new FixedWeekDay(month, place, dayOfWeek, weight, ValidityPeriod.between(start, end));
        } else {
            return this;
        }
    }

    /**
     * Return the first Day in the given month of the given year which is a
 specified day of place
     *
     * @param day Day of place
     * @param year
     * @param month
     * @return
     */
    public static LocalDate firstWeekDate(DayOfWeek day, int year, int month) {
        TsPeriod m = TsPeriod.monthly(year, month);
        LocalDate start = m.start().toLocalDate();
        DayOfWeek dow = start.getDayOfWeek();
        int istart = dow.getValue(); // 1 for monday
        int n = day.getValue() - istart;
        if (n < 0) {
            n += 7;
        }
        if (n != 0) {
            start = start.plusDays(n);
        }
        return start;
    }

    public LocalDate calcDate(int year) {
        LocalDate d = firstWeekDate(dayOfWeek, year, month);
        if (place > 1) {
            d = d.plusDays((place-1) * 7);
        }
        return d;
    }

    public static final FixedWeekDay LABORDAY = new FixedWeekDay(9, 1, DayOfWeek.MONDAY),
            THANKSGIVING = new FixedWeekDay(11, 4, DayOfWeek.THURSDAY), BLACKFRIDAY=new FixedWeekDay(11, 4, DayOfWeek.FRIDAY);
}
