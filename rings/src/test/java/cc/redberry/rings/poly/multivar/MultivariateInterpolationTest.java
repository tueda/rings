package cc.redberry.rings.poly.multivar;

import cc.redberry.rings.IntegersZp;
import cc.redberry.rings.Ring;
import cc.redberry.rings.bigint.BigInteger;
import com.carrotsearch.hppc.IntHashSet;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import java.util.List;

import static cc.redberry.rings.poly.multivar.MultivariatePolynomial.parse;
import static cc.redberry.rings.poly.multivar.RandomMultivariatePolynomials.randomPolynomial;
import static org.junit.Assert.assertEquals;

/**
 * @since 1.0
 */
public class MultivariateInterpolationTest extends AMultivariateTest {
    @Test
    @SuppressWarnings("unchecked")
    public void test1() throws Exception {
        String[] variables = {"a", "b"};
        Ring<BigInteger> ring = new IntegersZp(17);
        MultivariatePolynomial<BigInteger> val1 = MultivariatePolynomial.parse("a^2 + a^3 + 1", ring, MonomialOrder.LEX, variables);
        MultivariatePolynomial<BigInteger> val2 = MultivariatePolynomial.parse("12*a^2 + 13*a^3 + 11", ring, MonomialOrder.LEX, variables);
        MultivariatePolynomial<BigInteger> val3 = MultivariatePolynomial.parse("2*a^2 + 3*a^3 + 1", ring, MonomialOrder.LEX, variables);

        BigInteger[] points = {BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3)};
        MultivariatePolynomial<BigInteger>[] values = new MultivariatePolynomial[]{val1, val2, val3};
        MultivariatePolynomial<BigInteger> res = MultivariateInterpolation.interpolateNewton(1, points, values);
        assertInterpolation(1, res, points, values);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test2() throws Exception {
        String[] variables = {"a", "b"};
        Ring<BigInteger> ring = new IntegersZp(17);
        MultivariatePolynomial<BigInteger> val1 = MultivariatePolynomial.parse("a^2 + a^3 + 1", ring, MonomialOrder.LEX, variables);
        MultivariatePolynomial<BigInteger> val2 = MultivariatePolynomial.parse("12*a^2 + 13*a^3 + 11", ring, MonomialOrder.LEX, variables);
        MultivariatePolynomial<BigInteger> val3 = MultivariatePolynomial.parse("2*a^2 + 3*a^3 + 1", ring, MonomialOrder.LEX, variables);

        BigInteger[] points = {BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3)};
        MultivariatePolynomial<BigInteger>[] values = new MultivariatePolynomial[]{val1, val2, val3};
        MultivariatePolynomial<BigInteger> res = MultivariateInterpolation.interpolateNewton(1, points, values);

        MultivariateInterpolation.Interpolation<BigInteger> interpolation = new MultivariateInterpolation.Interpolation<>(1,
                points[0], values[0]);//= interpolation(1, Arrays.copyOf(points, 2), Arrays.copyOf(values, 2));
        interpolation.update(points[1], values[1]);
        interpolation.update(points[2], values[2]);

        assertInterpolation(1, res, points, values);
        assertInterpolation(interpolation);
        assertEquals(res, interpolation.getInterpolatingPolynomial());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test3() throws Exception {
        IntegersZp domain = new IntegersZp(197);
        String[] vars = {"a", "b", "c"};
        MultivariatePolynomial<BigInteger> base = parse("15*b*c^2+47*b^4*c^4+144*a*b^5*c^5+150*a^5*b^4+62*a^5*b^4*c", domain, MonomialOrder.LEX);
        int var = 2;
        BigInteger[] points = {
                BigInteger.valueOf(1),
                BigInteger.valueOf(10),
                BigInteger.valueOf(20),
                BigInteger.valueOf(30),
                BigInteger.valueOf(40),
                BigInteger.valueOf(50)
        };
        MultivariatePolynomial<BigInteger>[] values = base.evaluate(var, points);

        MultivariatePolynomial<BigInteger> interpolated = MultivariateInterpolation.interpolateNewton(var, points, values);
        assertInterpolation(var, interpolated, points, values);

        MultivariateInterpolation.Interpolation<BigInteger> interpolation = new MultivariateInterpolation.Interpolation<>(var, points[0], values[0]);
        for (int i = 1; i < points.length; i++)
            interpolation.update(points[i], values[i]);

        assertEquals(interpolated, interpolation.getInterpolatingPolynomial());
        assertInterpolation(interpolation);
    }

    @Test
    public void testRandom() throws Exception {
        RandomGenerator rnd = getRandom();
        RandomDataGenerator rndd = getRandomData();

        int nIterations = its(500, 2000);
        for (int n = 0; n < nIterations; n++) {

            int nVars = rndd.nextInt(2, 4);
            IntegersZp domain = new IntegersZp(getModulusRandom(16));
            MultivariatePolynomial<BigInteger> base = randomPolynomial(nVars, rndd.nextInt(3, 10), rndd.nextInt(5, 10), domain, MonomialOrder.LEX, rnd);
            int var = rndd.nextInt(0, nVars - 1);
            int degree = base.degrees()[var];

            MultivariateInterpolation.Interpolation<BigInteger> interpolation = new MultivariateInterpolation.Interpolation<>(var, base);
            IntHashSet seen = new IntHashSet();
            for (int i = 0; i <= degree + 2; i++) {
                int iPoint = rndd.nextInt(0, domain.modulus.intValue() - 1);
                if (seen.contains(iPoint))
                    continue;
                seen.add(iPoint);

                BigInteger point = BigInteger.valueOf(iPoint);
                MultivariatePolynomial<BigInteger> evaluated = base.evaluate(var, point);

                interpolation.update(point, evaluated);
            }

            BigInteger[] points = interpolation.getPoints().toArray(new BigInteger[0]);
            MultivariatePolynomial[] values = interpolation.getValues().toArray(new MultivariatePolynomial[0]);
            MultivariatePolynomial<BigInteger> res = MultivariateInterpolation.interpolateNewton(var, points, values);

            assertInterpolation(var, res, points, values);
            assertInterpolation(var, base, points, values);
            assertInterpolation(interpolation);
            assertEquals(base, interpolation.getInterpolatingPolynomial());
        }
    }

    private static <E> void assertInterpolation(MultivariateInterpolation.Interpolation<E> interpolation) {
        assertInterpolation(interpolation.getVariable(), interpolation.getInterpolatingPolynomial(), interpolation.getPoints(), interpolation.getValues());
    }

    private static <E> void assertInterpolation(int variable, MultivariatePolynomial<E> poly, E[] points, MultivariatePolynomial<E>[] values) {
        for (int i = 0; i < points.length; i++)
            assertEquals(values[i], poly.evaluate(variable, points[i]));
    }

    private static <E> void assertInterpolation(int variable, MultivariatePolynomial<E> poly, List<E> points, List<MultivariatePolynomial<E>> values) {
        for (int i = 0; i < points.size(); i++)
            assertEquals(values.get(i), poly.evaluate(variable, points.get(i)));
    }
}