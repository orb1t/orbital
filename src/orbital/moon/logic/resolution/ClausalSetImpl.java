/**
 * @(#)ClausalSetImpl.java 0.8 2003-04-23 Andre Platzer
 *
 * Copyright (c) 2001-2003 Andre Platzer. All Rights Reserved.
 */

package orbital.moon.logic.resolution;

import java.util.Set;
import java.util.HashSet;

import orbital.util.Utility;
import orbital.util.Setops;
import orbital.logic.functor.Functionals;

/**
 * @version 0.8, 2003-04-23
 * @author  Andr&eacute; Platzer
 */
class ClausalSetImpl extends HashSet/*_<Clause>_*/ implements ClausalSet {
    /**
     * Copy constructor.
     */
    public ClausalSetImpl(Set/*<Formula>*/ clauses) {
	super(clauses);
	assert Setops.all(clauses, Functionals.bindSecond(Utility.instanceOf, Clause.class)) : "instanceof Set<Formula>";
    }
    public ClausalSetImpl() {}
}
