/**
 * @(#)Values.java 1.0 2000/08/08 Andre Platzer
 * 
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.math;

import java.awt.Dimension;
import java.text.ParseException;
import orbital.logic.functor.Function;

import java.util.List;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Arrays;
import orbital.util.Setops;
import orbital.util.Utility;

import orbital.math.functional.Functions;
import java.util.ListIterator;

/**
 * Scalar value and arithmetic object value constructor and utilities class.
 * <p>
 * This class is the central static factory facade for instantiating new arithmetic objects
 * from primitive types values.
 * </p>
 * <p>
 * Since most general arithmetic objects are modelled as interfaces to provide a maximum of
 * flexibility, you need factory methods to create an arithmetic object value. The class
 * <tt><a href="Values.html">Values</a></tt> is that central factory class which can
 * create arithmetic object values from all kinds of primitive types.
 * This indirection introduces a more loosely coupled binding between users and providers
 * of arithmetic object classes.
 * As the indirection is provided by static methods, JIT compilers can optimize the resulting
 * code to prevent performance loss. Indeed, because of this delegative construction the
 * factory method can chose the best implementation class suitable for a specific primitive type
 * and size.
 * </p>
 * 
 * @version 1.0, 2000/08/08
 * @author  Andr&eacute; Platzer
 * @see <a href="{@docRoot}/DesignPatterns/AbstractFactory.html">Abstract Factory</a>
 * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade</a>
 * @see <a href="{@docRoot}/DesignPatterns/FacadeFactory.html">&quot;FacadeFactory&quot;</a>
 * @see java.util.Arrays
 * @todo perhaps we should only call true primitive and java.lang.Number type conversion methods valueOf(...). rename the rest of them according to the type they return. 
 */
public final class Values {
    private static class Debug {
	private static final java.util.logging.Logger test = java.util.logging.Logger.getLogger("orbital.test");
	private Debug() {}
	public static void main(String arg[]) throws Exception {
	    Arithmetic a[] = new Arithmetic[] {
		Values.valueOf(5), Values.rational(1, 4), Values.valueOf(1.23456789), Values.complex(-1, 2)
	    };
	    Arithmetic b[] = new Arithmetic[] {
		Values.valueOf(7), Values.rational(3, 4), Values.valueOf(3.1415926), Values.complex(3, 2)
	    };
	    for (int k = 0; k < a.length; k++) {
		test.info(a[k].getClass() + " arithmetic combined with various types");
		for (int i = 0; i < b.length; i++)
		    System.out.println(a[k] + "+" + b[i] + " = " + a[k].add(b[i]) + "\tof " + a[k].add(b[i]).getClass());
		Object x1, x2;
		assert (x1 = a[k].add(b[0])).equals(x2 = a[k].add((Integer) b[0])) && (x1.getClass() == x2.getClass()) : "compile-time sub-type result equals run-time sub-type result";
		assert (x1 = a[k].add(b[1])).equals(x2 = a[k].add((Rational) b[1])) && (x1.getClass() == x2.getClass()) : "compile-time sub-type result equals run-time sub-type result";
		assert (x1 = a[k].add(b[2])).equals(x2 = a[k].add((Real) b[2])) && (x1.getClass() == x2.getClass()) : "compile-time sub-type result equals run-time sub-type result";
		assert (x1 = a[k].add(b[3])).equals(x2 = a[k].add((Complex) b[3])) && (x1.getClass() == x2.getClass()) : "compile-time sub-type result equals run-time sub-type result";
	    } 
	} 
    }	 // Debug

    
    /**
     * prevent instantiation - module class
     */
    private Values() {}
    
    // scalar value constructors - facade factory
    // primitive type conversion methods

    // integer scalar value constructors - facade factory

    /**
     * Returns an Scalar whose value is equal to that of the specified primitive type.
     * @post RES.intValue() == val
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @see java.math.BigInteger#valueOf(long)
     */
    public static Integer valueOf(int val) {
	// If -MAX_CONSTANT < val < MAX_CONSTANT, return stashed constant
	if (val == 0)
	    return ZERO;
	if (val > 0 && val <= MAX_CONSTANT)
	    return posConst[val];
	else if (val < 0 && val >= -MAX_CONSTANT)
	    return negConst[-val];
	else
	    return new AbstractInteger.Int(val);
    } 
    public static Integer valueOf(java.lang.Integer val) {
	return valueOf(val.intValue());
    }
    /**
     * Returns a Scalar whose value is equal to that of the specified primitive type.
     * @post RES.longValue() == val
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @see java.math.BigInteger#valueOf(long)
     */
    public static Integer valueOf(long val) {
	return -MAX_CONSTANT < val && val < MAX_CONSTANT
	    ? valueOf((int) val)
	    : new AbstractInteger.Long(val);
    }
    public static Integer valueOf(java.lang.Long val) {
	return valueOf(val.longValue());
    }
    public static Integer valueOf(byte val) {
	return valueOf((int) val);
    }
    public static Integer valueOf(java.lang.Byte val) {
	return valueOf(val.byteValue());
    }
    public static Integer valueOf(short val) {
	return valueOf((int) val);
    }
    public static Integer valueOf(java.lang.Short val) {
	return valueOf(val.shortValue());
    }
    /**
     * Not yet supported.
     * Legacy conversion method.
     * @todo introduce valueOf(BigInteger) and valueOf(BigDecimal) some day. However, care about non-associative precisions.
     */
    public static Integer valueOf(java.math.BigInteger val) {
	throw new UnsupportedOperationException("conversion from " + val.getClass() + " is not currently supported, first convert it to a primitive type, instead");
    }

