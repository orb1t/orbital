/**
 * @(#)AlgebraicAlgorithmsTest.java 1.1 2002-10-26 Andre Platzer
 * 
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.math;

import java.util.*;
import orbital.math.functional.Operations;
import junit.framework.*;

/**
 * A test case, testing .
 * @version 1.1, 2002-10-26
 */
public class AlgebraicAlgorithmsTest extends check.TestCase {
    private static final int TEST_REPETITIONS = 1000;
    private static final int MAX = 1000;
    private static final int PRIMES_BIT_LENGTH = 5;
    private static final Comparator order = AlgebraicAlgorithms.LEXICOGRAPHIC;

    private static final Values vf = Values.getDefaultInstance();
    private Random random;
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
	return new TestSuite(AlgebraicAlgorithmsTest.class);
    }
    protected void setUp() {
	random = new Random();
    }

    /**
     * @internal similar to the algorithm computing exact solution to integer LGS.
     */
    public void testChineseRemainder() {
	for (int rep = 0; rep < TEST_REPETITIONS; rep++) {
	    final Integer result = vf.valueOf(random.nextInt(MAX));
	    // choose enough coprime numbers (we simply use primes) such that the result is unique
	    Integer M = vf.valueOf(1);
	    Set coprimes = new HashSet();   // nonempty set, in fact contains even primes
	    do {
		Integer prime = vf.valueOf(MathUtilities.generatePrime(PRIMES_BIT_LENGTH, random));
		if (coprimes.contains(prime))
		    continue;
		coprimes.add(prime);
		M = M.multiply(prime);
	    } while (M.compareTo(result) <= 0);
	    final Integer m[] = (Integer[]) coprimes.toArray(new Integer[0]);
	    final Integer x[] = new Integer[m.length];
	    for (int i = 0; i < x.length; i++)
		x[i] = (Integer) vf.quotient(result, m[i]).representative();
	    final Integer umod = (Integer) Operations.product.apply(vf.valueOf(m));
	    assert umod.equals(M) : umod + " = " + M;

	    System.out.println("expect solution:  " + result);
	    System.out.println("#congruences:     " + m.length);
	    System.out.println("congruent values: " + MathUtilities.format(x));
	    System.out.println("modulo values:    " + MathUtilities.format(m));
	    System.out.println("solution:         " + AlgebraicAlgorithms.chineseRemainder(x,m));
	    System.out.println("is unique modulo: " + umod);
	    System.out.println();
	    assertEquals(result, AlgebraicAlgorithms.chineseRemainder(x,m).representative());
	}
    }
    
    public void testGroebnerBasisSimple() {
	// some polynomials in <b>Q</b>[X,Y]
	Polynomial/*<Rational>*/ g =
	    vf.polynomial(new int[][] {
		{0, 1},
		{1, 0}
	    });
	Set/*_<Polynomial<Rational>>_*/ G = new HashSet();
	G.add(g);

	assertEquals(AlgebraicAlgorithms.groebnerBasis(G, order) , G);

	Set/*_<Polynomial<Rational>>_*/ H = new HashSet(G);
	H.add(g.multiply(g));
	H.add(g.multiply(g.add(g)).subtract(g));
	assertEquals(AlgebraicAlgorithms.groebnerBasis(H, order) , G);
    }

    public void testPolynomialQuotientCalculation() {
	System.out.println("calculate with quotients of polynomials");
	// create elements in <b>R</b>[X]/(Y^2-X^3-X^2)
	final Collection m = Arrays.asList(new Polynomial[] {
	    // alternative form of construction: explicit concatenation
	    // of monomials. This is more to type, but also more
	    // simple to construct
	    vf.MONOMIAL(new int[] {0,2}).subtract(vf.MONOMIAL(new int[] {3,0}))
	    .subtract(vf.MONOMIAL(new int[] {2,0}))
	});
	// the Groebner basis of m
	final Set gm = AlgebraicAlgorithms.groebnerBasis(new HashSet(m), order);
	Quotient/*<Polynomial<Real>>*/ f =
	    vf.quotient(vf.polynomial(new double[][] {
		{2,1},
		{3,0}
	    }), gm, order);
	Quotient/*<Polynomial<Real>>*/ g =
	    vf.quotient(vf.polynomial(new double[][] {
		{-1,1},
		{1,1}
	    }), gm, order);

	// perform calculations in both fields
	System.out.println("perform calculations in a quotient ring modulo " + m);

	f = vf.quotient(vf.polynomial(new double[][] {
	    {2,-1},
	    {3,0},
	    {-1,0}
	}), gm, order);

	assertEquals(f.multiply(g).representative(), vf.polynomial(new int[][] {
	    {-2,3,-2,-1},
	    {-1,4,-1,0},
	    {5,3,0,0}
	}));
    }
}