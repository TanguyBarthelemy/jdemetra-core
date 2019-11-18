/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.math.matrices.lapack;

/**
 *
 * Computes y = a * x + y
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SWAP {

    public void apply(int n, DataPointer x, DataPointer y) {
        if (n == 0) {
            return;
        }
        int xinc=x.inc(), yinc=y.inc();
        if (xinc == 1 && yinc == 1) {
            int yend = y.pos + n;
            for (int i = y.pos, j = x.pos; i < yend; ++i, ++j) {
                double tmp = x.p[j];
                x.p[j] = y.p[i];
                y.p[i] = tmp;
            }
        } else {
            int yend = y.pos + n * yinc;
            for (int i = y.pos, j = x.pos; i != yend; i += yinc, j += xinc) {
                double tmp = x.p[j];
                x.p[j] = y.p[i];
                y.p[i] = tmp;
            }
        }

    }
}
