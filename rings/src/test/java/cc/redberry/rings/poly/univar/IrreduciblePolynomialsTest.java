package cc.redberry.rings.poly.univar;

import cc.redberry.rings.Rings;
import cc.redberry.rings.bigint.BigInteger;
import cc.redberry.rings.poly.PolynomialFactorDecomposition;
import cc.redberry.rings.primes.SmallPrimes;
import cc.redberry.rings.util.RandomDataGenerator;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.rings.poly.univar.IrreduciblePolynomials.*;
import static cc.redberry.rings.poly.univar.UnivariatePolynomialArithmetic.createMonomialMod;
import static cc.redberry.rings.util.TimeUnits.nanosecondsToString;
import static org.junit.Assert.assertTrue;

/**
 * @since 1.0
 */
public class IrreduciblePolynomialsTest extends AUnivariateTest {
    @Test
    public void testIrreducibleRandom1() throws Exception {
        RandomGenerator rnd = getRandom();
        RandomDataGenerator rndd = getRandomData();
        int nIterations = its(5000, 1000);
        for (int i = 0; i < nIterations; i++) {
            long modulus = getModulusRandom(rndd.nextInt(5, 15));
            UnivariatePolynomialZp64 poly = RandomUnivariatePolynomials.randomMonicPoly(rndd.nextInt(5, 15), modulus, rnd);
            PolynomialFactorDecomposition<UnivariatePolynomialZp64> factors = UnivariateFactorization.Factor(poly);
            try {
                Assert.assertEquals(factors.size() == 1, IrreduciblePolynomials.irreducibleQ(poly));
            } catch (Throwable e) {
                System.out.println("Modulus: " + poly.ring.modulus);
                System.out.println("Poly: " + poly.toStringForCopy());
                System.out.println("Factors: " + factors);
                System.out.println("Irred: " + IrreduciblePolynomials.irreducibleQ(poly));
                throw e;
            }
        }
    }

    @Test
    public void test1() throws Exception {
        UnivariatePolynomialZp64 poly = UnivariatePolynomialZ64.create(18, 92, 51, 36, 61, 93, 14, 13, 45, 11, 21, 79, 61, 1).modulus(97);
        Assert.assertFalse(IrreduciblePolynomials.irreducibleQ(poly));
    }

    @Test
    public void test2() throws Exception {
        UnivariatePolynomialZp64 poly = UnivariatePolynomialZ64.create(42, 73, 0, 79, 47, 1).modulus(89);
        assertTrue(UnivariateFactorization.Factor(poly).toString(), IrreduciblePolynomials.irreducibleQ(poly));
    }

