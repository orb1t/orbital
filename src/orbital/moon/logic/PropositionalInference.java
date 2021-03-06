/**
 * @(#)PropositionalInference.java 1.1 2002-11-29 Andre Platzer
 *
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.logic;
import orbital.moon.logic.ClassicalLogic.Utilities;
import orbital.logic.imp.*;

import java.util.*;
import orbital.util.Setops;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Specialized propositional logic inference.
 * Implemented by Davis-Putnam-Loveland algorithm.
 *
 * @author Andr&eacute; Platzer
 * @version $Id$
 */
class PropositionalInference implements Inference {
    /**
     * Whether or not to use simplified clausal forms.
     */
    private static final boolean SIMPLIFYING = false;
    private static final Logger logger = Logger.getLogger(PropositionalInference.class.getPackage().getName());

    public PropositionalInference() {
        
    }
    public boolean infer(Formula[] B, Formula D) {
        // convert B to clausalForm
        Set/*<Set<Formula>>*/ S = new LinkedHashSet();
        for (Iterator i = Arrays.asList(B).iterator(); i.hasNext(); ) {
            Formula Bi = (Formula) i.next();
            Utilities.propositionalOnly(Bi.getSignature());
            S.addAll(Utilities.clausalForm(Bi, SIMPLIFYING));
        }

        //@todo could we remove tautologies?

        logger.log(Level.FINE, "W = {0}", S);
        if (logger.isLoggable(Level.FINEST))
            for (int i = 0; i < B.length; i++)
                logger.log(Level.FINEST, "W thus contains transformation of original formula {0}", Utilities.conjunctiveForm(B[i], SIMPLIFYING));

        // negate query since we are a negative test calculus
        Formula query = D.not();
        Utilities.propositionalOnly(query.getSignature());

        // convert (negated) query to clausalForm S
        Set queryClauses = Utilities.clausalForm(query, SIMPLIFYING);

        //@todo could we remove tautologies from query?
        logger.log(Level.FINE, "negated goal = {0} = {1}", new Object[] {query, queryClauses});

        //@internal if we don't copy the set, then formatting would need copies of the (mutable) sets.
        Set Sp = Setops.union(S, queryClauses);
        if (Sp.contains(Utilities.CONTRADICTION))
            // "premises are inconsistent since they already contain a contradiction, so ex falso quodlibet"
            return true;
        return refute(Sp);
    }
    public boolean isSound() {
        return true;
    } 
    public boolean isComplete() {
        //@internal for propositional logic
        return true;
    }

    /**
     * @param S the set of clauses to refute. may be changed by this method.
     * @return <code>false</code> if S is satisfiable,
     *  <code>true</code> if S is unsatisfiable (due to refutation).
     */
    private boolean refute(Set/*<Set<Formula>>*/ S) {
        logger.log(Level.FINE, "S = {0}", S);
        if (S.isEmpty()) {
            logger.log(Level.FINE, "satisfiable S = {0}", S);
            return false;
        }
        // search for any unit clause
        for (Iterator i = S.iterator(); i.hasNext(); ) {
            Set/*<Formula>*/ C = (Set)i.next();
            assert C.size() > 0 : "contradictions will have been detected earlier";
            if (C.size() != 1)
                continue;
            Formula literalC = (Formula) C.iterator().next();
            S = reduce(literalC, S);
            if (S.contains(ClassicalLogic.Utilities.CONTRADICTION)) {
                logger.log(Level.FINE, "unsatisfiable S = {0}", S);
                return true;
            }
            //@internal restarting loop would suffice @todo optimize by i = S.iterator();
            return refute(S);
        }

        // no unit clause, so choose an atom P
        //@internal we better choose a literal literalP and rely on duplex negation est affirmatio
        Formula literalP = (Formula) ((Set)S.iterator().next()).iterator().next();
        logger.log(Level.FINER, "choose unit clause {0}", literalP);
        return refute(Setops.union(S, Collections.singleton(Collections.singleton(literalP))))
            && refute(Setops.union(S, Collections.singleton(Collections.singleton(ClassicalLogic.Utilities.negation(literalP)))));
    }

    /**
     * @param S will not be changed
     */
    private Set/*<Set<Formula>>*/ reduce(final Formula C, Set/*<Set<Formula>>*/ S) {
        logger.log(Level.FINER, "reduce({0}, {1})", new Object[] {C, S});
        Set/*<Set<Formula>>*/ S2 = new LinkedHashSet(S.size());
        final Formula notC = ClassicalLogic.Utilities.negation(C);
        for (Iterator i = S.iterator(); i.hasNext(); ) {
            Set/*<Formula>*/ clause = (Set)i.next();
            if (clause.contains(C)) {
                logger.log(Level.FINEST, "remove clause {0}", clause);
            } else if (clause.contains(notC)) {
                Set/*<Formula>*/ clause2 = new LinkedHashSet(clause);
                clause2.remove(notC);
                S2.add(clause2);
                logger.log(Level.FINEST, "remove literal {0} retaining clause {1}", new Object[] {notC, clause2});
            } else {
                S2.add(clause);
                logger.log(Level.FINEST, "leave clause {0}", clause);
            }
        }
        logger.log(Level.FINER, "reduce({0}, {1}) = {2}", new Object[] {C, S, S2});
        return S2;
    }

    //@todo remove
    /*private Formula atomInLiteral(Formula literal) {
        // used duplex negatio est affirmatio (optimizable)
        if ((literal instanceof Composite)) {
            Composite f = (Composite) literal;
            Object    g = f.getCompositor();
            if (g == ClassicalLogic.LogicFunctions.not)
                // use duplex negatio est affirmatio to avoid double negations
                return (Formula) f.getComponent();
        }
        return literal;
        }*/
}// PropositionalInference