    // real scalar value constructors - facade factory

    /**
     * Returns a Scalar whose value is equal to that of the specified primitive type.
     * @post RES.doubleValue() == val
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @see java.math.BigInteger#valueOf(long)
     */
    public static Real valueOf(double val) {
	return new AbstractReal.Double(val);
    } 
    public static Real valueOf(java.lang.Double val) {
	return valueOf(val.doubleValue());
    }
    /**
     * Returns a Scalar whose value is equal to that of the specified primitive type.
     * @post RES.floatValue() == val
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @see java.math.BigInteger#valueOf(long)
     */
    public static Real valueOf(float val) {
	//@xxx return new AbstractReal.Float(val)
	return new AbstractReal.Double(val);
    } 
    public static Real valueOf(java.lang.Float val) {
	return valueOf(val.floatValue());
    }
    /**
     * Not yet supported.
     * Legacy conversion method.
     */
    public static Real valueOf(java.math.BigDecimal val) {
	throw new UnsupportedOperationException("conversion from " + val.getClass() + " is not currently supported, first convert it to a primitive type, instead");
    }

    // scalar value constructors - facade factory

    /**
     * Returns a Scalar whose value is equal to that of the specified number.
     * Legacy conversion method.
     * @return an instance of scalar that has the same value as the number.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @see #valueOf(double)
     */
    public static Scalar valueOf(Number val) {
	if (val == null)
	    return null;
	else if (val instanceof Scalar)
	    return (Scalar) val;
	else if (val instanceof java.lang.Integer)
	    return valueOf((java.lang.Integer) val);
	else if (val instanceof java.lang.Long)
	    return valueOf((java.lang.Long) val);
	else if (val instanceof java.lang.Double)
	    return valueOf((java.lang.Double) val);
	else if (val instanceof java.lang.Float)
	    return valueOf((java.lang.Float) val);
	else if (val instanceof java.lang.Byte)
	    return valueOf((java.lang.Byte) val);
	else if (val instanceof java.lang.Short)
	    return valueOf((java.lang.Short) val);
	else if (val instanceof java.math.BigInteger)
	    return valueOf((java.math.BigInteger) val);
	else if (val instanceof java.math.BigDecimal)
	    return valueOf((java.math.BigDecimal) val);
	else
	    return narrow(valueOf(val.doubleValue()));
    }

	
    // non-standard naming scalar value constructors

    /**
     * Returns a new rational whose value is equal to p/q.
     * @param p the numerator of p/q.
     * @param q the denominator p/q.
     */
    public static Rational rational(Integer p, Integer q) {
	return new AbstractRational.RationalImpl((AbstractInteger) p, (AbstractInteger) q);
    } 
    public static Rational rational(int p, int q) {
	return new AbstractRational.RationalImpl(p, q);
    } 
    /**
     * Returns a new (integer) rational whose value is equal to p/1.
     * @param p the numerator of p/1.
     */
    public static Rational rational(Integer p) {
	return new AbstractRational.RationalImpl((AbstractInteger) p);
    } 
    public static Rational rational(int p) {
	return new AbstractRational.RationalImpl(p);
    } 

    // complex scalar values constructors

    /**
     * Returns a new complex whose value is equal to a + <b>i</b>*b.
     * @param a real part.
     * @param b imaginary part.
     * @return a + <b>i</b>*b.
     * @see #cartesian(Real, Real)
     */
    public static Complex complex(Real a, Real b) {
	return cartesian(a, b);
    } 
    public static Complex complex(double a, double b) {
	return cartesian(a, b);
    } 
    public static Complex complex(float a, float b) {
	return complex((double)a, (double)b);
    }
    public static Complex complex(int a, int b) {
	return complex((double)a, (double)b);
    }
    public static Complex complex(long a, long b) {
	return complex((double)a, (double)b);
    }

    /**
     * Returns a new (real) complex whose value is equal to a + <b>i</b>*0.
     * @param a real part.
     * @return a + <b>i</b>*0.
     * @see #complex(Real, Real)
     */
    public static Complex complex(Real a) {
	return complex(a, Values.ZERO);
    } 
    public static Complex complex(double a) {
	return complex(a, 0);
    } 

    /**
     * Creates a new complex from cartesian coordinates.
     * @param a real part.
     * @param b imaginary part.
     * @return a + <b>i</b>*b.
     * @see #polar(Real, Real)
     */
    public static Complex cartesian(Real a, Real b) {
	return new AbstractComplex.ComplexImpl(a, b);
    } 
    public static Complex cartesian(double a, double b) {
	return new AbstractComplex.ComplexImpl(a, b);
    } 

