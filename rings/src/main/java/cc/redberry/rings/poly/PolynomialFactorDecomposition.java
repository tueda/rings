package cc.redberry.rings.poly;

import cc.redberry.rings.FactorDecomposition;
import cc.redberry.rings.util.ArraysUtil;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.carrotsearch.hppc.procedures.ObjectIntProcedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cc.redberry.rings.Rings.PolynomialRing;
import static cc.redberry.rings.poly.PolynomialMethods.polyPow;

/**
 * {@inheritDoc}
 *
 * @since 1.0
 * @since 2.2 FactorDecomposition renamed to PolynomialFactorDecomposition
 */
public final class PolynomialFactorDecomposition<Poly extends IPolynomial<Poly>>
        extends FactorDecomposition<Poly> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private PolynomialFactorDecomposition(Poly unit, List<Poly> factors, IntArrayList exponents) {
        super(PolynomialRing(unit), unit, factors, exponents);
    }

    private PolynomialFactorDecomposition(FactorDecomposition<Poly> factors) {
        super(factors.ring, factors.unit, factors.factors, factors.exponents);
    }

    @Override
    public boolean isUnit(Poly element) {
        return element.isConstant();
    }

    @Override
    public PolynomialFactorDecomposition<Poly> setUnit(Poly unit) {
        super.setUnit(unit);
        return this;
    }

    @Override
    public PolynomialFactorDecomposition<Poly> addUnit(Poly unit) {
        super.addUnit(unit);
        return this;
    }

    @Override
    public PolynomialFactorDecomposition<Poly> addFactor(Poly factor, int exponent) {
        super.addFactor(factor, exponent);
        return this;
    }

    @Override
    public PolynomialFactorDecomposition<Poly> addAll(FactorDecomposition<Poly> other) {
        super.addAll(other);
        return this;
    }

    @Override
    public PolynomialFactorDecomposition<Poly> canonical() {
        if (factors.size() == 0)
            return this;
        reduceUnitContent();
        Poly[] fTmp = factors.toArray(factors.get(0).createArray(factors.size()));
        int[] eTmp = exponents.toArray();
        for (int i = fTmp.length - 1; i >= 0; --i) {
            Poly poly = fTmp[i];
            if (poly.isMonomial() && eTmp[i] != 1) {
                poly = PolynomialMethods.polyPow(poly, eTmp[i], true);
                assert poly.isMonomial();
            }
            if (poly.signumOfLC() < 0) {
                poly.negate();
                if (eTmp[i] % 2 == 1)
                    unit.negate();
            }
        }

        ArraysUtil.quickSort(fTmp, eTmp);
        for (int i = 0; i < fTmp.length; i++) {
            factors.set(i, fTmp[i]);
            exponents.set(i, eTmp[i]);
        }
        return this;
    }

    /**
     * Makes the lead coefficient of this factorization equal to the l.c. of specified poly via multiplication of this
     * by appropriate unit
     */
    public PolynomialFactorDecomposition<Poly> setLcFrom(Poly poly) {
        Poly u = ring.getOne();
        for (int i = 0; i < size(); i++)
            u = u.multiply(PolynomialMethods.polyPow(get(i).lcAsPoly(), getExponent(i)));
        return setUnit(PolynomialMethods.divideExact(poly.lcAsPoly(), u));
    }

    /**
     * Resulting lead coefficient
     */
    public Poly lc() {
        Poly u = unit.clone();
        for (int i = 0; i < size(); i++)
            u = u.multiply(PolynomialMethods.polyPow(get(i).lcAsPoly(), getExponent(i)));
        return u;
    }

    /**
     * Calculates the signum of the polynomial constituted by this decomposition
     *
     * @return the signum of the polynomial constituted by this decomposition
     */
    public int signum() {
        int signum = unit.signumOfLC();
        for (int i = 0; i < factors.size(); i++)
            signum *= exponents.get(i) % 2 == 0 ? 1 : factors.get(i).signumOfLC();
        return signum;
    }

    /**
     * Makes each factor monic (moving leading coefficients to the {@link #unit})
     */
    public PolynomialFactorDecomposition<Poly> monic() {
        for (int i = 0; i < factors.size(); i++) {
            Poly factor = factors.get(i);
            addUnit(polyPow(factor.lcAsPoly(), exponents.get(i), false));
            factor = factor.monic();
            assert factor != null;
        }
        return this;
    }

    /**
     * Makes each factor primitive (moving contents to the {@link #unit})
     */
    public PolynomialFactorDecomposition<Poly> primitive() {
        for (int i = 0; i < factors.size(); i++) {
            Poly factor = factors.get(i);
            Poly content = factor.contentAsPoly();
            addUnit(polyPow(content, exponents.get(i), false));
            factor = factor.divideByLC(content);
            assert factor != null;
            if (factor.signumOfLC() < 0) {
                factor.negate();
                if (exponents.get(i) % 2 == 1)
                    unit.negate();
            }
        }
        return this;
    }

    public <OthPoly extends IPolynomial<OthPoly>> PolynomialFactorDecomposition<OthPoly> mapTo(Function<Poly, OthPoly> mapper) {
        return of(mapper.apply(unit), factors.stream().map(mapper).collect(Collectors.toList()), exponents);
    }

    /**
     * Calls {@link #monic()} if the coefficient ring is field and {@link #primitive()} otherwise
     */
    public PolynomialFactorDecomposition<Poly> reduceUnitContent() {
        return unit.isOverField() ? monic() : primitive();
    }

    @Override
    public PolynomialFactorDecomposition<Poly> clone() {
        return new PolynomialFactorDecomposition<>(unit.clone(), factors.stream().map(Poly::clone).collect(Collectors.toList()), new IntArrayList(exponents));
    }

    /** Unit factorization */
    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly> unit(Poly unit) {
        if (!unit.isConstant())
            throw new IllegalArgumentException();
        return empty(unit).addUnit(unit);
    }

    /** Empty factorization */
    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly> empty(Poly factory) {
        return new PolynomialFactorDecomposition<>(factory.createOne(), new ArrayList<>(), new IntArrayList());
    }

    /**
     * Factor decomposition with specified factors and exponents
     *
     * @param unit      the unit coefficient
     * @param factors   the factors
     * @param exponents the exponents
     */
    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly>
    of(Poly unit, List<Poly> factors, IntArrayList exponents) {
        if (factors.size() != exponents.size())
            throw new IllegalArgumentException();
        PolynomialFactorDecomposition<Poly> r = empty(unit).addUnit(unit);
        for (int i = 0; i < factors.size(); i++)
            r.addFactor(factors.get(i), exponents.get(i));
        return r;
    }

    /**
     * Factor decomposition with specified factors and exponents
     *
     * @param factors factors
     */
    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly>
    of(Poly... factors) {
        if (factors.length == 0)
            throw new IllegalArgumentException();
        return of(Arrays.asList(factors));
    }

    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly>
    of(Poly a) {
        Poly[] array = a.createArray(1);
        array[0] = a;
        return of(array);
    }

    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly>
    of(Poly a, Poly b) {
        return of(a.createArray(a, b));
    }

    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly>
    of(Poly a, Poly b, Poly c) {
        return of(a.createArray(a, b, c));
    }

    /**
     * Factor decomposition with specified factors and exponents
     *
     * @param factors factors
     */
    public static <Poly extends IPolynomial<Poly>> PolynomialFactorDecomposition<Poly> of(Collection<Poly> factors) {
        ObjectIntHashMap<Poly> map = new ObjectIntHashMap<>();
        for (Poly e : factors)
            map.putOrAdd(e, 1, 1);
        List<Poly> l = new ArrayList<>();
        IntArrayList e = new IntArrayList();
        map.forEach((ObjectIntProcedure<Poly>)(a, b) -> {
            l.add(a);
            e.add(b);
        });
        return of(factors.iterator().next().createOne(), l, e);
    }
}
