/**
 * @(#)FunctionTest.java 1.1 2002-03-24 Andre Platzer
 * 
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.math.functional;

import junit.framework.*;
import junit.extensions.*;

import orbital.math.*;
import orbital.math.Vector;
import orbital.math.Integer;
import orbital.math.functional.*;
import com.wolfram.jlink.*;
import java.lang.reflect.*;
import java.util.*;
import java.awt.Dimension;
import orbital.util.*;

/**
 * Automatic test-driver checking (some parts of) orbital.math.functional.* against Mathematica.
 *
 * @version $Id$
 * @author  Andr&eacute; Platzer
 * @todo test Rational as well
 */
public class FunctionTest extends check.TestCase {
    private static final int  TEST_REPETITION = 20;
    private static final Values vf = Values.getDefaultInstance();
    private static final Real tolerance = vf.valueOf(1e-8);
    private static final int DSOLVE_PRECISION_DIGITS = 3;
    private static final ArithmeticFormat mf = ArithmeticFormat.MATH_EXPORT_FORMAT;
        
    // the default matrix and vector (ddim.width) dimension
    private static final Dimension ddim = new Dimension(2,2);

    // test type bit mask constants
    public static final int   TYPE_NONE = 0;

    public static final int   TYPE_INTEGER = 1;
    public static final int   TYPE_REAL = 2;
    public static final int   TYPE_COMPLEX = 4;
    public static final int   TYPE_SCALAR = TYPE_INTEGER | TYPE_REAL | TYPE_COMPLEX;

    public static final int   TYPE_VECTOR = 32;
    public static final int   TYPE_MATRIX = 64;
    public static final int   TYPE_TENSOR = 128 | TYPE_MATRIX | TYPE_VECTOR;

    public static final int   TYPE_ALL = TYPE_SCALAR | TYPE_TENSOR;
    public static final int   TYPE_DEFAULT = TYPE_SCALAR;

    public static void main(String[] argv) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite() {
        //@internal could perhaps use RepeatedTest for testCalculations
        TestSuite suite = new TestSuite(FunctionTest.class);
        return suite;
    }

    private KernelLink ml;
    private Random random;
    protected void setUp() {
        random = new Random();
        /*
          (Windows)
          -linkmode launch -linkname 'c:/math40/mathkernel.exe'
        
          (Unix)
          -linkmode launch -linkname 'math -mathlink'
        */

        try {
            ml = MathLinkFactory.createKernelLink("-linkmode launch -linkname '"
                                                  + System.getProperty("com.wolfram.jlink.kernel")
                                                  + "'");
            ml.setComplexClass(ComplexAdapter.class);

            // Get rid of the initial InputNamePacket the kernel will send
            // when it is launched.
            ml.discardAnswer();

            // define our imaginary unit
	    //@xxx could have side effects for testdSolve
            ml.evaluate("i = I;");
            ml.discardAnswer();
        } catch (MathLinkException e) {
            throw new Error("Fatal error opening link: " + e.getMessage());
        }
    }
    
