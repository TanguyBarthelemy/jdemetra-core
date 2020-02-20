/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.highfreq.r;

import demetra.highfreq.r.PeriodicAirline;
import demetra.data.WeeklyData;
import org.junit.Test;

/**
 *
 * @author palatej
 */
public class PeriodicAirlineTest {
    
    public PeriodicAirlineTest() {
    }

    @Test
    public void testWeekly() {
        PeriodicAirline.Results rslt = PeriodicAirline.process(WeeklyData.US_CLAIMS2, null, true, new double[]{365.25/7}, new String[]{"ao", "wo"}, 5);
        System.out.println(rslt.getStatistics());
        System.out.println();
    }
    
}