/*
 * Copyright 2017 National Bank of Belgium
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
package jdplus.ssf;

import jdplus.data.DataBlock;
import jdplus.data.DataWindow;
import jdplus.math.matrices.MatrixWindow;
import jdplus.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
public class CompositeInitialization implements ISsfInitialization {

    private final ISsfInitialization[] initializers;
    private final int[] dim;
    private final int sdim;

    public CompositeInitialization(int[] dim, ISsfInitialization... initializers) {
        this.dim = dim;
        this.initializers = initializers;
        int tdim = 0;
        for (int i = 0; i < dim.length; ++i) {
            tdim += dim[i];
        }
        sdim = tdim;
    }

    @Override
    public int getStateDim() {
        return sdim;
    }

    @Override
    public boolean isDiffuse() {
        for (int i = 0; i < initializers.length; ++i) {
            if (initializers[i].isDiffuse()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getDiffuseDim() {
        int nd = 0;
        for (int i = 0; i < initializers.length; ++i) {
            nd += initializers[i].getDiffuseDim();
        }
        return nd;
    }

    @Override
    public void diffuseConstraints(FastMatrix b) {
        // statedim * diffusedim
        MatrixWindow cur = b.topLeft(0, 0);
        for (int i = 0; i < initializers.length; ++i) {
            int nst = initializers[i].getDiffuseDim();
            if (nst != 0) {
                initializers[i].diffuseConstraints(cur.next(dim[i], nst));
            } else {
                cur.vnext(dim[i]);
            }
        }
    }

    @Override
    public void a0(DataBlock a0) {
        DataWindow cur = a0.left();
        for (int i = 0; i < initializers.length; ++i) {
            initializers[i].a0(cur.next(dim[i]));
        }
    }

    @Override
    public void Pf0(FastMatrix p) {
        MatrixWindow cur = p.topLeft(0, 0);
        for (int i = 0; i < initializers.length; ++i) {
            initializers[i].Pf0(cur.next(dim[i], dim[i]));
        }
    }

    @Override
    public void Pi0(FastMatrix p) {
        MatrixWindow cur = p.topLeft(0, 0);
        for (int i = 0; i < initializers.length; ++i) {
            initializers[i].Pi0(cur.next(dim[i], dim[i]));
        }
    }

}
