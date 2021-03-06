/**
 * @(#)DynamicProgrammingProblem.java 1.0 2000/07/31 Andre Platzer
 * 
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.algorithm.template;

/**
 * Hook class for problems solved by DynamicProgramming.
 * @version $Id$
 * @author  Andr&eacute; Platzer
 * @see DynamicProgramming
 */
public interface DynamicProgrammingProblem/*<Solution,SolutionPart>*/ extends AlgorithmicProblem {

    // TODO: change Object[] to List?

    /**
     * Get the initial array of partial solutions.
     * @return (possibly a multi-dimensional) array of objects initialized ready to begin solving.
     */
    Object/*>SolutionPart<*/[] getInitialPartialSolutions();

    /**
     * Check whether the given partial solutions are a valid solution to the problem.
     * @preconditions partialSolutions resulted in repeated calls of solve after a single call to getInitialPartialSolutions.
     * @postconditions RES indicates whether we found a solution to the problem
     */
    boolean isSolution(Object/*>SolutionPart<*/[] partialSolutions);

    /**
     * Divide this problem into several (dependant) problem parts and return the index description for the next part to solve.
     * Solving will then continue with this part.
     * @return an array specifying the indices in the partialSolutions to solve next.
     * @postconditions RES.length is not lower than the number of dimensions for partialSolutions
     * && partialSolutions[RES[0]][RES[1]]...[RES[RES.length-1]] does not raise an exception.
     */
    int[] nextPart();

    /**
     * Solve the problem part using the partial solutions we already now.
     * @preconditions nextPart() returned part && partialSolutions is constructed by getInitialPartialSolution and solve
     * @return the solution for this case.
     * @see #nextPart()
     */
    Object/*>Solution<*/ solve(int[] part, Object/*>SolutionPart<*/[] partialSolutions);

    /**
     * Merge several partial solutions to a complete solution.
     * @param partialSolutions partial solutions
     * @return the complete solution consisting of the partial solutions.
     */
    Object/*>Solution<*/ merge(Object/*>SolutionPart<*/[] partialSolutions);
}
