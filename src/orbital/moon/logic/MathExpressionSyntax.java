/**
 * @(#)MathExpressionSyntax.java 1.0 2002-10-17 Andre Platzer
 *
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of Andre Platzer. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into.
 */

package orbital.moon.logic;

import orbital.math.*;
import orbital.math.functional.*;
import orbital.logic.imp.*;
import orbital.logic.imp.Symbol;
import orbital.logic.imp.ParseException;
import orbital.logic.functor.Functor;
import orbital.logic.functor.Notation;

import orbital.logic.trs.Substitutions;
import java.lang.reflect.Field;
import orbital.io.IOUtilities;
import java.io.StringReader;
import java.util.*;
import java.beans.IntrospectionException;
import orbital.util.InnerCheckedException;

/**
 * This class implements an expression syntax for mathematical expressions.
 * Those expressions currently get parsed as a composition of functions.
 * An alternative implementation would already evaluate the expression instead of explicitly
 * constructing a representation as a composition of functions.
 * @version 1.1, 2002-10-17
 * @author  Andr&eacute; Platzer
 * @structure depends orbital.logic.functor.Notation Uses registered notation functors reverse.
 */
public class MathExpressionSyntax implements ExpressionSyntax {
    private static final Values valueFactory = Values.getDefaultInstance();
    static final Symbol LAMBDA = ClassicalLogic.LAMBDA;
	
    /**
     * tool-main
     */
    public static void main(String arg[]) throws Exception {
	if (orbital.signe.isHelpRequest(arg)) {
	    System.out.println(usage);
	    return;
	} 
	System.out.print("Type expression: ");
	System.out.flush();
	String expr = IOUtilities.readLine(System.in);
	Object p = new MathExpressionSyntax().createMathExpression(expr);
	System.out.println("Original expression:\t" + expr);
	System.out.println("Parsed function:\t" + p);
	System.out.println("Evaluates to:\t" + (p instanceof Function ? ((Function) p).apply(null) : p));	//@XXX: erm why null?
    } 
    public static final String usage = "usage: \n\tstart and enter a correct mathematical expression to evaluate\n\tremember that function evaluation expressions are denoted with rectangular\n\tbrackets as in \"sin[2 + 4/15] + 3*x\"";


    /**
     * {@inheritDoc}.
     * @see #createMathExpression(String)
     */
    public Expression createExpression(String expression) throws ParseException {
	if (expression == null)
	    throw new NullPointerException("null is not an expression");
	try {
	    LogicParser parser = new LogicParser(new StringReader(expression));
	    parser.setSyntax(this);
	    return parser.parseTerm();
	} catch (orbital.moon.logic.ParseException ex) {
	    throw new ParseException(ex.getMessage(), ex.currentToken.next.beginLine, ex.currentToken.next.beginColumn, ex);
	} 
    }
    /**
     * Parses an expression and queries its arithmetic object.
     * @see #createExpression(String)
     */
    public Arithmetic createMathExpression(String expression) throws ParseException {
	return (Arithmetic) getValueOf(createExpression(expression));
    }

    public Signature coreSignature() {
	return _coreSignature;
    }
    private static final Interpretation _coreInterpretation =
	arrayToInterpretation(new Object[] {
	    Operations.plus,
	    Operations.minus,
	    Operations.subtract,
	    Operations.times,
	    Operations.inverse,
	    Operations.divide,
	    Operations.power
	}, false);
    private static final Signature _coreSignature = _coreInterpretation.getSignature();
    
    private static final Interpretation arrayToInterpretation(Object[] functors, boolean skipNull) {
	Map assoc = new TreeMap();
	for (int i = 0; i < functors.length; i++) {
	    final Functor f = (Functor)functors[i];
	    if (f == null)
		if (skipNull)
		    continue;
		else
		    throw new NullPointerException("illegal functor " + f);
	    try {
		assoc.put(new SymbolBase(f.toString(),
					 Types.declaredTypeOf(f),
					 Notation.getNotation(f)),
			  f);
	    }
	    catch (IntrospectionException ex) {throw new InnerCheckedException("could not detect specification", ex);}
	}
	Signature signature = new SignatureBase(assoc.keySet());
	return new InterpretationBase(signature, assoc);
    }
    public Signature scanSignature(String expression) throws ParseException {
	throw new UnsupportedOperationException("not yet implemented");
    }

    public Expression createAtomic(Symbol symbol) {
	if (symbol == null)
	    throw new NullPointerException("illegal symbol: " + symbol);
	final Type cod = symbol.getType().codomain();
	final Type doc = symbol.getType().domain();
	assert cod != Types.ABSURD : "@xxx currently assume map as strict";
	final String signifier = symbol.getSignifier();
	assert signifier != null;

	// handle special cases of term construction, first
	if (LAMBDA.equals(symbol))
	    return new MathExpression(LAMBDA, LAMBDA.getType());

	// check if it's already predefined in the coreSignature() but perhaps in a different notation and a more general type than parsed
	for (Iterator i = coreSignature().iterator(); i.hasNext(); ) {
	    // almost identical to @see ModernLogic#createAtomic
	    Object o = i.next();
	    assert o instanceof Symbol : "signature isa Set<" + Symbol.class.getName() + '>';
	    Symbol s = (Symbol) o;
	    if (s.getSignifier().equals(signifier)) {
		// check for compatible types so as to detect misunderstandings during parse
		if (!symbol.getType().subtypeOf(s.getType()))
		    continue;
		
		// fixed interpretation of core signature
		final Object ref = _coreInterpretation.get(s);
		return new MathExpression(ref, s.getType());
	    }
	}

	if (cod.equals(Types.NOTYPE))
	    if (doc.equals(Types.INDIVIDUAL))
		return new MathExpression(valueFactory.symbol(symbol.getSignifier()), symbol.getType());
	    else if (doc.subtypeOf(Types.type(Arithmetic.class)))
		return new MathExpression(valueFactory.valueOf(symbol.getSignifier()), symbol.getType());
	    else
		throw new IllegalArgumentException("strange type " + symbol.getType() + " of " + symbol);
	else {
	    return new MathExpression(findFunction(symbol.getSignifier()), symbol.getType());
	}
    }
    
