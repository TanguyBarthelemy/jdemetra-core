/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.timeseries.calendars;

import nbbrd.design.Development;
import java.time.LocalDate;

/**
 *
 * @author PALATEJ
 */
@Development(status = Development.Status.Release)
@lombok.Value
public class ChainedCalendar implements CalendarDefinition {

    private String first, second;
    private LocalDate breakDate;

}