    protected void tearDown() throws MathLinkException {
	try {
	    ml.discardAnswer();
	}
	finally {
	    ml.close();
	}
    }

    
    public void testCalculations() {
        MathUtilities.setDefaultPrecisionDigits(17);
        try {
            ml.evaluate("2+2");
            ml.waitForAnswer();
            int result = ml.getInteger();
            System.out.println("2 + 2 = " + result);

            ml.evaluate("2+2*(I+1)");
            ml.waitForAnswer();
            Complex result2 = ((ComplexAdapter) ml.getComplex()).getValue();
            System.out.println("2 + 2*(I+1) = " + result2 + "\t" + result2.getClass());
                        
            final double MIN = -1000;
            final double MAX = +1000;
            final double EPS = Double.longBitsToDouble(Double.doubleToLongBits(1.0)+1)-1.0;
            final double SMIN = -10;
            final double SMAX = +10;
            final double PI = Math.PI;
            System.err.println("epsilon = " + EPS + " = " + Long.toString(Double.doubleToLongBits(EPS), 16));
            System.err.println("1 + epsilon = " + (1 + EPS));
            System.err.println("-1 + epsilon = " + (-1 + EPS));
            System.err.println("1 - epsilon = " + (1 - EPS));
            System.err.println("(1 + epsilon/2) + epsilon/2= " + ((1 + EPS/2) + EPS/2));
                        
            //delta, logistic, reciprocal, sign
            //@todo id, zero with tensor once Functions.zero has been adapted
            testFunction("(#1)&",       Functions.id, MIN, MAX, TYPE_ALL, TYPE_ALL);
            testFunction("(1)&",        Functions.one, MIN, MAX, TYPE_ALL, TYPE_ALL);
            testFunction("(0)&",        Functions.zero, MIN, MAX, TYPE_ALL, TYPE_ALL);
            testFunction("Plus",        Operations.plus, MIN, MAX, TYPE_ALL, TYPE_SCALAR);
            testFunction("Plus",        Operations.plus, MIN, MAX, TYPE_TENSOR, TYPE_SCALAR);
            testFunction("Plus",        Operations.plus, MIN, MAX, TYPE_TENSOR, TYPE_REAL);
            testFunction("Subtract",    Operations.subtract, MIN, MAX, TYPE_ALL, TYPE_SCALAR);
            testFunction("Subtract",    Operations.subtract, MIN, MAX, TYPE_TENSOR, TYPE_REAL);
            testFunction("Times",       Operations.times, MIN, MAX, TYPE_SCALAR, TYPE_SCALAR);
            //@todo Operations.times with TYPE_TENSOR
            testFunction("Dot", Operations.times, MIN, MAX, TYPE_MATRIX, TYPE_SCALAR);
            testFunction("Dot", Operations.times, MIN, MAX, TYPE_MATRIX, TYPE_REAL);
            //testFunction("Divide",    Operations.divide, MIN, MAX, TYPE_REAL | TYPE_COMPLEX);
            //testFunction("Power",     Operations.power, MIN, MAX);
            testFunction("Minus",       Operations.minus, MIN, MAX, TYPE_ALL, TYPE_SCALAR);
            //testFunction("Inverse",   Operations.inverse, MIN, MAX, TYPE_MATRIX);
            try {
                testFunction("Exp",     Functions.exp, -10, 10, TYPE_SCALAR, TYPE_NONE);
                testFunction("Log",     Functions.log, EPS, MAX, TYPE_SCALAR, TYPE_NONE);
            }
            catch (AssertionError ignore) {
                ignore.printStackTrace();
            }
                        
            testFunction("Sqrt",        Functions.sqrt, 0, MAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Abs",         Functions.norm, MIN, MAX, TYPE_ALL, TYPE_ALL);
            testFunction("(#1^2)&",     Functions.square, MIN, MAX, TYPE_SCALAR | TYPE_MATRIX, TYPE_SCALAR);
            //testFunction("DiracDelta",Functions.diracDelta, MIN, MAX);

            testFunction("Sin",         Functions.sin, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Cos",         Functions.cos, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Tan",         Functions.tan, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE); //...
            testFunction("Cot",         Functions.cot, -PI+EPS, -EPS, TYPE_SCALAR, TYPE_NONE); //...
            testFunction("Cot",         Functions.cot, EPS, PI-EPS, TYPE_SCALAR, TYPE_NONE);
            testFunction("Csc",         Functions.csc, EPS, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Csc",         Functions.csc, SMIN, EPS, TYPE_SCALAR, TYPE_NONE);
            testFunction("Sec",         Functions.sec, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Sinh",        Functions.sinh, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Cosh",        Functions.cosh, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Tanh",        Functions.tanh, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Coth",        Functions.coth, EPS, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Coth",        Functions.coth, SMIN, EPS, TYPE_SCALAR, TYPE_NONE);
            testFunction("Csch",        Functions.csch, SMIN, EPS, TYPE_SCALAR, TYPE_NONE);
            testFunction("Csch",        Functions.csch, EPS, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("Sech",        Functions.sech, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("ArcCos",      Functions.arccos, -1, 1, TYPE_SCALAR, TYPE_NONE);
            testFunction("ArcSin",      Functions.arcsin, -1, 1, TYPE_SCALAR, TYPE_NONE);
            //testFunction("ArcCot",    Functions.arccot, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);  // differs by PI for negative values. we return positive values
            testFunction("ArcTan",      Functions.arctan, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("ArcCosh",     Functions.arcosh, 1, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("ArcSinh",     Functions.arsinh, SMIN, SMAX, TYPE_SCALAR, TYPE_NONE);
            testFunction("ArcTanh",     Functions.artanh, -1+EPS, 1-EPS, TYPE_REAL | TYPE_COMPLEX, TYPE_NONE);
            System.out.println();
            System.out.println("PASSED");
        } catch (MathLinkException ex) {
            System.out.println();
            throw (RuntimeException) new RuntimeException("MathLinkException occurred: " + ex).initCause(ex);
        } 
    }
        
    /**
     * @param testType the types of arguments to test.
     * @param componentType the types of components of the arguments to test (if testType includes any tensors).
     * @todo add tensor types
     */
    private void testFunction(String mFunction, Function jFunction, double min, double max, int testType, int componentType) throws MathLinkException {
        try {
            // Integer value test
            for (int i = 0; (testType & TYPE_INTEGER) != 0 && i < TEST_REPETITION; i++) {
                compareResults(mFunction, jFunction,
                               vf.valueOf(integerArgument((int)Math.ceil(min),(int)Math.floor(max))));
            }
    
            // Real value test
            for (int i = 0; (testType & TYPE_REAL) != 0 && i < TEST_REPETITION; i++)
                try {
                    compareResults(mFunction, jFunction,
                                   vf.valueOf(realArgument(min,max)));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }
                                
            // Complex value test
            for (int i = 0; (testType & TYPE_COMPLEX) != 0 && i < TEST_REPETITION; i++)
                try {
                    compareResults(mFunction, jFunction,
                                   vf.complex(realArgument(min,max), realArgument(min,max)));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }
        }
        catch (UnsupportedOperationException ignore) {}
    }

    /**
     * Compares the results of a function application
     * of Java and of Mathematica.
     */
    private void compareResults(String mFunction, Function jFunction, Scalar x) throws MathLinkException {
        final String  mFunctionCall = mFunction + "[" + x + "]";
        final String  jFunctionCall = jFunction + "[" + x + "]";
        ml.evaluate("N[" + mFunctionCall + "]");
        ml.waitForAnswer();
        System.out.print(mFunctionCall + " = ");
        Complex mresult;
//      if (ml.getType() == MathLink.MLTKSYM && "ComplexInfinity".equals(ml.getSymbol()))
//          mresult = Values.INFINITY;
//      else
            mresult = ((ComplexAdapter) ml.getComplex()).getValue();
        System.out.println(mresult);
        final Complex jresult = (Complex) jFunction.apply(x);
        System.out.println(jFunctionCall + " = " + jresult);
        boolean isSuccessful = jresult.equals(mresult, tolerance);
        assertTrue(isSuccessful , mFunctionCall + " = " + mresult + " != " + jFunctionCall + " = " + jresult + "\tdelta=" + jresult.subtract(mresult));
    }

    /**
     * @param testType the types of arguments to test.
     * @param componentType the types of components of the arguments to test (if testType includes any tensors).
     * @internal the possible range of arguments is a major problem.
     */
    private void testFunction(String mFunction, BinaryFunction jFunction, double min, double max, int testType, int componentType) throws MathLinkException {
        try {
            // Integer value test
            for (int i = 0; (testType & TYPE_INTEGER) != 0 && i < TEST_REPETITION; i++)
                try {
                    compareResults(mFunction, jFunction,
                                   vf.valueOf(integerArgument((int)Math.ceil(min),(int)Math.floor(max))),
                                   vf.valueOf(integerArgument((int)Math.ceil(min),(int)Math.floor(max))));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }
    
            // Real value test
            for (int i = 0; (testType & TYPE_REAL) != 0 && i < TEST_REPETITION; i++)
                try {
                    compareResults(mFunction, jFunction,
                                   vf.valueOf(realArgument(min,max)),
                                   vf.valueOf(realArgument(min,max)));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }
    
            // Complex value test
            for (int i = 0; (testType & TYPE_COMPLEX) != 0 && i < TEST_REPETITION; i++)
                try {
                    compareResults(mFunction, jFunction,
                                   vf.complex(realArgument(min,max),realArgument(min,max)),
                                   vf.complex(realArgument(min,max),realArgument(min,max)));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }

            // Vector value test
            for (int i = 0; (testType & TYPE_VECTOR) != 0 && i < TEST_REPETITION; i++)
                try {
                    final Vector jx = vectorArgument(min,max, componentType, ddim.width);
                    final Vector jy = vectorArgument(min,max, componentType, ddim.width);
                    final String  mFunctionCall = mFunction + "[" + listForm(jx) + "," + listForm(jy) + "]";
                    final String  jFunctionCall = jFunction + "[" + jx + "," + jy + "]";
                    ml.evaluate("N[" + mFunctionCall + "]");
                    ml.waitForAnswer();
                    System.out.print(mFunctionCall + " = ");
                    final Vector mresult = vf.valueOf(ComplexAdapter.unconvert((ComplexAdapter[])ml.getComplexArray1()));
                    System.out.println(mresult);
                    final Vector jresult = (Vector) jFunction.apply(jx, jy);
                    System.out.println(jFunctionCall + " = " + jresult);
                    assertTrue(jresult.equals(mresult, tolerance) , mFunctionCall + " = " + mresult + " != " + jFunctionCall + " = " + jresult + "\n\tdelta=" + jresult.subtract(mresult));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }

            // Matrix value test
            for (int i = 0; (testType & TYPE_MATRIX) != 0 && i < TEST_REPETITION; i++)
                try {
                    final Matrix jx = matrixArgument(min,max, componentType, ddim);
                    final Matrix jy = matrixArgument(min,max, componentType, ddim);
                    final String  mFunctionCall = mFunction + "[" + listForm(jx) + "," + listForm(jy) + "]";
                    final String  jFunctionCall = jFunction + "[" + jx + "," + jy + "]";
                    ml.evaluate("N[" + mFunctionCall + "]");
                    ml.waitForAnswer();
                    System.out.print(mFunctionCall + " = ");
                    final Matrix mresult = vf.valueOf(ComplexAdapter.unconvert((ComplexAdapter[][])ml.getComplexArray2()));
                    System.out.println(mresult);
                    final Matrix jresult = (Matrix) jFunction.apply(jx, jy);
                    System.out.println(jFunctionCall + " = " + jresult);
                    assertTrue(jresult.equals(mresult, tolerance) , mFunctionCall + " =\n" + mresult + "\n!=\n" + jFunctionCall + " =\n" + jresult + "\ndelta=\n" + jresult.subtract(mresult));
                }
                catch (MathLinkException e) {
                    if (!"machine number overflow".equals(e.getMessage()))
                        throw e;
                }
        }
        catch (UnsupportedOperationException ignore) {}
    }

    /**
     * Compares the results of a binary function application
     * of Java and of Mathematica.
     */
    private void compareResults(String mFunction, BinaryFunction jFunction, Scalar x, Scalar y) throws MathLinkException {
        final String  mFunctionCall = mFunction + "[" + x + "," + y + "]";
        final String  jFunctionCall = jFunction + "[" + x + "," + y + "]";
        try {
            ml.evaluate("N[" + mFunctionCall + "]");
            ml.waitForAnswer();
            System.out.print(mFunctionCall + " = ");
            final Complex mresult = ((ComplexAdapter) ml.getComplex()).getValue();
            System.out.println(mresult);
            final Complex jresult = (Complex) jFunction.apply(x, y);
            System.out.println(jFunctionCall + " = " + jresult);
            boolean isSuccessful = jresult.equals(mresult, tolerance);
            assertTrue(isSuccessful , mFunctionCall + " = " + mresult + " != " + jFunctionCall + " = " + jresult + "\tdelta=" + jresult.subtract(mresult));
        }
        catch (MathLinkException e) {
            if (!"machine number overflow".equals(e.getMessage()))
                throw e;
        }
    }


    // testing more sophisticated symbolic algorithms

    public void testdSolve() throws MathLinkException {
	final double MIN = -5;
	final double MAX = +5;
	final int componentType = TYPE_INTEGER /*| TYPE_REAL*/;

	for (int rep = 0; rep < TEST_REPETITION; rep++) {
	    final int n = integerArgument(1,5);
	    final Dimension dim = new Dimension(n,n);
	    final Symbol t = vf.symbol("t");
	    final Real tau = vf.ZERO();
	    Matrix A = matrixArgument(MIN,MAX, componentType, dim);
	    // make strict upper diagonal matrix for nilpotence
	    for (int i = 0; i < A.dimension().height; i++) {
		for (int j = 0; j <= i && j < A.dimension().width; j++) {
		    A.set(i, j, vf.ZERO());
		}
	    }
	    Vector b = vf.ZERO(n);
	    // generalise eta to symbolic
	    Vector eta = vectorArgument(MIN,MAX, componentType, dim.height);
	    Function f = AlgebraicAlgorithms.dSolve(A, b, tau, eta);
	    System.out.println("Solving ODE x'(t) ==\n" + A + "*x(t) + " + b + "\nwith initial value  " + eta + " at " + tau + "\nyields " + f);
	    System.out.println("  solution at " + t + " is " + f.apply(t));

	    // compare results to Mathematica's DSolve
	    try {
		/*
		 * Generates essentially the following template
		 * Module[{A = {{4, -6}, {1, -1}},
		 *   eta = {2, 5},
		 *   X},
		 *  X[t_] = {x1[t], x2[t]};
		 *  X[t] /. DSolve[Join[MapThread[#1 == #2 &, {X'[t], A.X[t] + b}],
		 *      MapThread[#1 == #2 &, {X[0], eta}]
		 *      ],
		 *     X[t], t] [[1]]
		 *  ]	      
		 */
		final String oursol = mf.format(f.apply(t));
		// construct {x0[t], x1[t], ... x(n-1)[t]}
		String dimensionspace = "{";
		for (int i = 0; i < n; i++) {
		    dimensionspace += "x" + i + "[" + t + "]";
		    if (i + 1 < n)
			dimensionspace += ",";
		}
		dimensionspace += "}";
		String ode = "Module[{X}, X[" + t + "_] = " + dimensionspace + ";\n"
		    + "X[" + t + "] /.\n"
		    + "DSolve[Join[\n"
		    + " MapThread[#1==#2&, {X'[" + t + "], (" + mf.format(A) + ").X[" + t + "] + " + mf.format(b) + "}],\n"
		    + " MapThread[#1==#2&, {X[" + tau + "], (" + mf.format(eta) + ")}]],\n"
		    + " X[" + t + "], " + t + "][[1]]\n"
		    + "]";

		System.out.println(ode);
		ml.newPacket();
		ml.evaluate("" + ode + "");
		ml.waitForAnswer();
		final String refsol = ml.getExpr().toString();
		System.out.println("Our solution:\t\t" + oursol);
		System.out.println("Reference solution:\t" + refsol);

		System.out.println("Simplify[SetPrecision[(" + ode + " == " + oursol + "), " + DSOLVE_PRECISION_DIGITS + "]]");
		ml.newPacket();
		ml.evaluate("Simplify[SetPrecision[(\n" + ode + " == \n" + oursol + "),\n " + DSOLVE_PRECISION_DIGITS + "]]");
		ml.waitForAnswer();
		final String comparison = ml.getExpr().toString();
		System.out.println("Result:\t" + comparison);
		if (!comparison.equals("True"))
		    System.out.println("dSolve validation FAILED");
		assertTrue(comparison.equals("True") , " dSolve equivalence: " + oursol + " == " + refsol + " resulting in " + comparison);
		System.out.println("Next");
	    }
	    catch (MathLinkException e) {
		if (!"machine number overflow".equals(e.getMessage()))
		    throw e;
	    }
	}
    }
    

    // create (random) argument values
    
    private int integerArgument(int min, int max) {
        return min + random.nextInt(max-min);
    }
    private double realArgument(double min, double max) {
        return ((max-min) * random.nextDouble() + min);
    }
    private Scalar randomArgument(double min, double max, int testType) {
        if ((testType & TYPE_INTEGER) != 0 && Utility.flip(random, 0.4))
            return vf.valueOf(integerArgument((int)min, (int)max));
        else if ((testType & TYPE_COMPLEX) != 0 && Utility.flip(random, 0.4))
            return vf.complex(realArgument(min, max), realArgument(min, max));
//      else if ((testType & TYPE_RATIONAL) != 0 && Utility.flip(random, 0.4))
//          return vf.rational(integerArgument((int)min, (int)max), integerArgument(1, (int)max));
        else
            return vf.valueOf(realArgument(min, max));
    }
    private Matrix matrixArgument(double min, double max, int testType, Dimension dim) {
        Matrix x = vf.newInstance(dim);
        if (testType == TYPE_REAL && Utility.flip(random, 0.5))
            // randomly switch to RMatrix
            x = vf.valueOf(new double[dim.height][dim.width]);
        for (int i = 0; i < dim.height; i++)
            for (int j = 0; j < dim.width; j++)
                x.set(i,j, randomArgument(min, max, testType));
        return x;
    }
    private Vector vectorArgument(double min, double max, int testType, int dim) {
        Vector x = vf.newInstance(dim);
        if (testType == TYPE_REAL && Utility.flip(random, 0.5))
            // randomly switch to RVector
            x = vf.valueOf(new double[dim]);
        for (int i = 0; i < dim; i++)
            x.set(i, randomArgument(min, max, testType));
        return x;
    }

    // Helpers

    /**
     * Adapter class between orbital.math.* and J/Link.
     * @structure delegate value:Complex
     */
    public static class ComplexAdapter {
        private Complex value;
        public ComplexAdapter(double re, double im) {
            value = vf.complex(re,im);
        }
                
        public static final Complex[] unconvert(ComplexAdapter[] v) {
            Complex[] r = new Complex[v.length];
            for (int i = 0; i < r.length; i++)
                r[i] = v[i].getValue();
            return r;
        }
        public static final Complex[][] unconvert(ComplexAdapter[][] v) {
            Complex[][] r = new Complex[v.length][v[0].length];
            for (int i = 0; i < r.length; i++)
                for (int j = 0; j < r[i].length; j++)
                    r[i][j] = v[i][j].getValue();
            return r;
        }
                
        /**
         * Whether we are equal to another ComplexAdapter or Complex.
         */
        public boolean equals(Object o) {
            return o instanceof ComplexAdapter && value.equals(((ComplexAdapter)o).value)
                || Complex.isa.apply(o) && value.equals(o);
        }
                
        public double re() {
            return value.re().doubleValue();
        }

        public double im() {
            return value.im().doubleValue();
        }
            
        public Complex getValue() {
            return value;
        }
            
        public String toString() {
            return value.toString();
        }
    }
        
    private String listForm(Matrix m) {
        String           nl = " ";//System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (int i = 0; i < m.dimension().height; i++) {
            sb.append((i == 0 ? "" : "," + nl) + '{');
            for (int j = 0; j < m.dimension().width; j++)
                sb.append((j == 0 ? "" : ",\t") + m.get(i, j));
            sb.append('}');
        } 
        sb.append('}');
        return sb.toString();
    }

    private String listForm(Vector m) {
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        for (int i = 0; i < m.dimension(); i++)
            sb.append((i == 0 ? "" : ", ") + m.get(i));
        sb.append('}');
        return sb.toString();
    }
}
