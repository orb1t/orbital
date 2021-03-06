/**
 * @(#)IterationLimitException.java 0.9 2000/06/17 Andre Platzer
 * 
 * Copyright (c) 2000 Andre Platzer. All Rights Reserved.
 */

package orbital.logic;

/**
 * Thrown when the limit for the maximum number of iterations is overrun.
 * 
 * @version $Id$
 * @author  Andr&eacute; Platzer
 */
public class IterationLimitException extends LimitException {
    private static final long serialVersionUID = -2155726108226650819L;

    /**
     * The limit for the maximum number of iterations for mathematical operations
     * such as fixed point iterations.
     */
    public static int MaxIterations = 4096;

    public IterationLimitException() {}

    public IterationLimitException(String spec) {
        super(spec);
    }
}
