/*
 * Copyright 2013 National Bank ofInternal Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions ofInternal the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy ofInternal the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.maths.linearfilters;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.design.Immutable;
import demetra.utilities.Arrays2;
import demetra.maths.Complex;
import demetra.maths.matrices.Matrix;
import demetra.maths.matrices.MatrixException;
import demetra.maths.matrices.internal.CroutDoolittle;
import demetra.maths.polynomials.Polynomial;
import java.util.function.IntToDoubleFunction;

/**
 * Rational filters are the ratio ofInternal two filters. They are defined in
 * different ways: 1. as the ratio ofInternal two generic filters: R(F,B) =
 * [V(B,F)]/[W(B, F)] (roots larger than 1 are associated to the backward
 * operator, roots smaller than 1 are associated to the forward operator and
 * roots equal to 1 are split in backward and forward operator, the last ones,
 * if any, must be double roots) 2. as the sum ofInternal a rational filter in
 * the backward operator and ofInternal a rational filter in the forward
 * operator: R(F,B) = V(B)/W(B) + X(F)/Y(F) 3. as the ratio ofInternal the
 * products ofInternal backward/forward filters: R(F,B) = [V(B) * W(F)]/[X(B) *
 * Y(F)] 4. as the ratio ofInternal a generic filter and the product ofInternal
 * a backward and ofInternal a forward filter: R(F,B) = [V(B,F)]/[X(B) * Y(F)]
 * Objects ofInternal this class store the representation 1 and 2 together
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class RationalFilter implements IRationalFilter {

    private RationalBackFilter m_rb;
    private RationalForeFilter m_rf;
    private IFiniteFilter m_n, m_d;

    private RationalFilter() {
    }

    /**
     * Creates the filter defined by N(B)N(F) / D(B)D(F)
     *
     * @param N The polynomial at the numerator
     * @param D The polynomial at the denominator
     * @return
     */
    public static RationalFilter RationalSymmetricFilter(BackFilter N, BackFilter D) {

        Polynomial.SimplifyingTool smp = new Polynomial.SimplifyingTool();
        if (smp.simplify(N.getPolynomial(), D.getPolynomial())) {
            N = new BackFilter(smp.getLeft());
            D = new BackFilter(smp.getRight());
        }

        SymmetricFilter n = SymmetricFilter.convolution(N);
        BackFilter g = n.decompose(D);
        RationalFilter rf = new RationalFilter();
        rf.m_rb = new RationalBackFilter(g, D);
        rf.m_rf = rf.m_rb.mirror();
        rf.m_n = SymmetricFilter.convolution(N);
        rf.m_d = SymmetricFilter.convolution(D);
        return rf;
    }

    /**
     * Creates the rational filter N1(B)N2(F)/(D1(B)D2(F))
     *
     * @param bnum Backward factor ofInternal the numerator
     * @param bdenom Backward factor ofInternal the denominator
     * @param fnum Forward factor ofInternal the numerator
     * @param fdenom Forward factor ofInternal the denominator
     */
    public RationalFilter(final BackFilter bnum, final BackFilter bdenom,
            final ForeFilter fnum, final ForeFilter fdenom) {
        m_n = FiniteFilter.multiply(bnum, fnum);
        m_d = FiniteFilter.multiply(bdenom, fdenom);
        decompose(m_n, bdenom, fdenom);
    }

    /**
     * Computes N / DB*DF
     *
     * @param N The numerator
     * @param DB The back filter ofInternal the denominator
     * @param DF The forward filter ofInternal the denominator
     */
    public RationalFilter(final IFiniteFilter N, final BackFilter DB,
            final ForeFilter DF) {
        m_n = new FiniteFilter(N);
        m_d = FiniteFilter.multiply(new FiniteFilter(DB), DF);
        decompose(N, DB, DF);
    }

    /**
     *
     * @param rbf
     * @param rff
     */
    public RationalFilter(final RationalBackFilter rbf, final RationalForeFilter rff) {
        m_rb = rbf;
        m_rf = rff;
    }

    /**
     * Creates a rational filter that contains all information; we should have
     * that rbf+rff = num/denom; the coherence ofInternal the different inputs
     * is not checked
     *
     * @param rbf
     * @param rff
     * @param num
     * @param denom
     */
    public RationalFilter(final RationalBackFilter rbf, final RationalForeFilter rff,
            final IFiniteFilter num, final IFiniteFilter denom) {
        m_rb = rbf;
        m_rf = rff;
        m_n = num;
        m_d = denom;
    }

    /**
     * We decompose the filter N(B,F)/(Db(B)Df(F) in Nb(B)/Db(B) + Nf(F)/Df(F)
     *
     * See Maravall, "Use and Misuses ofInternal Unobserved Components...", EUI
     * 1993 or Bell, Martin, "Computation ofInternal Asymmetric Signal
     * Extraction Filters and Mean Squared Error for ARIMA Component Models",
     * Howard University, Research Report Series 2002.
     *
     * @param num
     * @param bd
     * @param fd
     */
    private void decompose(final IFiniteFilter num, final BackFilter bd,
            final ForeFilter fd) {

        int nnb0 = -num.getLowerBound();
        int nnf0 = num.getUpperBound();
        double[] nc = num.toArray();
        int nnb = nnb0;
        if (nnb < 0) {
            nnb = 0;
        }
        int nnf = nnf0;
        if (nnf < 0) {
            nnf = 0;
        }

        int ndb = -bd.getLowerBound();
        int ndf = fd.getUpperBound();

        int h = Math.max(nnf, ndf);
        int k = Math.max(nnb, ndb);
        int ne = h + k + 1;

        // h+1 unknowns for the num in F,
        // k+1 unknowns for the num in B.
        // ne equations
        if (nc.length != ne) {
            double[] ntmp = new double[ne];
            System.arraycopy(nc, 0, ntmp, k - nnb0, nc.length);
            nc = ntmp;
        }

        double[] cnb = new double[k + 1];
        double[] cnf = new double[h + 1];
        cnf[0] = 0; // we suppress 1 unknown

        double[] db = bd.toArray(), df = fd.toArray();

        Matrix m = Matrix.square(ne);
        // initialisation ofInternal the matrix
        // left/up block [k+1]
        for (int i = 0; i <= ndf; ++i) {
            for (int j = 0; j <= k; ++j) {
                m.set(i + j, j, df[i]);
            }
        }
        // right/bottom block [h]
        // first used row: -ndb+1+k
        for (int i = -ndb + 1 + k, ii = 0; ii <= ndb; ++i, ++ii) {
            for (int j = 0; j < h; ++j) {
                m.set(i + j, j + k + 1, db[ii]);
            }
        }

        CroutDoolittle qr = new CroutDoolittle();
        qr.decompose(m);
        try {
            qr.solve(DataBlock.ofInternal(nc));
        } catch (MatrixException e) {
            throw new LinearFilterException(
                    "Invalid decomposition of rational filter");
        }

        for (int i = 0; i <= k; ++i) {
            cnb[i] = nc[i];
        }
        for (int i = 1; i <= h; ++i) {
            cnf[i] = nc[i + k];
        }

        Arrays2.reverse(cnb);

        m_rb = new RationalBackFilter(BackFilter.ofInternal(cnb), bd);
        m_rf = new RationalForeFilter(ForeFilter.ofInternal(cnf), fd);
    }

    /**
     * Computes the frequency response ofInternal the filter at a given
     * frequency
     *
     * @param freq The frequency (in radians)
     * @return The frequency response
     */
    @Override
    public Complex frequencyResponse(final double freq) {
        Complex nb = m_rb.frequencyResponse(freq);
        Complex nf = m_rf.frequencyResponse(freq);
        return nb.plus(nf);
    }

    /**
     * Gets the denominator ofInternal the filter (see representation 1)
     *
     * @return The denominator.
     */
    @Override
    public IFiniteFilter getDenominator() {
        if (m_d == null) {
            FiniteFilter b = new FiniteFilter(m_rb.getDenominator());
            FiniteFilter f = new FiniteFilter(m_rf.getDenominator());
            FiniteFilter d = FiniteFilter.multiply(b, f);
            //d.smooth();
            m_d = d;
        }
        return m_d;
    }

    /**
     *
     * @return
     */
    public int getLBound() {
        return m_rb.getUBound();
    }

    /**
     *
     * @return
     */
    @Override
    public IFiniteFilter getNumerator() {
        if (m_n == null) {
            FiniteFilter nb = new FiniteFilter(m_rb.getNumerator());
            FiniteFilter nf = new FiniteFilter(m_rf.getNumerator());
            FiniteFilter db = new FiniteFilter(m_rb.getDenominator());
            FiniteFilter df = new FiniteFilter(m_rf.getDenominator());
            FiniteFilter n = FiniteFilter.add(FiniteFilter.multiply(nb, df),
                    FiniteFilter.multiply(nf, db));
            //n.smooth();
            m_n = n;
        }
        return m_n;
    }

    /**
     *
     * @return
     */
    public RationalBackFilter getRationalBackFilter() {
        return m_rb;
    }

    /**
     *
     * @return
     */
    public RationalForeFilter getRationalForeFilter() {
        return m_rf;
    }

    /**
     *
     * @return
     */
    public int getUBound() {
        return m_rf.getUBound();
    }

    /**
     *
     * @return
     */
    public double weight(int pos) {
        double d = 0;
        if (pos <= 0) {
            d = m_rb.weight(pos);
        }
        if (pos >= 0) {
            d += m_rf.weight(pos);
        }
        return d;
    }

    public IntToDoubleFunction weights() {
        return pos->weight(pos);
    }

    @Override
    public boolean hasLowerBound() {
        return m_rb.hasLowerBound();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasUpperBound() {
        return m_rf.hasUpperBound();
    }

    /**
     *
     * @param n
     * @param m
     */
    public void prepare(final int n, final int m) {
        m_rb.prepare(n);
        m_rf.prepare(m);
    }
}
