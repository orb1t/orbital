/**
 * @(#)LocalOptimizerSearch.java 1.1 2002/06/01 Andre Platzer
 *
 * Copyright (c) 2002 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;
import orbital.algorithm.template.GeneralSearchProblem.Option;

import java.util.Iterator;
import java.io.Serializable;

import java.util.List;
import java.util.Random;

import java.util.NoSuchElementException;
import orbital.util.Setops;

/**
 * General search scheme for local optimizing search.
 * <p>
 * Local optimizers will try state transitions and accept only improvements, or states that
 * promise to lead to improvements. In order to achieve that, most of these local optimizers
 * use some sort of randomization and a probability depending on the degree of improvement or
 * decrease.
 * In fact, most uses of local optimizers also depend more or less on a random initial state.
 * Then, some of them may profit from random-restart.
 * </p>
 * <p>
 * Subclasses usually use the {@link LocalOptimizerSearch.OptionIterator} provided
 * to implement the traversal policy. Then they only have to provide the abstract
 * methods to implement the search algorithm.</p>
 *
 * @version 1.1, 2002/06/01
 * @author  Andr&eacute; Platzer
 * @todo should we implement HeuristicAlgorithm and provide get/setHeuristic, just because most of our subclasses are?
 */
public abstract class LocalOptimizerSearch extends GeneralSearch implements ProbabilisticAlgorithm, EvaluativeAlgorithm {
    private static final long serialVersionUID = 465553782601369843L;
    /**
     * The random generator source.
     * @serial the random source is serialized to let the seed persist.
     */
    private Random random;

    public LocalOptimizerSearch() {
	this(new Random());
    }
    public LocalOptimizerSearch(Random random) {
	this.random = random;
    }

    public Random getRandom() {
	return random;
    }

    public void setRandom(Random randomGenerator) {
	this.random = randomGenerator;
    }

    /**
     * A slight modification of {@link GeneralSearch#search(Iterator)}.
     * This method will finally return the most optimal node found, regardless of whether
     * it is a solution, or not.
     * This behaviour is especially useful if the isSolution test is omitted by categorically
     * returning <span class="keyword">true</span> there.
     */
    protected Option search(Iterator/*<Option<S,A>>*/ nodes) {
	Option node = null;
	while (nodes.hasNext()) {
	    node = (Option) nodes.next();

	    if (getProblem().isSolution(node))
		return node;
    	}

    	// current choice instead of failing
    	return node;
    }

    /**
     * An iterator over a state space in "choosy" random order.
     * <p>
     * It will pick a random move, but only accept some transitions.
     * </p>
     * @version 1.1, 2002/06/01
     * @author  Andr&eacute; Platzer
     * @see TransitionPath
     * @internal optimized version of a TransitionPath sandwhiched with a TransitionModel and an action iterator.
     * @todo introduce template method  select(...) for selection strategy?
     * @todo would we benfit from extending GeneralSearchProblem.OptionIterator?
     */
    public static abstract class OptionIterator implements Iterator, Serializable {
	private static final long serialVersionUID = -658271440377589506L;
    	/**
    	 * The search problem to solve.
    	 * @serial
	 * @internal we do not use algorithm.getProblem() for performance and (perhaps) update reasons.
    	 */
    	private final GeneralSearchProblem/*<S,A>*/ problem;
	/**
	 * The algorithm using this (randomized) iterator.
	 * @serial
	 */
	private final LocalOptimizerSearch algorithm;
	/**
	 * The current state s&isin;S of this transition path.
	 * @serial
	 */
	private Option state;
	public OptionIterator(GeneralSearchProblem problem, LocalOptimizerSearch algorithm) {
	    this.problem = problem;
	    //@todo super(problem); extend GeneralSearch.OptionIterator and use its select()?
	    this.state = new GeneralSearchProblem.Option(getProblem().getInitialState());
	    this.algorithm = algorithm;
	}

    	/**
    	 * Get the current problem.
    	 * @pre true
    	 * @return the problem specified in the last call to solve.
    	 */
    	protected final GeneralSearchProblem/*<S,A>*/ getProblem() {
	    return problem;
    	}

	/**
	 * Get the algorithm using this (randomized) iterator.
	 */
	protected final LocalOptimizerSearch getAlgorithm() {
	    return algorithm;
	}

	/**
	 * Get the current state s&isin;S of this transition path.
	 * i.e. the last state returned by {@link #next()}, or the initial state if no transition has
	 * already occurred.
	 */
	protected final Option getState() {
	    return state;
	}

	public Object next() {
	    //@internal optimize: this has a horrible performance if constructing states is an expensive operation, and the technique of lazy state construction is not applied. @todo Could perhaps solve by transforming GSP into a TransitionModel with separated actions() and transitions() with the latter constructing the state.
	    List nodes = Setops.asList(problem.expand(state));
	    if (nodes.isEmpty())
		//@internal note that hasNext() will not consider this case, since it is considered as an error
		throw new NoSuchElementException("specification hurt: there are no transitions from " + state);

	    // randomly select @todo s.a. s.a.
	    Option sp =  (Option)
		nodes.get(algorithm.getRandom().nextInt(nodes.size()));

	    if (accept(state, sp)) {
		// accept the transition current->sp
		state = sp;
	    }

	    return state;
	}

	/**
	 * The predicate asked whether to accept a transition.
	 * <div>s&rarr;<sub>a</sub>s&#697; is accepted (and performed) iff accept(s,s&#697;)</div>
	 * @internal alternative would be a delegation to a BinaryPredicate accept.
	 */
	protected abstract boolean accept(Option state, Option sp);

	/**
	 * Decides whether to stop further transitions.
	 * The predicate asked whether to continue or stop further transition.
	 * <div>transitions are continued further iff cont(s)</div>
	 * where s is the current state.
	 * @internal alternative would be a delegation to a Predicate cont.
	 */
	public abstract boolean hasNext();

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    };
}