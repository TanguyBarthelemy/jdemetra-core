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
package jdplus.sp;

import demetra.data.DoubleSeq;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.spi.MatrixOperations;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class MatrixOperationsProcessorTest {

    public MatrixOperationsProcessorTest() {
    }

    @Test
    public void testInv() {
        int n = 50;
        Matrix M = random(n, n);
        Matrix I = MatrixOperations.inv(M);
        Matrix P = MatrixOperations.times(M, I);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                assertEquals(P.get(i, j), i == j ? 1 : 0, 1e-12);
            }
        }
    }

    static Matrix random(int nr, int nc) {
        double[] data = new double[nr * nc];
        DoubleSeq.Mutable seq = DoubleSeq.Mutable.of(data);
        Random rnd = new Random(0);
        seq.set(rnd::nextDouble);

        return Matrix.ofInternal(data, nr, nc);
    }

}