    /**
     * Creates a new complex from polar coordinates with r*<b>e</b><sup><b>i</b>&phi;</sup>.
     * @param r = |z| is the length.
     * @param phi = &phi; is the angle &ang; in radians.
     * @pre r&ge;0
     * @return r*<b>e</b><sup><b>i</b>&phi;</sup> = r * (cos &phi; + <b>i</b> sin &phi;).
     * @see #cartesian(Real, Real)
     */
    public static Complex polar(Real r, Real phi) {
	return new AbstractComplex.ComplexImpl(r.multiply((Real) Functions.cos.apply(phi)), r.multiply((Real) Functions.sin.apply(phi)));
    } 
    public static Complex polar(double r, double phi) {
	return new AbstractComplex.ComplexImpl(r * Math.cos(phi), r * Math.sin(phi));
    } 


    // static utilities concerning Vectors
	 
    /**
     * Returns a Vector containing the specified arithmetic objects.
     * <p>
     * Note that the resulting vector may or may not be backed by the
     * specified array.
     * </p>
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     */
    public static /*<R implements Arithmetic>*/ Vector/*<R>*/ valueOf(Arithmetic/*>R<*/[] values) {
	return new ArithmeticVector/*<R>*/(values);
    } 
    //@todo couldn't we even return Vector<Real>?
    public static Vector valueOf(double[] values) {
	return new RVector(values);
    } 
    public static Vector/*<Integer>*/ valueOf(int[] values) {
	// kind of map valueOf
	Vector/*<Integer>*/ v = getInstance(values.length);
	for (int i = 0; i < values.length; i++)
	    v.set(i, valueOf(values[i]));
	return v;
    } 

    //@todo introduce Vector vector(List<R> values) and Matrix matrix(List<List<R>> values)
    static /*<R implements Arithmetic>*/ Vector/*<R>*/ vector(List/*_<R>_*/ values) {
	Vector/*<R>*/   r = Values.getInstance(values.size());
	Iterator/*_<R>_*/   it = values.iterator();
	for (int i = 0; i < values.size(); i++)
	    r.set(i, (Arithmetic/*>R<*/) it.next());
	assert !it.hasNext() : "iterator should be finished after all elements";
	return r;
    }

    /**
     * Creates a new instance of vector with the specified dimension.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     */
    public static /*<R implements Arithmetic>*/ Vector/*<R>*/ getInstance(int dim) {
	return new ArithmeticVector/*<R>*/(dim);
    } 

    /**
     * Gets zero Vector, with all elements set to <code>0</code>.
     */
    public static Vector ZERO(int n) {
	return CONST(n, Values.ZERO);
    } 

    /**
     * Gets unit base Vector <code>i</code>, with all elements set to <code>0</code> except element <code>i</code> set to <code>1</code>.
     * These <code>e<sub>i</sub></code> are the standard base of <code><b>R</b><sup>n</sup></code>:
     * &forall;x&isin;<b>R</b><sup>n</sup> &exist;! x<sub>k</sub>&isin;<b>R</b>: x = x<sub>1</sub>*e<sub>1</sub> + ... + x<sub>n</sub>*e<sub>n</sub>.
     */
    public static /*<R implements Scalar>*/ Vector/*<R>*/ BASE(int n, int e_i) {
	ArithmeticVector/*<R>*/ base = (ArithmeticVector/*<R>*/) (Vector/*<R>*/) getInstance(n);
	for (int i = 0; i < base.dimension(); i++)
	    base.D[i] = (Arithmetic/*>R<*/) Values.valueOf(i == e_i ? 1 : 0);
	return base;
    } 

    /**
     * Gets a constant Vector, with all elements set to <code>c</code>.
     */
    public static /*<R implements Arithmetic>*/ Vector/*<R>*/ CONST(int n, Arithmetic/*>R<*/ c) {
	ArithmeticVector/*<R>*/ constant = (ArithmeticVector/*<R>*/) (Vector/*<R>*/) getInstance(n);
	Arrays.fill(constant.D, c);
	return constant;
    } 


