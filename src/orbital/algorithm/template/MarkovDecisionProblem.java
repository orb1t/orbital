/**
 * @(#)MarkovDecisionProblem.java 1.0 2000/10/11 Andre Platzer
 *
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;

import java.util.Iterator;
import java.io.Serializable;
import orbital.math.*;

import orbital.util.Utility;

/**
 * Hook class for MarkovDecisionProcess algorithm. Objects implementing this interface represent
 * a Markov decision process model.
 * <p>
 * A <dfn>Markov decision problem</dfn> (MDP) for a Markov decision process is a
 * mathematical model for making sense of some classes of problems.
 * An MDP is a special kind of sequential decision problem.
 * It is a combinatorical optimization problem as long as the state space is discrete.
 * It is characterized by
 * <ul>
 *  <li>a {@link TransitionModel transition model} with a transition relation.
 *    Where the transition relation satisfies the <dfn id="MarkovProperty">Markov property</dfn>
 *    for states,
 *    <center>
 *        <span class="Formula"><b>P</b>(S<sub>t+1</sub>=s'|S<sub>t</sub>=s,A<sub>t</sub>=a,S<sub>t-1</sub>,A<sub>t-1</sub>,&#8230;,S<sub>0</sub>,A<sub>0</sub>) = <b>P</b>(S<sub>t+1</sub>=s'|S<sub>t</sub>=s,A<sub>t</sub>=a)</span>
 *    </center>
 *    i.e. the transition depends only on the current state s&isin;S and the action a&isin;A(s) taken,
 *    and is independent from the previous history.
 *    (<small>Note that for infinite state spaces, this is not a true restriction.</small>)
 *    This transition model defines a stochastic process called Markov process,
 *    or - in case of a finite state space - Markov chain.
 *  </li>
 *  <li>
 *    immediate {@link MarkovDecisionProblem.Transition#getCost() action costs}
 *    c(s,a)&gt;0 for taking the action a&isin;A(s) in the state s&isin;S.
 *    If the immediate costs are bounded random numbers depending on state and action, then
 *    c(s,a) denotes the <em>expected</em> immediate cost, instead.
 *    Also note that there is no strict requirement for c(s,a)&gt;0. Instead there are several
 *    criterions that ensure convergence even for c(s,a)&isin;[0,&infin;).
 *    However, they have even more restrictive constraints, like finite state sets (see Barto <span xml:lang="la">et al</span>, 1995).
 *    <!-- @todo-->These state and action dependent costs ensure that the utility function is separable.
 *  </li>
 *  <li>a set G&sube;S of {@link #isSolution(Object) goal states}. More often this is implied by an accessible description of the goal states.</li>
 * </ul>
 * Its solution is a policy &pi;:S&rarr;A(s) telling which action to take in each state.
 * A solution &pi; is optimal if it has minimum <em>expected</em> cost.
 * </p>
 * <hr />
 * <p>
 * A <dfn>Partially Observable Markov decision problem</dfn> (POMDP) is one kind of a
 * Markov decision problem with incomplete information which are an extension to MDPs.
 * POMDPs do not rely on an accessible environment, but work on an inaccessible environment where
 * some percepts might not be enough to determine the state, and thus we have incomplete information
 * about the state.
 * Utilitarian ideas have been around in artificial intelligence for a long time. For ideas of
 * a non-observing agent see (Lem 1971).
 * </p>
 * <p>
 * For POMDPs, the Markov property does not hold for percepts, but only for states.
 * In the absence of feedback, a POMDP is a deterministic search in belief state space.
 * In the presence of feedback, a POMDP is an MDP over belief space (Astrom, 1965).
 * However, a single belief state is a probability distribution over physical states, then.
 * In fact, solving a POMDP can be quite complex.
 * </p>
 *
 * @version $Id$
 * @author  Andr&eacute; Platzer
 * @param <A> the type of transition actions.
 * @param <S> the type of transition states.
 * @param <M> the class representing transitions.
 * @see MarkovDecisionProcess
 * @see <a href="http://www.ldc.usb.ve/~hector">Hector Geffner. Modelling and Problem Solving</a>
 * @see "D. P. Bertsekas. Dynamic Programming: Deterministic and Stochastic Models. Prentice-Hall, Englewood Cliffs, NJ, 1989."
 * @see "S. Ross. Introduction to Stochastic Dynamic Programming. Academic Press, New York, 1983."
 * @see "A. Barto, S. Bradtke, and S. Singh. Learning to act using real-time dynamic programming. <i>Artificial Intelligence</i>, 72:81-138, 1995."
 * @see "Stanis&#322;aw Lem. Doktor Diagoras in: Sterntageb&uuml;cher, suhrkamp p491, 1978. (original edition 1971)"
 */
