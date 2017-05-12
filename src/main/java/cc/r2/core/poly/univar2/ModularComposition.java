package cc.r2.core.poly.univar2;


import java.util.ArrayList;

import static cc.r2.core.poly.univar2.PolynomialArithmetics.polyMod;

/**
 * Polynomial composition.
 *
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ModularComposition {
    private ModularComposition() {}

    /**
     * Returns {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree.
     *
     * @param polyModulus the monic modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @return {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree]}, where {@code degree} is {@code polyModulus} degree
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static <T extends IUnivariatePolynomial<T>> ArrayList<T> xPowers(T polyModulus, DivisionWithRemainder.InverseModMonomial<T> invMod) {
        return polyPowers(PolynomialArithmetics.createMonomialMod(polyModulus.coefficientDomainCardinality(), polyModulus, invMod), polyModulus, invMod, polyModulus.degree());
    }

    /**
     * Returns {@code poly^{i} mod polyModulus} for i in {@code [0...nIterations]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @return {@code poly^{i} mod polyModulus} for i in {@code [0...nIterations]}
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static <T extends IUnivariatePolynomial<T>> ArrayList<T> polyPowers(T poly, T polyModulus, DivisionWithRemainder.InverseModMonomial<T> invMod, int nIterations) {
        ArrayList<T> exponents = new ArrayList<>();
        polyPowers(polyMod(poly, polyModulus, invMod, true), polyModulus, invMod, nIterations, exponents);
        return exponents;
    }

    /** writes poly^{i} mod polyModulus for i in [0...nIterations] to exponents */
    private static <T extends IUnivariatePolynomial<T>> void polyPowers(T polyReduced, T polyModulus, DivisionWithRemainder.InverseModMonomial<T> invMod, int nIterations, ArrayList<T> exponents) {
        exponents.add(polyReduced.createOne());
        // polyReduced must be reduced!
        T base = polyReduced.clone();//polyMod(poly, polyModulus, invMod, true);
        exponents.add(base);
        T prev = base;
        for (int i = 0; i < nIterations; i++)
            exponents.add(prev = polyMod(prev.clone().multiply(base), polyModulus, invMod, false));
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     **/
    public static lUnivariatePolynomialZp powModulusMod(lUnivariatePolynomialZp poly,
                                                        lUnivariatePolynomialZp polyModulus,
                                                        DivisionWithRemainder.InverseModMonomial<lUnivariatePolynomialZp> invMod,
                                                        ArrayList<lUnivariatePolynomialZp> xPowers) {
        poly = polyMod(poly, polyModulus, invMod, true);
        return powModulusMod0(poly, polyModulus, invMod, xPowers);
    }

    /** doesn't do poly mod polyModulus first */
    private static lUnivariatePolynomialZp powModulusMod0(lUnivariatePolynomialZp poly,
                                                          lUnivariatePolynomialZp polyModulus,
                                                          DivisionWithRemainder.InverseModMonomial<lUnivariatePolynomialZp> invMod,
                                                          ArrayList<lUnivariatePolynomialZp> xPowers) {
        lUnivariatePolynomialZp res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i) {
            if (poly.data[i] == 0)
                continue;
            res.addMul(xPowers.get(i), poly.data[i]);
        }
        return polyMod(res, polyModulus, invMod, false);
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     **/
    public static <E> UnivariatePolynomial<E> powModulusMod(UnivariatePolynomial<E> poly,
                                                            UnivariatePolynomial<E> polyModulus,
                                                            DivisionWithRemainder.InverseModMonomial<UnivariatePolynomial<E>> invMod,
                                                            ArrayList<UnivariatePolynomial<E>> xPowers) {
        poly = polyMod(poly, polyModulus, invMod, true);
        return powModulusMod0(poly, polyModulus, invMod, xPowers);
    }

    /** doesn't do poly mod polyModulus first */
    private static <E> UnivariatePolynomial<E> powModulusMod0(UnivariatePolynomial<E> poly,
                                                              UnivariatePolynomial<E> polyModulus,
                                                              DivisionWithRemainder.InverseModMonomial<UnivariatePolynomial<E>> invMod,
                                                              ArrayList<UnivariatePolynomial<E>> xPowers) {
        UnivariatePolynomial<E> res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i) {
            if (poly.domain.isZero(poly.data[i]))
                continue;
            res.addMul(xPowers.get(i), poly.data[i]);
        }
        return polyMod(res, polyModulus, invMod, false);
    }

    /**
     * Returns {@code poly^modulus mod polyModulus} using precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     *
     * @param poly        the polynomial
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @param xPowers     precomputed monomial powers {@code x^{i*modulus} mod polyModulus} for i in {@code [0...degree(poly)]}
     * @return {@code poly^modulus mod polyModulus}
     * @see #xPowers(IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     **/
    @SuppressWarnings("unchecked")
    public static <T extends IUnivariatePolynomial<T>> T powModulusMod(T poly,
                                                                       T polyModulus,
                                                                       DivisionWithRemainder.InverseModMonomial<T> invMod,
                                                                       ArrayList<T> xPowers) {
        if (poly instanceof lUnivariatePolynomialZp)
            return (T) powModulusMod((lUnivariatePolynomialZp) poly, (lUnivariatePolynomialZp) polyModulus,
                    (DivisionWithRemainder.InverseModMonomial<lUnivariatePolynomialZp>) invMod, (ArrayList<lUnivariatePolynomialZp>) xPowers);
        else if (poly instanceof UnivariatePolynomial)
            return (T) powModulusMod((UnivariatePolynomial) poly, (UnivariatePolynomial) polyModulus,
                    (DivisionWithRemainder.InverseModMonomial) invMod, (ArrayList) xPowers);
        else
            throw new RuntimeException();
    }


    @SuppressWarnings("unchecked")
    private static <T extends IUnivariatePolynomial<T>> T powModulusMod0(T poly,
                                                                         T polyModulus,
                                                                         DivisionWithRemainder.InverseModMonomial<T> invMod,
                                                                         ArrayList<T> xPowers) {
        if (poly instanceof lUnivariatePolynomialZp)
            return (T) powModulusMod0((lUnivariatePolynomialZp) poly, (lUnivariatePolynomialZp) polyModulus, (DivisionWithRemainder.InverseModMonomial) invMod, (ArrayList) xPowers);
        else if (poly instanceof UnivariatePolynomial)
            return (T) powModulusMod0((UnivariatePolynomial) poly, (UnivariatePolynomial) polyModulus, (DivisionWithRemainder.InverseModMonomial) invMod, (ArrayList) xPowers);
        else
            throw new RuntimeException();
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated using Brent & Kung algorithm for modular composition.
     *
     * @param poly        the polynomial
     * @param pointPowers precomputed powers of evaluation point {@code point^{i} mod polyModulus}
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @param tBrentKung  Brent-Kung splitting parameter (optimal choice is ~sqrt(main.degree))
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see #polyPowers(IUnivariatePolynomial, IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial, int)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static <T extends IUnivariatePolynomial<T>> T compositionBrentKung(
            T poly,
            ArrayList<T> pointPowers,
            T polyModulus,
            DivisionWithRemainder.InverseModMonomial<T> invMod,
            int tBrentKung) {
        if (poly.isConstant())
            return poly;
        ArrayList<T> gj = new ArrayList<>();
        int degree = poly.degree();
        for (int i = 0; i <= degree; ) {
            int to = i + tBrentKung;
            if (to > (degree + 1))
                to = degree + 1;
            T g = poly.getRange(i, to);
            gj.add(powModulusMod0(g, polyModulus, invMod, pointPowers));
            i = to;
        }
        T pt = pointPowers.get(tBrentKung);
        T res = poly.createZero();
        for (int i = gj.size() - 1; i >= 0; --i)
            res = polyMod(res.multiply(pt).add(gj.get(i)), polyModulus, invMod, false);
        return res;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated using Brent & Kung algorithm for modular composition.
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static <T extends IUnivariatePolynomial<T>> T compositionBrentKung(T poly, T point, T polyModulus, DivisionWithRemainder.InverseModMonomial<T> invMod) {
        if (poly.isConstant())
            return poly;
        int t = safeToInt(Math.sqrt(poly.degree()));
        ArrayList<T> hPowers = polyPowers(point, polyModulus, invMod, t);
        return compositionBrentKung(poly, hPowers, polyModulus, invMod, t);
    }

    private static int safeToInt(double dbl) {
        if (dbl > Integer.MAX_VALUE || dbl < Integer.MIN_VALUE)
            throw new ArithmeticException("int overflow");
        return (int) dbl;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus } calculated with plain Horner scheme.
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @param invMod      pre-conditioned modulus ({@link DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)} )})
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static lUnivariatePolynomialZp compositionHorner(lUnivariatePolynomialZp poly, lUnivariatePolynomialZp point, lUnivariatePolynomialZp polyModulus, DivisionWithRemainder.InverseModMonomial<lUnivariatePolynomialZp> invMod) {
        if (poly.isConstant())
            return poly;
        lUnivariatePolynomialZp res = poly.createZero();
        for (int i = poly.degree; i >= 0; --i)
            res = polyMod(res.multiply(point).addMonomial(poly.data[i], 0), polyModulus, invMod, false);
        return res;
    }

    /**
     * Returns modular composition {@code poly(point) mod polyModulus}. Brent & Kung algorithm used
     * ({@link #compositionBrentKung(IUnivariatePolynomial, ArrayList, IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial, int)}
     *
     * @param poly        the polynomial
     * @param point       the evaluation point
     * @param polyModulus the monic polynomial modulus
     * @return modular composition {@code poly(point) mod polyModulus }
     * @see #polyPowers(IUnivariatePolynomial, IUnivariatePolynomial, DivisionWithRemainder.InverseModMonomial, int)
     * @see DivisionWithRemainder#fastDivisionPreConditioning(IUnivariatePolynomial)
     */
    public static <T extends IUnivariatePolynomial<T>> T composition(T poly, T point, T polyModulus) {
        return compositionBrentKung(poly, point, polyModulus, DivisionWithRemainder.fastDivisionPreConditioning(point));
    }
}
