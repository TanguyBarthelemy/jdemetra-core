/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.linearsystem.internal;

import jdplus.data.DataBlock;
import jdplus.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.design.BuilderPattern;
import jdplus.math.matrices.MatrixException;
import demetra.design.AlgorithmImplementation;
import demetra.design.Development;
import jdplus.linearsystem.LinearSystemSolver;
import jdplus.math.matrices.decomposition.LUDecomposition;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@AlgorithmImplementation(algorithm = LinearSystemSolver.class)
@Development(status = Development.Status.Release)
public class LUSolver implements LinearSystemSolver {

    @BuilderPattern(LUSolver.class)
    public static class Builder {

        private final LUDecomposition lu;
        private boolean normalize;

        private Builder(LUDecomposition lu) {
            this.lu = lu;
        }

        public Builder normalize(boolean normalize) {
            this.normalize = normalize;
            return this;
        }

        public LUSolver build() {
            return new LUSolver(lu, normalize);
        }
    }

    public static Builder builder(LUDecomposition lu) {
        return new Builder(lu);
    }

    private final LUDecomposition lu;
    private final boolean normalize;

    private LUSolver(LUDecomposition lu, boolean normalize) {
        this.lu = lu;
        this.normalize = normalize;
    }

    @Override
    public void solve(Matrix A, DataBlock b) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != b.length()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b
        Matrix An;
        if (normalize) {

            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            DoubleSeqCursor.OnMutable cells = b.cursor();
            while (rows.hasNext()) {
                DataBlock row = rows.next();
                double norm = row.norm2()/Math.sqrt(row.length());
                row.div(norm);
                cells.applyAndNext(x -> x / norm);
            }
        } else {
            An = A;
        }
        lu.decompose(An);
        lu.solve(b);
    }

    @Override
    public void solve(Matrix A, Matrix B) {
        if (!A.isSquare()) {
            throw new MatrixException(MatrixException.SQUARE);
        }
        if (A.getRowsCount() != B.getRowsCount()) {
            throw new MatrixException(MatrixException.DIM);
        }
        // we normalize b 
        Matrix An;
        if (normalize) {
            An = A.deepClone();
            DataBlockIterator rows = An.rowsIterator();
            DataBlockIterator brows = B.rowsIterator();
            while (rows.hasNext()) {
                DataBlock row = rows.next();
                double norm = row.norm2();
                row.div(norm);
                brows.next().div(norm);
            }
        } else {
            An = A;
        }
        lu.decompose(An);
    }
}
