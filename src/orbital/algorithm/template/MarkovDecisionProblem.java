/**
 * @(#)MarkovDecisionProblem.java 1.0 2000/10/11 Andre Platzer
 *
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;

import java.util.Collection;
import java.util.Set;

/**
 * Hook class for MarkovDecisionProcess algorithm. Objects implementing this interface represent
 * a Markov Decision Process model.
 * <p>
 * A <dfn>Markov Decision Problem</dfn> (MDP) for a Markov Decision Process is a
 * mathematical model for making sense of some classes of problems.
 * An MDP is a special kind of sequential decision problem.
 * It is characterized by
 * <ul>
 *  <li>a state space S.</li>
 *  <li>a set of actions A.</li>
 *  <li>a set G&sube;S of goal states. More often this is implied by an accessible description of the goal states.</li>
 *  <li>actions A(s)&sube;A applicable in each state s&isin;S.</li>
 *  <li>
 *    state transition functions specifying the <dfn>transition model</dfn>
 *    (which forms a rewrite system, perhaps with additional probability information),
 *    as either
 *    <ul type="circle">
 *      <li>
 *        deterministic transition function t:S&times;A(s)&rarr;S,
 *        with t(s,a) being the next state reached when performing action a&isin;A(s) in state s&isin;S.
 *      </li>
 *      <li>
 *        non-deterministic transition function t:S&times;A(s)&rarr;&weierp;(S), 
 *        such that t(s,a) is the set of next states that could be reached when performing action a&isin;A(s) in state s&isin;S.
 *      </li>
 *      <li>
 *        non-deterministic transition relation T&sube;S&times;A(s)&times;S,
 *        such that &lang;s,a,s'&rang;&isin;T iff s' is a next state that could be reached when performing action a&isin;A(s) in state s&isin;S.
 *      </li>
 *      <li>
 *        stochastic transition probabilities P<sub>a</sub>:S&rarr;[0,1]; s'&#8614;P<sub>a</sub>(s'|s) := <b>P</b>(s'|s,a) := <b>P</b>(s<sub>t+1</sub>=s'|s<sub>t</sub>=s,a<sub>t</sub>=a)
 *        denoting the probability of reaching state s'&isin;S on taking the action a&isin;A(s) in state s&isin;S.
 *        The function P is written this way in order to remind it has a specific probability distribution.
 *        Another possibility would be to use a combined function
 *        P:S&times;A(s)&rarr;S&times;[0,1]; (s,a)&#8614;&lang;s',P<sub>a</sub>(s'|s)&rang; which is more
 *        closely related to implementation issues, but usually considered an inconvenient notation.
 *      </li>
 *    </ul>
 *    Which all fulfill the <dfn>Markov property</dfn> for states,
 *    <center>
 *        <span class="Formula"><b>P</b>(s<sub>t+1</sub>=s'|s<sub>t</sub>,a<sub>t</sub>,s<sub>t-1</sub>,a<sub>t-1</sub>,...,s<sub>0</sub>,a<sub>0</sub>) = <b>P</b>(s<sub>t+1</sub>=s'|s<sub>t</sub>,a<sub>t</sub>)</span>
 *    </center>
 *    i.e. the transition depends only on the current state s&isin;S and the action a&isin;A(s) taken,
 *    and is independent from the previous history.
 *  </li>
 *  <li>
 *    immediate action costs c(s,a)&gt;0 for taking the action a&isin;A(s) in the state s&isin;S.
 *    If the immediate costs are bounded random numbers depending on state and action, then
 *    c(s,a) denotes the <em>expected</em> immediate cost, instead.
 *    Also note that there is no strict requirement for c(s,a)&gt;0. Instead there are several
 *    criterions that ensure convergence even for c(s,a)&isin;[0,&infin;).
 *    However, they have even more restrictive constraints, like finite state sets (see Barto <span xml:lang="la">et al</span>, 1995).
 *    <span class="@todo">These state and action dependent costs ensure that the utility function is separable.</span>
 *  </li>
 * </ul>
 * Its solution is a policy &pi;:S&rarr;A(s) telling which action to take in each state.
 * A solution &pi; is optimal if it has minimum <em>expected</em> cost.
 * </p>
 * <hr />
 * <p>
 * A <dfn>Partially Observable Markov Decision problem</dfn> (POMDP) is one kind of a
 * Markov Decision Problem with incomplete information which are an extension to MDPs.
 * POMDPs do not rely on an accessible environment, but work on an inaccessible environment where
 * some percepts might not be enough to determine the state, and thus we have incomplete information
 * about the state.
 * Utilitarian ideas have been around in artificial intelligence for a long time. For ideas of a
 * non-observing agent see Lem 1971.
 * </p>
 * <p>
 * For POMDPs, the Markov property does not hold for percepts, but only for states.
 * In the absence of feedback, a POMDP is a deterministic search in belief state space.
 * In the presence of feedback, a POMDP is an MDP over belief space (Astrom, 1965).
 * However, a single belief state is a probability distribution over physical states, then.
 * In fact, solving a POMDP can be quite complex.
 * </p>
 *
 * @version 1.0, 2000/10/11
 * @author  Andr&eacute; Platzer
 * @see MarkovDecisionProcess
 * @see <a href="http://www.ldc.usb.ve/~hector">Hector Geffner. Modelling and Problem Solving</a>
 * @see "D. P. Bertsekas. Dynamic Programming: Deterministic and Stochastic Models. Prentice-Hall, Englewood Cliffs, NJ, 1989."
 * @see "S. Ross. Introduction to Stochastic Dynamic Programming. Academic Press, New York, 1983."
 * @see "A. Barto, S. Bradtke, and S. Singh. Learning to act using real-time dynamic programming. <i>Artificial Intelligence</i>, 72:81-138, 1995."
 * @see "Stanis&#322;aw Lem. Doktor Diagoras in: Sterntageb&uuml;cher, suhrkamp p491, 1978. (original edition 1971)"
 */
