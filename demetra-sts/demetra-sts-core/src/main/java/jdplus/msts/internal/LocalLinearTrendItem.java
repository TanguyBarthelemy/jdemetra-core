/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.msts.internal;

import jdplus.msts.ModelItem;
import jdplus.msts.MstsMapping;
import jdplus.msts.VarianceInterpreter;
import jdplus.ssf.SsfComponent;
import java.util.Arrays;
import java.util.List;
import jdplus.msts.ParameterInterpreter;

/**
 *
 * @author palatej
 */
public class LocalLinearTrendItem extends AbstractModelItem {

    public final VarianceInterpreter lv, sv;

    public LocalLinearTrendItem(final String name, double lvar, double svar, boolean lfixed, boolean sfixed) {
        super(name);
        lv = new VarianceInterpreter(name + ".lvar", lvar, lfixed, true);
        sv = new VarianceInterpreter(name + ".svar", svar, sfixed, true);
    }

    @Override
    public void addTo(MstsMapping mapping) {
        mapping.add(lv);
        mapping.add(sv);
        mapping.add((p, builder) -> {
            double e1 = p.get(0);
            double e2 = p.get(1);
            SsfComponent cmp = jdplus.sts.LocalLinearTrend.of(e1, e2);
            builder.add(name, cmp);
            return 2;
        });
    }

    @Override
    public List<ParameterInterpreter> parameters() {
        return Arrays.asList(lv, sv);
    }

}