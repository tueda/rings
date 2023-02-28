package cc.redberry.rings.poly.univar;

import cc.redberry.rings.Ring;
import cc.redberry.rings.poly.IPolynomial;
import cc.redberry.rings.poly.multivar.AMultivariatePolynomial;
import cc.redberry.rings.poly.multivar.DegreeVector;
import cc.redberry.rings.poly.multivar.MonomialOrder;
import com.carrotsearch.hppc.IntHashSet;

import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Parent interface for univariate polynomials. Dense representation (array of coefficients) is used to hold univariate
 * polynomials. Positional operations treat index so that i-th coefficient corresponds to {@code x^i} monomial.
 *
 * @param <Poly> the type of polynomial (self type)
 * @since 1.0
 */
public interface IUnivariatePolynomial<Poly extends IUnivariatePolynomial<Poly>> extends IPolynomial<Poly> {
    /**
     * Returns the degree of this polynomial
     *
     * @return the degree of this polynomial
     */
    @Override
    default int size() {return degree() + 1;}

    /**
     * Returns the number of non zero terms in this poly
     */
    default int nNonZeroTerms() {
        int c = 0;
        for (int i = degree(); i >= 0; --i)
            if (!isZeroAt(i))
                ++c;
        return c;
    }

    /**
     * Returns whether i-th coefficient of this is zero
     *
     * @param i the position
     * @return whether i-th coefficient of this is zero
     */
    boolean isZeroAt(int i);

    @Override
    default boolean isZeroCC() {
        return isZeroAt(0);
    }

    /**
     * Fills i-th element with zero
     *
     * @param i position
     * @return self
     */
    Poly setZero(int i);

    /**
     * Sets i-th element of this by j-th element of other poly
     *
     * @param indexInThis index in self
     * @param poly        other polynomial
     * @param indexInPoly index in other polynomial
     * @return self
     */
    Poly setFrom(int indexInThis, Poly poly, int indexInPoly);

    /**
     * Returns i-th coefficient of this as a constant polynomial
     *
     * @param i index in this
     * @return i-th coefficient of this as a constant polynomial
     */
    Poly getAsPoly(int i);

    /**
     * Returns a set of exponents of non-zero terms
     *
     * @return a set of exponents of non-zero terms
     */
    default IntHashSet exponents() {
        IntHashSet degrees = new IntHashSet();
        for (int i = degree(); i >= 0; --i)
            if (!isZeroAt(i))
                degrees.add(i);
        return degrees;
    }

    /**
     * Returns position of the first non-zero coefficient, that is common monomial exponent (e.g. 2 for x^2 + x^3 +
     * ...). In the case of zero polynomial, -1 returned
     *
     * @return position of the first non-zero coefficient or -1 if this is zero
     */
    int firstNonZeroCoefficientPosition();

    /**
     * Returns the quotient {@code this / x^offset}, it is polynomial with coefficient list formed by shifting
     * coefficients of {@code this} to the left by {@code offset}.
     *
     * @param offset shift amount
     * @return the quotient {@code this / x^offset}
     */
    Poly shiftLeft(int offset);

    /**
     * Multiplies {@code this} by the {@code x^offset}.
     *
     * @param offset monomial exponent
     * @return {@code this * x^offset}
     */
    Poly shiftRight(int offset);

    /**
     * Returns the remainder {@code this rem x^(newDegree + 1)}, it is polynomial formed by coefficients of this from
     * zero to {@code newDegree} (both inclusive)
     *
     * @param newDegree new degree
     * @return remainder {@code this rem x^(newDegree + 1)}
     */
    Poly truncate(int newDegree);

    /**
     * Creates polynomial formed from the coefficients of this starting from {@code from} (inclusive) to {@code to}
     * (exclusive)
     *
     * @param from the initial index of the range to be copied, inclusive
     * @param to   the final index of the range to be copied, exclusive.
     * @return polynomial formed from the range of coefficients of this
     */
    Poly getRange(int from, int to);

    /**
     * Reverses the coefficients of this
     *
     * @return reversed polynomial
     */
    Poly reverse();

    /**
     * Creates new monomial {@code x^degree} (with the same coefficient ring)
     *
     * @param degree monomial degree
     * @return new monomial {@code coefficient * x^degree}
     */
    Poly createMonomial(int degree);

    /**
     * Returns the formal derivative of this poly (new instance, so the content of this is not changed)
     *
     * @return the formal derivative
     */
    Poly derivative();

    @Override
    Poly clone();

    /**
     * Sets the content of this with {@code oth} and destroys oth
     *
     * @param oth the polynomial (will be destroyed)
     * @return this := oth
     */
    Poly setAndDestroy(Poly oth);

    /**
     * Calculates the composition of this(oth) (new instance, so the content of this is not changed))
     *
     * @param value polynomial
     * @return composition {@code this(oth)}
     */
    Poly composition(Poly value);

    /**
     * Calculates the composition of this(oth) (new instance, so the content of this is not changed))
     *
     * @param value polynomial
     * @return composition {@code this(oth)}
     */
    default Poly composition(Ring<Poly> ring, Poly value) {
        if (value.isOne())
            return ring.valueOf(this.clone());
        if (value.isZero())
            return ccAsPoly();

        Poly result = ring.getZero();
        for (int i = degree(); i >= 0; --i)
            result = ring.add(ring.multiply(result, value), getAsPoly(i));
        return result;
    }

    /**
     * Stream polynomial coefficients as constant polynomials
     */
    Stream<Poly> streamAsPolys();

    default <E> UnivariatePolynomial<E> mapCoefficientsAsPolys(Ring<E> ring, Function<Poly, E> mapper) {
        return streamAsPolys().map(mapper).collect(new UnivariatePolynomial.PolynomialCollector<>(ring));
    }

    /**
     * Calculates the composition of this(oth)
     *
     * @param value polynomial
     * @return composition {@code this(oth)}
     */
    AMultivariatePolynomial composition(AMultivariatePolynomial value);

    /**
     * Convert to multivariate polynomial
     */
    AMultivariatePolynomial asMultivariate(Comparator<DegreeVector> ordering);

    /**
     * Convert to multivariate polynomial
     */
    default AMultivariatePolynomial asMultivariate() {
        return asMultivariate(MonomialOrder.DEFAULT);
    }

    /** ensures that internal storage has enough size to store {@code desiredCapacity} elements */
    void ensureInternalCapacity(int desiredCapacity);

    @Override
    default boolean isLinearOrConstant() {
        return degree() <= 1;
    }

    @Override
    default boolean isLinearExactly() {
        return degree() == 1;
    }
}