public
interface MarkovDecisionProblem extends AlgorithmicProblem {
    /**
     * Check whether the given state is a goal state (a valid solution to the problem).
     * @pre s&isin;S
     * @param state the state s&isin;S to check for being a goal state.
     * @return whether s&isin;G.
     */
	boolean isSolution(Object state);

	/**
	 * Get the next applicable actions.
     * @param state the state s&isin;S to expand for applicable actions.
     * @pre s&isin;S
	 * @return A(s), a list of alternative actions applicable in the state s&isin;S.
	 *  The order of the list is decisive because for actions with equal costs
	 *  the first will be selected.
	 * @post RES=A(s)
	 * @see GeneralSearchProblem#expand(GeneralSearchProblem.Option)
	 * @see GreedyProblem#nextCandidates(List)
	 */
	Collection nextActions(Object state);
	
	/**
	 * Get the next states that could be reached.
	 * <p>
	 * For efficiency reasons it is recommended that this method does return
	 * only those states s'&isin;S that can be reached (i.e. where <b>P</b>(s'|s,a) &gt; 0)
	 * if that is cheap to determine.
	 * Although this is not strictly required.</p>
     * @param state the state s&isin;S.
     * @param action the action a&isin;A(s) that must be applicable in state s&isin;S.
     * @pre s&isin;S &and; a&isin;A(s)
	 * @return a list of states t(s,a) that could be reached when performing
	 *  the action a in the state s.
	 *  Where
	 *  t:S&times;A(s)&rarr;&weierp;(S) is the non-deterministic transition function.
	 *  For probabilistic actions it is t(s,a) = {s'&isin;S &brvbar; <b>P</b>(s'|s,a) &gt; 0}.
	 * @pre action&isin;A(state)
	 */
	Set nextStates(Object state, Object action);
	
	/**
	 * Transition probability.
     * @param sp the state s'&isin;S of which we want to know the probability of reaching it.
     * @param state the state s&isin;S we are in.
     * @param action the action a&isin;A(s) to perform.
     * @pre s&isin;S &and; a&isin;A(s) &and; s'&isin;S
	 * @return <b>P</b>(s'|s,a) = P<sub>a</sub>(s'|s) = P<sub>a</sub>(s' &cap; s) / P<sub>a</sub>(s),
	 *  the probability of reaching state s' if performing the action a in state s.
	 * @post RES&isin;[0,1]
	 */
	double transitionProbability(Object sp, Object state, Object action);
	
	/**
	 * Get the cost of an option to take.
	 * @param state in which state s&isin;S an action is taken.
	 * @param action specifies which action a&isin;A(s) is taken.
     * @pre s&isin;S &and; a&isin;A(s)
	 * @return c(s,a), the cost of taking the action a in state s.
	 * @pre action&isin;A(state)
	 * @post c(s,a)>0 &or; c(s,a)&isin;[0,&infin;)
	 */
	double getCost(Object state, Object action);
}