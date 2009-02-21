/**
 * @(#)SparsePolynomial.java 1.1 2002-09-01 Andre Platzer
 *
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.math;
import orbital.algorithm.Combinatorical;
import orbital.math.*;
import orbital.math.Integer;

import orbital.math.functional.Function;
import orbital.math.functional.Operations;
import orbital.util.KeyValuePair;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Iterator;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;

/**
 * Implementation of polynomials in R[S] with a sparse map of coefficients.
 * Arbitrary S.
 * @version $Id$
 * @author  Andr&eacute; Platzer
 */
class SparsePolynomial/*<R extends Arithmetic, S extends Arithmetic>*/
    extends AbstractPolynomial/*<R,S>*/ {
    private static final long serialVersionUID = -8833160240745985849L;

    /**
     * The exponent index 0&isin;S for the constant term of this polynomial.
     */
    private final Arithmetic/*>S<*/ CONSTANT_TERM;

    /**
     * The 0&isin;R of the coefficient domain.
     */
    private final Arithmetic/*>R<*/ COEFFICIENT_ZERO;

    /**
     * Maps indices in S to the corresponding coefficients &iota;(s)&isin;R.
     */
    private final Map/*<S,R>*/ coefficients;

    /**
     * tags a dirty degree, i.e., when the degree cache is possibly out of sync
     */
    private static final int DIRTY = java.lang.Integer.MIN_VALUE + 10; 
    /**
     * Caches the degree value.
     * @see #degree()
     */
    private transient int degree = DIRTY;
    public SparsePolynomial(Arithmetic zeroCoefficient, Arithmetic/*>S<*/ anIndexObject) {
        super(anIndexObject.valueFactory());
        //@internal assuming (S,+) here
        this.CONSTANT_TERM = anIndexObject.zero();
        assert zeroCoefficient.isZero() : "zero coefficient should be zero";
        this.COEFFICIENT_ZERO = zeroCoefficient;
        this.coefficients = new LinkedHashMap();
    }
    public SparsePolynomial(Map/*<S,R>*/ coefficients, ValueFactory valueFactory) {
        super(valueFactory);
        if (coefficients.isEmpty()) {
        	throw new IllegalArgumentException("Cannot infer coefficient and exponent domain from empty coefficients");
        }
        Map.Entry/*<S,R>*/ e = (Map.Entry)coefficients.entrySet().iterator().next();
        this.CONSTANT_TERM = ((Arithmetic/*>S<*/)e.getKey()).zero();
        this.COEFFICIENT_ZERO = ((Arithmetic/*>S<*/)e.getValue()).zero();
        this.coefficients = coefficients;
    }
    
    public SparsePolynomial(Tensor coeff) {
        super(coeff.valueFactory());
        int[] i0 = new int[coeff.rank()];
        Arrays.fill(i0, 0);
        this.CONSTANT_TERM = valueFactory().valueOf(i0);
        this.COEFFICIENT_ZERO = coeff.get(i0);
        this.coefficients = new LinkedHashMap();
        for (Iterator i = coeff.entries(); i.hasNext(); ) {
        	KeyValuePair e = (KeyValuePair) i.next();
        	Arithmetic vi = (Arithmetic) e.getValue();
        	if (!vi.isZero()) {
        		this.coefficients.put(e.getKey(), vi);
        	}
        }
    }

    // factory-methods
    
    protected final Arithmetic/*>Polynomial<R,S><*/ newInstance(Object/*>S<*/ productIndexSet) {
        return new SparsePolynomial(COEFFICIENT_ZERO, (Arithmetic)productIndexSet);
    }

    // iterator-views
    
    /**
     * The number of times this object has been structurally modified.
     * Structural modifications are those that change the number of elements in
     * the object or otherwise modify its internal structure.
     * This field is used to make iterators of the object fail-fast.
     * <p>
     * To use this feature, increase modCount whenever an implementation method changes
     * this tensor.</p>
     * @see java.util.ConcurrentModificationException
     */
    protected transient int modCount = 0;

    public Iterator indices() {
        return coefficients.keySet().iterator();
    } 

    public ListIterator iterator() {
        return new ListIterator() {
                private final Iterator cursor = coefficients.entrySet().iterator();
                private Entry lastRet = null;
                /**
                 * The modCount value that the iterator believes that the backing
                 * object should have. If this expectation is violated, the iterator
                 * has detected concurrent modification.
                 */
                private transient int expectedModCount = modCount;
                public boolean hasNext() {
                    return cursor.hasNext();
                } 
                public Object next() {
                    try {
                        Object v = (lastRet = (Entry) cursor.next()).getValue();
                        checkForComodification();
                        return v;
                    }
                    catch(IndexOutOfBoundsException e) {
                        checkForComodification();
                        throw (AssertionError) new AssertionError("cursor should already have thrown a NoSuchElementException").initCause(e);
                    }
                } 
                public boolean hasPrevious() {
                    throw new UnsupportedOperationException("not currently supported");
                } 
                public Object previous() {
                    throw new UnsupportedOperationException("not currently supported");
                } 

                public void set(Object o) {
                    if (!(o instanceof Arithmetic))
                        throw new IllegalArgumentException();
                    if (lastRet == null)
                        throw new IllegalStateException();
                    checkForComodification();
        
                    try {
                    	final int oldDegree = degree;
                    	final Arithmetic ci = (Arithmetic)o;
                    	final int newPotentialDegree = ((Integer)Operations.sum.apply(getExponentVector(lastRet.getKey()))).intValue();
                    	if (!ci.isZero()) {
                    		lastRet.setValue(ci);
                    		if (oldDegree < newPotentialDegree) {
                    			// update degree if index is higher than degree and nonzero (because it might raise)
                    			// or equal and we reset to zero (because it might drop)
                    			//@todo delta-degrees can be optimized faster by exploiting that we know the old degree where to start 
                    			SparsePolynomial.this.degree = DIRTY;//degreeImpl(coefficients);
                    		}
                    	} else {
                    		// auto-cleanup zero coefficients for sparse representation
                    		cursor.remove();
                    		if (oldDegree == newPotentialDegree) {
                    			// update degree if index is higher than degree and nonzero (because it might raise)
                    			// or equal and we reset to zero (because it might drop)
                    			//@todo delta-degrees can be optimized faster by exploiting that we know the old degree where to start 
                    			SparsePolynomial.this.degree = DIRTY;//degreeImpl(coefficients);
                    		}
                    	}
                        expectedModCount = modCount;
                    } catch(IndexOutOfBoundsException e) {
                        throw new ConcurrentModificationException();
                    }
                }

                // UnsupportedOperationException, categorically

                public int nextIndex() {
                    throw new UnsupportedOperationException("a polynomial does not have a one-dimensional index");
                }
                public int previousIndex() {
                    throw new UnsupportedOperationException("a polynomial does not have a one-dimensional index");
                }

                public void add(Object o) {
                    throw new UnsupportedOperationException("adding a single element from a polynomial is impossible");
                } 
                public void remove() {
                    throw new UnsupportedOperationException("removing a single element from a polynomial is impossible");
                } 

                private final void checkForComodification() {
                    if (modCount != expectedModCount)
                        throw new ConcurrentModificationException();
                }
            };
    }
    
    public Iterator monomials() {
    	return new Iterator() {
            private final Iterator cursor = coefficients.entrySet().iterator();
			public boolean hasNext() {
				return cursor.hasNext();
			}

			public Object next() {
				Map.Entry e = (Map.Entry) cursor.next();
				return new KeyValuePair(e.getKey(), e.getValue());
			}

			public void remove() {
				cursor.remove();
			}
    		
    	};
    }

    public Object indexSet() {
        return CONSTANT_TERM;
    }

    public int rank() {
    	if (CONSTANT_TERM instanceof Vector) {
    		return ((Vector)CONSTANT_TERM).dimension();
    	} else if (CONSTANT_TERM instanceof Integer) {
    		return 1;
    	} else {
            throw new UnsupportedOperationException("no rank on " + this + "[" + CONSTANT_TERM.getClass().getName() + "]");
    	}
    }

    public final int degreeValue() {
        if (degree == DIRTY) {
                this.degree = degreeImpl();
        }
        return degree;
    }
    /**
     * Implementation calculating the degree of a polynomial,
     * given its coefficients.
     * @internal optimizable by far, start with big indices, not with 0,...,0
     */
    private int degreeImpl() {
        int d = java.lang.Integer.MIN_VALUE;
        for (Iterator/*<Map.Entry<S, R>>*/ i = coefficients.entrySet().iterator(); i.hasNext(); ) {
        	Map.Entry/*<S, R>*/ e = (Map.Entry)i.next();
        	Arithmetic xi = (Arithmetic)e.getKey();
        	Vector v = getExponentVector(xi);
            final Arithmetic vi = (Arithmetic)e.getValue();
            if (vi != null && !vi.isZero()) {
                final int sum = ((Integer)Operations.sum.apply(v)).intValue();
                if (sum > d)
                    d = sum;
            }
        }
        //throw new UnsupportedOperationException("no degree on " + get(CONSTANT_TERM).getClass().getName() + "[" + CONSTANT_TERM.getClass().getName() + "]");
        return d;
    }

    public int[] degrees() {
        final int degrees[] = new int[rank()];
        Arrays.fill(degrees, -1);
        for (Iterator/*<Map.Entry<S, R>>*/ j = coefficients.entrySet().iterator(); j.hasNext(); ) {
        	Map.Entry/*<S, R>*/ e = (Map.Entry)j.next();
        	Arithmetic xi = (Arithmetic)e.getKey();
        	Vector v = getExponentVector(xi);
            final Arithmetic vi = (Arithmetic)e.getValue();
            if (vi != null && !vi.isZero()) {
                // degrees = max(degrees, index)
                for (int i = 0; i < degrees.length; i++) {
                	int expo = ((Integer)v.get(i)).intValue();
                    if (expo > degrees[i]) {
                        degrees[i] = expo;
                    }
                }
            }
        }
        return degrees;
//      throw new UnsupportedOperationException("no partial degrees on " + get(CONSTANT_TERM).getClass().getName() + "[" + CONSTANT_TERM.getClass().getName() + "]");
    }

    public Arithmetic get(Arithmetic i) {
        Object ci = coefficients.get(i);
        if (ci == null)
        	return COEFFICIENT_ZERO;
        else
            return (Arithmetic/*__*/) ci;
    }
        
    public void set(Arithmetic i, Arithmetic ci) {
        final int oldDegree = degree;
        final int newPotentialDegree = ((Integer)Operations.sum.apply(getExponentVector(i))).intValue();
    	if (!ci.isZero()) {
            coefficients.put(i, ci);
    		if (oldDegree < newPotentialDegree) {
    			// update degree if index is higher than degree and nonzero (because it might raise)
    			// or equal and we reset to zero (because it might drop)
    			//@todo delta-degrees can be optimized faster by exploiting that we know the old degree where to start 
    			this.degree = DIRTY;//degreeImpl(coefficients);
    		}
    	} else {
    		// auto-cleanup zero coefficients for sparse representation
    		coefficients.remove(i);
    		if (oldDegree == newPotentialDegree) {
    			// update degree if index is higher than degree and nonzero (because it might raise)
    			// or equal and we reset to zero (because it might drop)
    			//@todo delta-degrees can be optimized faster by exploiting that we know the old degree where to start 
    			this.degree = DIRTY;//degreeImpl(coefficients);
    		}
    	}
    }

    public Arithmetic zero() {
        SparsePolynomial r = (SparsePolynomial)newInstance(CONSTANT_TERM);
        r.set(CONSTANT_TERM, COEFFICIENT_ZERO);
        return r;
    }

    public Arithmetic one() {
        SparsePolynomial r = (SparsePolynomial)newInstance(CONSTANT_TERM);
        r.set(CONSTANT_TERM, COEFFICIENT_ZERO.one());
        return r;
    }

    /**
     * Get a vectorial representation of an index for an exponent of a monomial, if possible
     * @param m
     * @return
     * @see orbital.math.AlgebraicAlgorithms#getExponentVector(Object)
     */
    private static Vector/*<Integer>*/ getExponentVector(Object m) {
    	if (m instanceof Vector) {
    		return (Vector)m;
    	} else if (m instanceof Integer) {
    		// univariate case
    		return ((Arithmetic)m).valueFactory().valueOf(new Integer[] {(Integer)m});
    	} else {
    		throw new ClassCastException("Cannot convert exponent representation into Vector<Integer> from " + m);
    	}
    }
}