    /**
     * Returns an unmodifiable view of the specified vector.
     * This method allows modules to provide users with "read-only" access to constant vectors.
     * <p>
     * Query operations on the returned vector "read through" to the specified vector,
     * and attempts to modify the returned vector, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     * <p>
     * Note that cloning a constant vector will not return a constant matrix, but a clone of the
     * specified vector.</p>
     */
    public static /*<R implements Arithmetic>*/ Vector/*<R>*/ constant(final Vector/*<R>*/ v) {
	return /*refine/delegate Vector*/ new AbstractVector/*<R>*/() {
		protected Vector/*<R>*/ newInstance(int d) {throw new AssertionError("this method should never get called in this context");}
		public int dimension() { return v.dimension(); }
		public Arithmetic/*>R<*/ get(int i) { return v.get(i); }
		public void set(int i, Arithmetic/*>R<*/ v) { throw new UnsupportedOperationException(); }
		protected void set(Arithmetic/*>R<*/ v[]) { throw new UnsupportedOperationException(); }
		public Iterator iterator() { return Setops.unmodifiableListIterator((ListIterator)v.iterator()); }
		public boolean equals(Object b) { return v.equals(b); }
		public int hashCode() { return v.hashCode(); }
		public Object clone() { return v.clone(); }
		public Real norm() { return v.norm(); }
		public Real norm(double p) { return v.norm(p); }
		public Arithmetic add(Arithmetic b) { return v.add(b); }
		public Arithmetic subtract(Arithmetic b) { return v.subtract(b); }
		public Arithmetic minus() { return v.minus(); }
		public Arithmetic multiply(Arithmetic b) { return v.multiply(b); }
		public Arithmetic scale(Arithmetic b) { return v.scale(b); }
		public Arithmetic inverse() { return v.inverse(); }
		public Arithmetic divide(Arithmetic b) { return v.divide(b); }
		public Arithmetic power(Arithmetic b) { return v.power(b); }
		public Vector/*<R>*/ cross(Vector/*<R>*/ b) { return v.cross(b); }
		public Matrix/*<R>*/ transpose() { return v.transpose(); }
		public Vector/*<R>*/ insert(int i, Arithmetic/*>R<*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ insert(int i, Vector/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ append(Arithmetic/*>R<*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ append(Vector/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ remove(int i) { throw new UnsupportedOperationException(); }
		public Arithmetic/*>R<*/[] toArray() { return v.toArray(); }
		public String toString() { return v.toString(); }
	    };
    }

    // static utilitiy methods concerning Matrices

    /**
     * Returns a Matrix containing the specified arithmetic objects.
     * <p>
     * Matrix components are expected row-wise, which means that
     * as the first index in <code>values</code>, the row i is used
     * and as the second index in <code>values</code>, the column j is used.
     * </p>
     * <p>
     * Note that the resulting vector may or may not be backed by the
     * specified array.
     * </p>
     * @param values the element values of the matrix to create.
     *  The matrix may be backed by this exact array per reference.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     */
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ valueOf(Arithmetic/*>R<*/[][] values) {
	return new ArithmeticMatrix/*<R>*/(values);
    } 
    public static Matrix valueOf(double[][] values) {
	return new RMatrix(values);
    } 
    public static Matrix/*<Integer>*/ valueOf(int[][] values) {
	for (int i = 1; i < values.length; i++)
	    Utility.pre(values[i].length == values[i - 1].length, "rectangular array required");
	// kind of map valueOf
	Matrix/*<Integer>*/ v = getInstance(values.length, values[0].length);
	for (int i = 0; i < values.length; i++)
	    for (int j = 0; j < values[0].length; j++)
		v.set(i, j, valueOf(values[i][j]));
	return v;
    } 

    /**
     * Creates a new instance of matrix with the specified dimension.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     */
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ getInstance(Dimension dim) {
	return new ArithmeticMatrix/*<R>*/(dim);
    } 
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ getInstance(int height, int width) {
	return new ArithmeticMatrix/*<R>*/(height, width);
    } 

    /**
     * Gets zero Matrix, with all elements set to 0.
     */
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ ZERO(Dimension dim) {
	return ZERO(dim.height, dim.width);
    } 
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ ZERO(int height, int width) {
	ArithmeticMatrix zero = (ArithmeticMatrix) getInstance(height, width);
	for (int i = 0; i < zero.dimension().height; i++)
	    for (int j = 0; j < zero.dimension().width; j++)
		zero.D[i][j] = Values.ZERO;
	return zero;
    }

    /**
     * Gets the identity Matrix, with all elements set to 0, except the leading diagonal m<sub>i,i</sub> set to 1.
     * @pre dim.width == dim.height
     */
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ IDENTITY(Dimension dim) {
	return IDENTITY(dim.height, dim.width);
    } 
    /**
     * Gets the identity Matrix, with all elements set to 0, except the leading diagonal m<sub>i,i</sub> set to 1.
     * @pre width == height
     * @see Functions#delta
     * @see #IDENTITY(Dimension)
     */
    public static /*<R implements Scalar>*/ Matrix/*<R>*/ IDENTITY(int height, int width) {
	if (!(width == height))
	    throw new IllegalArgumentException("identity matrix is square");
	ArithmeticMatrix/*<R>*/ identity = (ArithmeticMatrix/*<R>*/) (Matrix/*<R>*/) getInstance(height, width);
	for (int i = 0; i < identity.dimension().height; i++)
	    for (int j = 0; j < identity.dimension().width; j++)
		identity.D[i][j] = (Arithmetic/*>R<*/) Values.valueOf(orbital.math.functional.Functions.delta(i, j));
	return identity;
    } 

    /**
     * Gets diagonal Matrix, with all elements set to 0, except the leading diagonal m<sub>i,i</sub> set to v<sub>i</sub>.
     * @see Functions#delta
     */
    public static /*<R implements Scalar>*/ Matrix/*<R>*/ DIAGONAL(Vector/*<R>*/ diagon) {
	Matrix/*<R>*/ diagonal = getInstance(new Dimension(diagon.dimension(), diagon.dimension()));
	for (int i = 0; i < diagonal.dimension().height; i++)
	    for (int j = 0; j < diagonal.dimension().width; j++)
		diagonal.set(i, j, i == j ? diagon.get(i) : (Arithmetic/*>R<*/) Values.valueOf(0));
	return diagonal;
    } 

    /**
     * Returns an unmodifiable view of the specified matrix.
     * This method allows modules to provide users with "read-only" access to constant matrices.
     * <p>
     * Query operations on the returned matrix "read through" to the specified matrix,
     * and attempts to modify the returned matrix, whether direct or via its iterator,
     * result in an UnsupportedOperationException.</p>
     * <p>
     * Note that cloning a constant matrix will not return a constant matrix, but a clone of the
     * specified matrix m.</p>
     */
    public static /*<R implements Arithmetic>*/ Matrix/*<R>*/ constant(final Matrix/*<R>*/ m) {
	return /*refine/delegate Matrix*/ new AbstractMatrix/*<R>*/() {
		protected Matrix/*<R>*/ newInstance(Dimension d) {throw new AssertionError("this method should never get called in this context");}
		public Dimension dimension() { return m.dimension(); }
		public Arithmetic/*>R<*/ get(int i, int j) { return m.get(i,j); }
		public void set(int i, int j, Arithmetic/*>R<*/ v) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ getColumn(int j) { return m.getColumn(j); }
		public void setColumn(int j, Vector/*<R>*/ v) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ getRow(int i) { return m.getRow(i); }
		public void setRow(int i, Vector/*<R>*/ v) { throw new UnsupportedOperationException(); }
		public void set(Arithmetic/*>R<*/ v[][]) { throw new UnsupportedOperationException(); }
		public ListIterator getColumns() { return Setops.unmodifiableListIterator((ListIterator)m.getColumns()); }
		public ListIterator getRows() { return Setops.unmodifiableListIterator((ListIterator)m.getRows()); }
		public Iterator iterator() { return Setops.unmodifiableIterator(m.iterator()); }
		public Vector/*<R>*/ getDiagonal() { return m.getDiagonal(); }
		public boolean isSquare() { return m.isSquare(); }
		public boolean isSymmetric() throws ArithmeticException{ return m.isSymmetric(); }
		public boolean isRegular() throws ArithmeticException{ return m.isRegular(); }
		public int isDefinite() throws ArithmeticException{ return m.isDefinite(); }
		public boolean equals(Object b) { return m.equals(b); }
		public int hashCode() { return m.hashCode(); }
		public Object clone() { return m.clone(); }
		public Matrix/*<R>*/ subMatrix(int r1, int r2, int c1, int c2) { return m.subMatrix(r1,r2, c1,c2); }
		public Real norm() { return m.norm(); }
		public Real norm(double p) { return m.norm(p); }
		public Arithmetic/*>R<*/ trace() { return m.trace(); }
		public Arithmetic/*>R<*/ det() { return m.det(); }
		public Arithmetic add(Arithmetic b) {return m.add(b);}
		public Arithmetic minus() {return m.minus();}
		public Arithmetic subtract(Arithmetic b) {return m.subtract(b);}
		public Arithmetic multiply(Arithmetic b) {return m.multiply(b);}
		public Arithmetic scale(Arithmetic b) { return m.scale(b); }
		public Arithmetic inverse() {return m.inverse();}
		public Arithmetic divide(Arithmetic b) {return m.divide(b);}
		public Arithmetic power(Arithmetic b) {return m.power(b);}
		public Matrix/*<R>*/ transpose() { return m.transpose(); }
		public Matrix/*<R>*/ pseudoInverse() { return m.pseudoInverse(); }
		public Matrix/*<R>*/ appendColumns(Matrix/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Matrix/*<R>*/ appendRows(Matrix/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Matrix/*<R>*/ insertColumns(int i, Matrix/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Matrix/*<R>*/ insertRows(int i, Matrix/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Matrix/*<R>*/ removeColumn(int j) { throw new UnsupportedOperationException(); }
		public Matrix/*<R>*/ removeRow(int i) { throw new UnsupportedOperationException(); }
		public Arithmetic/*>R<*/[][] toArray() {return m.toArray(); }
		public String toString() { return m.toString(); }
	    };
    }

    // polynomial constructors and utilities

    /**
     * Returns a polynomial with the specified coefficients.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     * @param coefficients an array <var>a</var> containing the
     * coefficients of the polynomial.
     * @return the polynomial <var>a</var><sub>0</sub> + <var>a</var><sub>1</sub>X + <var>a</var><sub>2</sub>X<sup>2</sup> + ... + <var>a</var><sub>n</sub>X<sup>n</sup> for n=coefficients.length-1.
     * @see #asPolynomial(Vector)
     */
    public static /*<R implements Arithmetic>*/ Polynomial/*<R>*/ polynomial(Arithmetic/*>R<*/[] coefficients) {
    	return new AbstractPolynomial/*<R>*/(coefficients);
    }
    public static Polynomial polynomial(double[] coefficients) {
    	return new AbstractPolynomial(Values.valueOf(coefficients).toArray());
    }

    /**
     * Returns a vector view of a polynomial.
     * Interprets the coefficients of the polynomial as the components
     * of a vector.
     * @return the polynomial <var>a</var><sub>0</sub> + <var>a</var><sub>1</sub>X + <var>a</var><sub>2</sub>X<sup>2</sup> + ... + <var>a</var><sub>n</sub>X<sup>n</sup> for n=a.dimension()-1.
     * @see #polynomial(Arithmetic[])
     * @see #asVector(Polynomial)
     * @todo perhaps implement a true view flexible for changes
     */
    public static /*<R implements Arithmetic>*/ Polynomial/*<R>*/ asPolynomial(Vector a) {
    	return new AbstractPolynomial/*<R>*/((Arithmetic/*>R<*/[]) a.toArray());
    }

    /**
     * Returns a polynomial view of a vector.
     * Interprets the components of the vector as the coefficients
     * of a polynomial.
     * @return the vector (<var>a</var><sub>0</sub>,<var>a</var><sub>1</sub>,<var>a</var><sub>2</sub>,...,<var>a</var><sub>n</sub>) of the polynomial <var>a</var><sub>0</sub> + <var>a</var><sub>1</sub>X + <var>a</var><sub>2</sub>X<sup>2</sup> + ... + <var>a</var><sub>n</sub>X<sup>n</sup> for n=a.degree().
     * @see Polynomial#getCoefficients()
     * @see #asPolynomial(Vector)
     * @todo implement a true view flexible for changes (but only if Polynomial.set(...) has been introduced)
     */
    public static /*<R implements Arithmetic>*/ Vector asVector(Polynomial/*<R>*/ p) {
    	return Values.valueOf(p.getCoefficients());
    }

    /**
     * Returns an unmodifiable view of the specified polynomial.
     * This method allows modules to provide users with "read-only" access to constant polynomials.
     * <p>
     * Query operations on the returned polynomial "read through" to the specified polynomial,
     * and attempts to modify the returned polynomial, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     * <p>
     * Note that cloning a constant polynomial will not return a constant matrix, but a clone of the
     * specified polynomial.</p>
     */
    public static /*<R implements Arithmetic>*/ Polynomial/*<R>*/ constant(Polynomial/*<R>*/ p) {
	// Polynomials are currently unmodifiable anyhow.
	return p;
    }
    

    // quotient constructors

    /**
     * Returns a new quotient a&#772;=[a]&isin;M/mod
     * of the givne value reduced with the quotient operator.
     * <p>
     * Note that unlike {@link #quotient(Euclidean,Euclidean) quotients of euclidean rings}
     * and due to the black-box behaviour of the quotient operator
     * these quotients do not guarantee and implementation of {@link #inverse()}.
     * </p>
     * @param mod is the quotient operator applied (see {@link Quotient#getQuotientOperator()}).
     */
    public static /*<M implements Arithmetic>*/ Quotient/*<M>*/ quotient(Arithmetic/*>M<*/ a, Function/*<M,M>*/ mod) {
	return new AbstractQuotient(a, mod);
    }
    /**
     * Returns a new quotient a&#772;=[a]&isin;M/(m) of the given
     * value reduced modulo m.
     * <p> Will use special remainder classes
     * modulo m in euclidean rings.  These remainder classes are those
     * induced by the {@link Euclidean#modulo(Euclidean) euclidean
     * remainder} operator.  Quotients of euclidean rings have the big
     * advantage of supporting a simple calculation of multiplicative
     * inverses modulo m.
     * </p>
     */
    public static /*<M implements Euclidean>*/ Quotient/*<M>*/ quotient(Euclidean/*>M<*/ a, Euclidean/*>M<*/ m) {
	return new AbstractQuotient(a, m);
    }
    /**
     * Returns a new quotient a&#772;=[a]&isin;M/(m)
     * of the given value reduced modulo m.
     * <p>
     * <small>Being identical to {@link #quotient(Euclidean,Euclidean)},
     * this method only helps resolving the argument type ambiguity
     * for polynomials.</small>
     * </p>
     * @see #quotient(Euclidean,Euclidean)
     */
    public static /*<M implements Euclidean>*/ Quotient/*<M>*/ quotient(Euclidean/*>M<*/ a, Polynomial m) {
	return quotient(a, (Euclidean)m);
    }
    /**
     * (Convenience) Returns a new quotient a&#772;=[a]&isin;M/(m)
     * of the given value reduced modulo m.
     * <p>
     * This is only a convenience constructor for a special case of
     * M=<b>Z</b>, M/(m)=<b>Z</b>/m<b>Z</b>.
     * Although this case appears rather often, it is by far not the
     * only case of quotients.
     * </p>
     * @see <a href="{@docRoot}/DesignPatterns/Convenience.html">Convenience Method</a>
     * @see #quotient(Euclidean,Euclidean)
     */
    public static Quotient/*<Integer>*/ quotient(int a, int m) {
	return quotient(valueOf(a), valueOf(m));
    }

    // symbol constructors

    /**
     * Returns a new algebraic symbol.
     * @return the algebraic symbol "signifier".
     */
    public static Symbol symbol(String signifier) {
	return new AbstractSymbol(signifier);
    }

    // general static methods for scalar values

    /**
     * Returns an arithmetic object whose value is equal to that of the
     * representation in the specified string.
     * @param s the string to be parsed.
     * @return an instance of arithmetic that is equal to the representation in s.
     * @throws NumberFormatException if the string does not contain a parsable arithmetic object.
     * @see <a href="{@docRoot}/DesignPatterns/Facade.html">Facade (method)</a>
     */
    public static Arithmetic valueOf(String s) throws NumberFormatException {
	try {
	    return ArithmeticFormat.getDefaultInstance().parse(s);
	}
	catch(ClassCastException x) {throw new NumberFormatException("found " + x.getMessage());}
	catch(ParseException x) {throw new NumberFormatException(x.toString());}
    }

    // faster version for users that know they really want a non-narrowed real value, only
    /*public static Real valueOf(String s) {
      return new Double(java.lang.Double.valueOf(s));
      }
      public static Integer valueOf(String s) {
      return new Long(java.lang.Long.valueOf(s));
      }*/

    // conversion methods

    /**
     * Returns an unmodifiable vector view of the specified matrix.
     * This method allows modules to provide users with "read-only" access.
     * <p>
     * Query operations on the returned vector "read through" to the specified matrix,
     * and attempts to modify the returned vector, whether direct or via its iterator,
     * result in an UnsupportedOperationException.
     * </p>
     * <p>
     * The matrix is interpreted row-wise as a vector.
     * </p>
     */
    public static /*<R implements ListIterator,  Arithmetic>*/ Vector/*<R>*/ asVector(final Matrix/*<R>*/ m) {
	return /*refine/delegate Vector*/ new AbstractVector/*<R>*/() {
		protected Vector/*<R>*/ newInstance(int dim) {
		    return m instanceof RMatrix
			? (Vector) new RVector(dim)
			: (Vector) new ArithmeticVector/*<R>*/(dim);
		} 
		public int dimension() {
		    Dimension dim = m.dimension();
		    return dim.height * dim.width;
		}
		/**
		 * Return the corresponding row index of the given vector index.
		 */
		private int rowOf(int i) {
		    return i / m.dimension().width;
		}
		/**
		 * Return the corresponding column index of the given vector index.
		 */
		private int columnOf(int i) {
		    return i % m.dimension().width;
		}
		public Arithmetic/*>R<*/ get(int i) { return m.get(rowOf(i),columnOf(i)); }
		public void set(int i, Arithmetic/*>R<*/ v) { throw new UnsupportedOperationException(); }
		protected void set(Arithmetic/*>R<*/ v[]) { throw new UnsupportedOperationException(); }
		public Iterator iterator() { return new ListIterator() {
			private final Iterator i = m.iterator();
			// partly delegation to java.util.ListIterator interface

			public void add(Object param1) {
			    throw new UnsupportedOperationException();
			}
			public Object next() {
			    return i.next();
			}

			public boolean hasNext() {
			    return i.hasNext();
			}
			public void remove() {
			    throw new UnsupportedOperationException();
			}
			public void set(Object param1) {
			    throw new UnsupportedOperationException();
			}
			public int previousIndex() {
			    throw new UnsupportedOperationException();
			}
			public Object previous() {
			    throw new UnsupportedOperationException();
			}
			public int nextIndex() {
			    throw new UnsupportedOperationException();
			}

			public boolean hasPrevious() {
			    throw new UnsupportedOperationException();
			}

		    };
		}
		public Object clone() { throw new UnsupportedOperationException("@xxx dunno"); }
		public Vector/*<R>*/ insert(int i, Arithmetic/*>R<*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ insert(int i, Vector/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ append(Arithmetic/*>R<*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ append(Vector/*<R>*/ b) { throw new UnsupportedOperationException(); }
		public Vector/*<R>*/ remove(int i) { throw new UnsupportedOperationException(); }
	    };
    }


    /**
     * Returns a minimized Scalar whose value is equal to that of the specified scalar.
     * @return an instance of scalar that is most restrictive.
     * This means that an integer will be returned instead of a real whenever possible
     * and so on.
     * @post RES.equals(val)
     * @todo optimize by avoiding to create intermediate objects, f.ex. convert complex(2+i*0) -> real(2) -> rational(2) -> integer(2) also use OBDD
     */
    public static final Scalar narrow(Scalar val) {
	if (val instanceof Integer)
	    return val;
	if (Complex.hasType.apply(val)) {
	    Complex c = (Complex) val;
	    if (!c.im().equals(Values.ZERO))
		return val;
	    else
		val = c.re();
	}
	if (Real.isa.apply(val)) {
	    Real r = (Real) val;
	    try {
		if (MathUtilities.isInteger(r.doubleValue()))
		    return new AbstractInteger.Long((long) r.doubleValue());
	    } catch (UnsupportedOperationException nonconform_trial) {
		// ignore
	    } 
	    if (Rational.isa.apply(val))
		return (Rational) val;
	    else
		return val;
	} else
	    // some other unknown scalar thing
	    return val;
    } 

    /**
     * Get two minimized Arithmetic objects whose values equal the specified numbers,
     * while both values returned will have the same type.
     * @return an array with two elements,
     * the first being an Arithmetic object of the same value as a,
     * the second being an Arithmetic object of the same value as b,
     * that both have the same minimum (that is most restrictive) type.
     * This means that an integer will be returned instead of a real whenever possible,
     * a real instead of a complex and so on.
     * But it will always be true that both elements returned have exactly the same type.
     * @see MathUtilities#getEqualizer()
     * @post RES[0].getClass() == RES[1].getClass()
     */
    public static Scalar[] minimumEqualized(Number a, Number b) {
	//@xxx adapt better to new Complex>Real>Rational>Integer type hierarchy and conform to a new OBDD (ordered binary decision diagram)
	//@todo partial order with Arithmetic>Scalar>Complex>Real>Rational>Integer and greatest common super type of A,B being A&cup;B = sup {A,B}
	if (Complex.hasType.apply(a) || Complex.hasType.apply(b))
	    return new Complex[] {
		Complex.hasType.apply(a) ? (Complex) a : new AbstractComplex.ComplexImpl(a), Complex.hasType.apply(b) ? (Complex) b : new AbstractComplex.ComplexImpl(b)
	    };

	// this is a tricky binary decision diagram (optimized), see documentation
	if (Integer.hasType.apply(a)) {
	    if (Integer.hasType.apply(b))
		return new Integer[] {
		    new AbstractInteger.Long(a), new AbstractInteger.Long(b)
		};
	} else {	// a is no integer
	    if (!Rational.hasType.apply(a))
		return new Real[] {
		    new AbstractReal.Double(a), new AbstractReal.Double(b)
		};
	} 
        
	/* fall-through: all other cases come here */
	if (Rational.hasType.apply(b))
	    return new Rational[] {
		Rational.hasType.apply(a) ? (Rational) a : rational(a.intValue()), Rational.hasType.apply(b) ? (Rational) b : rational(b.intValue())
	    };
	//@xxx Rational + Integer != Real
	return new Real[] {
	    new AbstractReal.Double(a), new AbstractReal.Double(b)
	};
    } 

    // Constants

    /**
     * Initialize static constant array when class is loaded.
     * @invariant 0 < MAX_CONSTANT < Integer.MAX_VALUE
     * @xxx note that we should think about the order of static initialization.
     *  If Integer.ZERO uses Values.ZERO, then we must assure class Values is initialized first.
     */
    private static final int	 MAX_CONSTANT = 4;
    private static final Integer posConst[] = new Integer[MAX_CONSTANT + 1];
    private static final Integer negConst[] = new Integer[MAX_CONSTANT + 1];
    static {
	posConst[0] = negConst[0] = new AbstractInteger.Long(0);
	for (int i = 1; i <= MAX_CONSTANT; i++) {
	    posConst[i] = new AbstractInteger.Long(i);
	    negConst[i] = new AbstractInteger.Long(-i);
	} 
    } 

    /**
     * 0.
     */
    public static final Integer ZERO = posConst[0];

    /**
     * 1.
     */
    public static final Integer ONE = posConst[1];

    /**
     * +&infin;.
     * @see #INFINITY
     * @see #NEGATIVE_INFINITY
     */
    public static final Real POSITIVE_INFINITY = valueOf(java.lang.Double.POSITIVE_INFINITY);

    /**
     * -&infin;.
     * @see #INFINITY
     * @see #POSITIVE_INFINITY
     */
    public static final Real NEGATIVE_INFINITY = valueOf(java.lang.Double.NEGATIVE_INFINITY);

    /**
     * &pi;.
     */
    public static final Real PI = valueOf(Math.PI);
    /**
     * <b>e</b>.
     */
    public static final Real E = valueOf(Math.E);

    /**
     * not a number &perp;&infin;&isin;<b>R</b>&cup;{&perp;}.
     */
    public static final Real NaN = valueOf(java.lang.Double.NaN);

    /**
     * A constant for the imaginary unit <b>i</b>&isin;<b>C</b>.
     * @see #i
     */
    public static final Complex I = complex(0, 1);
    /**
     * A constant for the imaginary unit <b>i</b>&isin;<b>C</b>.
     * @see #I
     */
    public static final Complex i = I;

    /**
     * complex infinity &infin;&isin;<b>C</b>.
     * @see #INFINITY
     */
    public static final Complex INFINITY = complex(java.lang.Double.POSITIVE_INFINITY, java.lang.Double.NaN);
}