    public Expression compose(Expression compositor, Expression arguments[]) throws ParseException {
	MathExpression op = (MathExpression)compositor;
	if (!Types.isApplicableTo(compositor.getType(), arguments))
	    throw new ParseException("compositor " + compositor + ":" + compositor.getType() + " not applicable to the " + arguments.length + " arguments " + MathUtilities.format(arguments) + ":" + Types.typeOf(arguments), ModernLogic.COMPLEX_ERROR_OFFSET);

	// handle special cases of term construction, first
	if (LAMBDA.equals(op.referee)) {
	    assert arguments.length == 2;
	    orbital.math.Symbol x = (orbital.math.Symbol) ((MathExpression)arguments[0]).referee;
	    Object t = getValueOf(arguments[1]);
	    return new MathExpression(convert((orbital.logic.functor.Function) Substitutions.lambda.apply(x, t)),
				      Types.map(arguments[0].getType(), arguments[1].getType()));
	}

	MathFunctor f = (MathFunctor) op.getValue();
	Object[] arg = new Object[arguments.length];
	for (int i = 0; i < arg.length; i++)
	    arg[i] = getValueOf(arguments[i]);
	return new MathExpression(apply(f, arg), op.getType().domain());
    }

    static final Object getValueOf(Expression x) {
	return ((MathExpression)x).getValue();
    }

    /**
     * Find the function for a function call.
     * <p>
     * Will lookup known functions in {@link orbital.math.functional.Functions}.
     * </p>
     */
    private static MathFunctor findFunction(String fname) throws IllegalArgumentException {
	final String f = fname;
	try {
	    final Field referee = Functions.class.getField(f);
	    return (MathFunctor) referee.get(null);
	} catch(NoSuchFieldException notdefined) {
	    throw new IllegalArgumentException("Function '" + f + "' not defined");
	} catch(IllegalAccessException ex) {
	    throw (InternalError) new InternalError(ex.getMessage()).initCause(ex);
	} catch(SecurityException ex) {
	    throw (InternalError) new InternalError(ex.getMessage()).initCause(ex);
	}
    }

    /**
     * function application.
     * <p>
     * Will compose the function with its arguments in order to represent the function
     * application parsed now.
     * </p>
     * @note a functor is returned since the function application is not evaluated, but
     *  only <em>parsed</em>.
     *  This method results in a composition of function and arguments instead of
     *  the value of the function at the specified arguments.
     * @todo perhaps weaken to Functor for lambda expressions?
     */
    private static MathFunctor apply(MathFunctor f, Object[] arg) throws IllegalArgumentException {
	switch(arg.length) {
	case 1:
	    if (f instanceof Function)
		return Functionals.genericCompose((Function) f, arg[0]);
	    else
		throw new IllegalStateException("Function '" + f + "/" + arg.length + "' not defined as unary");
	case 2: {
	    if (f instanceof BinaryFunction)
		return Functionals.genericCompose((BinaryFunction) f, arg[0], arg[1]);
	    else
		throw new IllegalStateException("Function '" + f + "/" + arg.length + "' not defined as binary");
	}
	default:
	    throw new IllegalArgumentException("Calling a function '" + f  + "/" + arg.length + "' is not supported. Only 1 or two arguments can be used, yet");
	}
    }

    /**
     * convert a orbital.logic.functor.Function to a orbital.math.functional.Function 
     * that does not support other operations than apply.
     */
    private static final Function convert(final orbital.logic.functor.Function f) {
	return new AbstractFunction() {
		public Object apply(Object o) {
		    return f.apply(o);
		}
		public Function derive() {
		    throw new UnsupportedOperationException("not yet implemented, symbolic");
		} 
		public Function integrate() {
		    throw new UnsupportedOperationException("not yet implemented, symbolic");
		} 
		public Real norm() {
		    throw new UnsupportedOperationException();
		}
		public boolean equals(Object o) {
		    return f.equals(o);
		}
		public int hashCode() {
		    return f.hashCode();
		}
		public String toString() {
		    return f.toString();
		} 
	    };
    }

    // identical to @see orbital.math.functional.Function (below)
    /*private static*/ static abstract class AbstractFunction/*<A implements Arithmetic, B implements Arithmetic>*/  extends orbital.moon.math.AbstractFunctor implements Function/*<A,B>*/ {}


    private static class MathExpression implements Expression {
	private Object referee;
	private Type type;
	public MathExpression(Object referee, Type type) {
	    this.referee = referee;
	    this.type = type;
	}
	
	public Object getValue() {
	    return referee;
	}

	public Type getType() {
	    return type;
	}

	public Signature getSignature() {
	    throw new UnsupportedOperationException("not yet implemented");
	}

	public String toString() {
	    return referee.toString();
	}
    }
}