    @Test
    public void test3() throws Exception {
        UnivariatePolynomialZp64 poly = UnivariatePolynomialZ64.create(952, 1768, 349, 1839, 1538, 1851, 941, 167, 1).modulus(1861);
        int exponent = 7;

        UnivariateDivision.InverseModMonomial<UnivariatePolynomialZp64> invMod = UnivariateDivision.fastDivisionPreConditioning(poly);
        UnivariatePolynomialZp64 xq = createMonomialMod(poly.modulus(), poly, invMod);
        IntObjectMap<UnivariatePolynomialZp64> cache = new IntObjectHashMap<>();

        UnivariatePolynomialZp64 actual = IrreduciblePolynomials.composition(xq.clone(), exponent, poly, invMod, cache);
        UnivariatePolynomialZp64 expected = composition(xq.clone(), exponent, poly, invMod);
        UnivariatePolynomialZp64 expected0 = createMonomialMod(Rings.Z.pow(BigInteger.valueOf(poly.modulus()), exponent), poly, invMod);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expected0, actual);
    }

    @Test
    public void test4() {
        RandomGenerator rnd = getRandom();
        int prime = SmallPrimes.nextPrime(1 << 20);
        for (int i = 0; i < its(10, 30); ++i) {
            long start = System.nanoTime();
            UnivariatePolynomialZp64 poly = randomIrreduciblePolynomial(UnivariatePolynomialZp64.zero(prime), 128, rnd);
            System.out.println("Generate : " + nanosecondsToString(System.nanoTime() - start));

            start = System.nanoTime();
            PolynomialFactorDecomposition<UnivariatePolynomialZp64> factors = UnivariateFactorization.Factor(poly);
            System.out.println("Factor   : " + nanosecondsToString(System.nanoTime() - start));

            start = System.nanoTime();
            boolean test1 = IrreduciblePolynomials.finiteFieldIrreducibleViaModularComposition(poly);
            System.out.println("Test (m) : " + nanosecondsToString(System.nanoTime() - start));

            start = System.nanoTime();
            boolean test2 = IrreduciblePolynomials.finiteFieldIrreducibleBenOr(poly);
            System.out.println("Test (bo): " + nanosecondsToString(System.nanoTime() - start));
            System.out.println();

            assertTrue(test1);
            assertTrue(test2);
            assertTrue(factors.isTrivial());
        }
    }

    private static UnivariatePolynomialZp64 composition(UnivariatePolynomialZp64 xq, int n, UnivariatePolynomialZp64 poly, UnivariateDivision.InverseModMonomial<UnivariatePolynomialZp64> invMod) {
        UnivariatePolynomialZp64 composition = xq.clone();
        for (int i = 1; i < n; i++)
            composition = ModularComposition.composition(composition, xq, poly, invMod);
        return composition;
    }

    @Test
    public void testCachedComposition1() throws Exception {
        RandomGenerator rnd = getRandom();
        RandomDataGenerator rndd = getRandomData();
        int nIterations = its(100, 1000);
        for (int i = 0; i < nIterations; i++) {
            if (i % 10 == 0)
                System.out.println(i);
            long modulus = getModulusRandom(rndd.nextInt(5, 15));
            UnivariatePolynomialZp64 poly = RandomUnivariatePolynomials.randomMonicPoly(rndd.nextInt(5, 10), modulus, rnd);

            UnivariateDivision.InverseModMonomial<UnivariatePolynomialZp64> invMod = UnivariateDivision.fastDivisionPreConditioning(poly);
            UnivariatePolynomialZp64 xq = createMonomialMod(poly.modulus(), poly, invMod);
            IntObjectMap<UnivariatePolynomialZp64> cache = new IntObjectHashMap<>();

            int exponent = rndd.nextInt(1, 1024);
            UnivariatePolynomialZp64 actual = IrreduciblePolynomials.composition(xq.clone(), exponent, poly, invMod, cache);
            UnivariatePolynomialZp64 expected = composition(xq.clone(), exponent, poly, invMod);
            String msg = new StringBuilder()
                    .append("\npoly: " + poly.toStringForCopy())
                    .append("\nmodulus: " + modulus)
                    .append("\nexponent: " + exponent)
                    .toString();
            Assert.assertEquals(msg, expected, actual);
            if (exponent <= 64)
                Assert.assertEquals(msg, createMonomialMod(Rings.Z.pow(BigInteger.valueOf(poly.modulus()), exponent), poly, invMod), actual);

            for (IntCursor c : cache.keys()) {
                int ci = c.value;
                msg = msg + "\nexponent: " + ci;
                Assert.assertEquals(msg, composition(xq.clone(), ci, poly, invMod), cache.get(ci));
                if (ci <= 64)
                    Assert.assertEquals(msg, createMonomialMod(Rings.Z.pow(poly.coefficientRingCardinality(), ci), poly, invMod), cache.get(ci));
            }
        }
    }

    @Test
    public void test5() {
        UnivariatePolynomialZp64 poly = UnivariatePolynomialZ64.create(1, 1, 1).modulus(17);
        assertTrue(UnivariateFactorization.Factor(poly).isTrivial());
        assertTrue(finiteFieldIrreducibleViaModularComposition(poly));
        assertTrue(finiteFieldIrreducibleBenOr(poly));
    }

    @Test
    public void test6() {
        RandomGenerator rnd = getRandom();
        RandomDataGenerator rndd = getRandomData();
        int prime = 17;
        for (int i = 0; i < its(1000, 1000); ++i) {
            UnivariatePolynomialZp64 poly = randomIrreduciblePolynomial(UnivariatePolynomialZp64.zero(prime), rndd.nextInt(2, 32), rnd);
            PolynomialFactorDecomposition<UnivariatePolynomialZp64> factors = UnivariateFactorization.Factor(poly);
            boolean test1 = IrreduciblePolynomials.finiteFieldIrreducibleViaModularComposition(poly);
            boolean test2 = IrreduciblePolynomials.finiteFieldIrreducibleBenOr(poly);
            assertTrue(test1);
            assertTrue(test2);
            assertTrue(factors.isTrivial());
        }
    }
}