public interface MarkovDecisionProblem/*<A,S,M extends MarkovDecisionProblem.Transition>*/ extends TransitionModel/*<A,S,M>*/, AlgorithmicProblem {
    /**
     * Check whether the given state is a goal state (a valid solution to the problem).
     * <p>
     * Optional variation: Search algorithms generally seek out for
     * one single solution. In order to find all solutions to a
     * problem, simply let this method store solutions and return
     * <span class="keyword">false</span> instead, until enough
     * solutions have occurred. However, expanding solution nodes
     * should result in an empty list at some time to ensure
     * termination, then.
     * </p>
     * @preconditions s&isin;S
     * @param state the state s&isin;S to check for being a goal state.
     * @return G(s), resp. whether s&isin;G.
     */
    boolean isSolution(Object/*>S<*/ state);

    /**
     * Represents a transition option during a Markov decision process.
     * <p>
     * A transition option is at least a pair &lang;p,c&rang;&isin;[0,1]&times;<b>R</b>
     * of a probability of reaching a state (in the corresponding context), and the cost
     * of the action taken to reach it.
     * </p>
     * @stereotype Structure
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    static interface Transition/*<A,S,M extends MarkovDecisionProblem.Transition>*/
        extends TransitionModel.Transition {
        /**
         * Get the cost of taking the action leading to this transition.
         * <p>
         * Note: Since c:S&times;A&rarr;<b>R</b> is a function,
         * the cost should only depend on the action a taken, and the state s in which
         * it was taken, not the actual outcome (which is unknown a priori).
         * Otherwise, subset construction would not be applicable, etc.
         * </p>
         * @return c(s,a) the cost of taking the action a&isin;A(s) that took us here
         *  from state s&isin;S.
         * @postconditions RES>0 &or; RES&isin;[0,&infin;)
         */
        Real getCost();
    }

    /**
     * Default implementation of transition options for Markov decision processes.
     * @stereotype Structure
     * @stereotype Implementation
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    public static class DefaultTransition/*<A,S,M extends MarkovDecisionProblem.Transition>*/
        implements Transition, Serializable {
        private static final long serialVersionUID = -5421585936741224969L;
        /**
         * the probability of reaching a state (in the corresponding context).
         * @serial
         */
        private Scalar probability;
        /**
         * the immediate action cost c=c(s,a) of the action performed to reach the state.
         * @serial
         */
        private Real cost;

        /**
         * Create a new option &lang;p,c&rang;.
         * @param probability the probability of reaching a state s&#697;.
         * @param cost the immediate cost of taking the action which took us to that state s&#697;.
         */
        public DefaultTransition(Scalar probability, Real cost) {
            this.probability = probability;
            this.cost = cost;
        }
        /**
         * @deprecated convenience constructor, prefer to use {@link Values#valueOf(double)}..
         */
        public DefaultTransition(double probability, double cost) {
            this(Values.getDefaultInstance().valueOf(probability),
                 Values.getDefaultInstance().valueOf(cost));
        }

        public int compareTo(Object/*>TransitionModel.Transition<*/ o) {
            return ((Comparable)getProbability()).compareTo(((Transition)o).getProbability());
        }
                
        public String toString() {
            return getClass().getName() + "[" + getProbability() + "," + getCost() + "]";
        }
                
        public Scalar getProbability() {
            return probability;
        }

        public Real getCost() {
            return cost;
        }
    }
}
