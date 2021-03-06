/**
 * @(#)BestFirstSearch.java 1.0 2000/09/17 Andre Platzer
 *
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;

import java.util.Collection;
import orbital.logic.functor.Function;
import orbital.math.Real;
 
import java.util.List;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Comparator;

import java.util.Collections;
import orbital.util.Setops;

/**
 * BestFirstSearch class (BFS). An heuristic search algorithm.
 * <p>
 * Expands best nodes first, i.e. those that have min f(n).</p>
 * <p>
 * Implementation data structure is a Heap.</p>
 *
 * @version $Id$
 * @author  Andr&eacute; Platzer
 * @see Greedy
 */
public abstract class BestFirstSearch/*<A,S>*/ extends GeneralSearch/*<A,S>*/
    implements EvaluativeAlgorithm/*<GeneralSearchProblem<A,S>,S>*/ {
    private static final long serialVersionUID = -7753264910951203557L;

    protected Iterator createTraversal(GeneralSearchProblem/*<A,S>*/ problem) {
        return new OptionIterator/*<A,S>*/(problem, getEvaluation());
    }

    /**
     * An iterator over a state space in best-first order.
     * Expands nodes with better f-costs, first.
     * @invariants isSorted(nodes)
     * @version $Id$
     * @author  Andr&eacute; Platzer
     */
    public static class OptionIterator/*<A,S>*/ extends GeneralSearch.OptionIterator/*<A,S>*/ {
        private static final long serialVersionUID = 1955160705943645903L;
        /**
         * the sorted list of nodes.
         * @serial
         */
        // @todo return new Heap();
        // or return new TreeSet(new HeuristicSearch.EvaluationComparator(getEvaluation())) and optimize add(..)
        // or even return new PriorityQueue();

        // @todo use a data structure that caches the evaluation values and does not need to reevaluate them over and over again, just because we want to sort it.
        private List/*<S>*/ nodes;
        /**
         * the comparator used for sorting nodes.
         * @serial
         */
        private Comparator/*<S>*/ comparator;
        /**
         * @param evaluation the evaluation function to use for sorting the options monotonically.
         *  Usually {@link EvaluativeAlgorithm#getEvaluation()}.
         */
        public OptionIterator(GeneralSearchProblem/*<A,S>*/ problem, Function/*<S,Real>*/ evaluation) {
            super(problem);
            this.nodes = new LinkedList/*<S>*/();
            nodes.add(problem.getInitialState());
            this.comparator = new EvaluationComparator/*<S>*/(evaluation);
        }
        protected boolean isEmpty() {
            return nodes.isEmpty();
        }
        /**
         * Select the node with min f(n).
         * Due to the sorted list that is the first object.
         * @preconditions sorted(nodes)
         */
        protected Object/*>S<*/ select() {
            return nodes.remove(0);
        }
        /**
         * merge old and new lists.
         * @preconditions sorted(nodes)
         * @postconditions sorted(nodes)
         * @see orbital.util.Setops#merge(List, List, Comparator)
         */
        protected boolean add(Iterator/*<S>*/ newNodes) {
            if (!newNodes.hasNext())
                return false;
            List l = Setops.asList(newNodes);
            // sort newNodes because it most likely is not yet sorted
            Collections.sort(l, comparator);
            nodes = Setops.merge(nodes, l, comparator);
            return true;
        }
    };

    //  protected Collection createCollection() {
    //          // @todo return new Heap();
    //          // or return new TreeSet(new HeuristicSearch.EvaluationComparator(getEvaluation())) and optimize add(..)
    //          // or even return new PriorityQueue();
    //
    //          // @todo use a data structure that caches the evaluation values and does not need to reevaluate them over and over again, just because we want to sort it.
    //          return new LinkedList();
    //  }
    //
    //    /**
    //     * Select the node with min f(n).
    //     * Due to the sorted list this is the first object.
    //     * @preconditions sorted(nodes)
    //     */
    //    protected GeneralSearchProblem.Option select(Collection nodes) {
    //          Iterator i = nodes.iterator();
    //          GeneralSearchProblem.Option sel = (GeneralSearchProblem.Option) i.next();
    //          i.remove();
    //          return sel;
    //    }
    //
    //          /**
    //           * merge old and new lists.
    //     * @preconditions sorted(oldNodes)
    //     * @postconditions sorted(RES)
    //   * @see orbital.util.Setops#merge(List, List, Comparator)
    //   */
    //    protected Collection add(Collection newNodes, Collection oldNodes) {
    //          Comparator comparator = new EvaluationComparator(this);
    //          // sort newNodes because it most likely is not yet sorted
    //          Collections.sort((List) newNodes, comparator);
    //          return Setops.merge((List) oldNodes, (List) newNodes, comparator);
    //    }
}
