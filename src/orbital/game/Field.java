/**
 * @(#)Field.java 0.9 1996/03/02 Andre Platzer
 * 
 * Copyright (c) 1996-2001 Andre Platzer. All Rights Reserved.
 */

package orbital.game;

import orbital.robotic.*;
import java.io.Serializable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Iterator;
import java.awt.Point;

import java.awt.Color;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import orbital.util.Pair;

import orbital.game.AdversarySearch.Option;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A rectangular field containing figures.
 * <p>
 * For performance reasons, it is recommended to overwrite {@link #expand()} with a
 * faster game-specific version that replaces the generic implementation.</p>
 * 
 * @version 1.0, 11/07/98
 * @author  Andr&eacute; Platzer
 * @structure composite bidirectional field: Figure[][]
 * @invariants sub classes support nullary constructor && isRectangular(field).
 * @see orbital.robotic.Table
 */
public class Field implements Serializable {
    private static final long serialVersionUID = -5715424141700986103L;
    private static final Logger logger = Logger.getLogger(Field.class.getPackage().getName());
    /**
     * All Figures contained in the field. And null for empty fields.
     * @serial
     */
    private Figure field[][];

    public Field(int width, int height) {
	this.field = new Figure[height][width];
    }

    protected Field(Dimension dimension) {
	this(dimension.width, dimension.height);
    }

    /**
     * for serialization and sub type cloning.
     */
    protected Field() {
	this.field = null;
    }

    /**
     * Creates a deep copy clone of the object.
     * @return		a clone of this Object with the same type, and a deep clone field.
     * @postconditions RES.getClass() == getClass() && "RES.field.equals(this.field)" && RES.field != field
     * @throws	CloneNotSupportedException Object explicitly does not
     * want to be cloned, or does not support a nullary constructor.
     */
    public Object clone() throws CloneNotSupportedException {
	try {
	    Field	  f = (Field) getClass().newInstance();
	    f.field = new Figure[field.length][field[0].length];
	    for (int y = 0; y < field.length; y++)
		for (int x = 0; x < field[0].length; x++) {
		    // Position p = new Position(x, y);
		    // f.setFigure(p, (Figure) getFigure(p).clone());
		    // optimized version
		    Figure figure = (Figure) field[y][x].clone();
		    f.field[y][x] = figure;
		    if (figure != null) {
			// figure might be changed
			figure.setField(f);
		    }
		} 
	    return f;
    	}
    	catch (InstantiationException ass) {
	    throw (CloneNotSupportedException) new CloneNotSupportedException("invariant: sub classes of " + Field.class + " must support nullary constructor for cloning").initCause(ass);
    	}
    	catch (IllegalAccessException ass) {
	    throw (CloneNotSupportedException) new CloneNotSupportedException("invariant: sub classes of " + Field.class + " must support nullary constructor for cloning").initCause(ass);
    	}
    } 

    /**
     * Checks whether two Fields are equal. By checking whether all
     * Figures contained are equal if compared pairwise.
     */
    public boolean equals(Object obj) {
	if (obj instanceof Field) {
	    Field		B = (Field) obj;
	    if (!getDimension().equals(B.getDimension()))
		return false;
	    //TODO: return Arrays.equals(field, B.field);
	    Iterator t = this.iterator();
	    Iterator e = B.iterator();
	    while (t.hasNext() && e.hasNext()) {
		Figure f1 = (Figure) t.next();
		Figure f2 = (Figure) e.next();
		if (!f1.equals(f2))
		    return false;
	    }

	    assert !(t.hasNext() || e.hasNext()) : "number of elements must be equal if dimension is equal";

	    // well then it's true
	    return true;
	} 
	return false;
    } 
	
    public int hashCode() {
	throw new InternalError("not yet implemented");
    }

    // get/set properties

    public Dimension getDimension() {
	return new Dimension(field[0].length, field.length);
    } 

    /**
     * Returns Figure at Position p
     */
    public Figure getFigure(Position p) {
	if (!inRange(p))
	    throw new ArrayIndexOutOfBoundsException("position " + p + " exceeds bounds " + getDimension());
	return field[p.y][p.x];
    } 

    /**
     * Sets Figure at Position p.
     * <p>
     * Automatically updates the Figure's internal position and field container reference.</p>
     * @internal see #clone()
     */
    public void setFigure(Position p, Figure f) {
	field[p.y][p.x] = f;
	if (f != null) {
	    // f might be changed
	    f.move(p);
	    if (f instanceof FigureImpl) {
		FigureImpl f2 = (FigureImpl)f;
		if (f2.getField() != null && f2.getField() != this)
		    logger.log(Level.FINE, "unsafe composite figure {0} was in {1} and is transferred to {2} in an unsafe way", new Object[] {f2, f2.getField(), this});
	    }
	    f.setField(this);
	}
    } 

    /**
     * returns whether Field p is empty. A Field is empty if the Figure
     * set there is null or it is explicitly set but regards itself as being
     * empty - checked with Figure.isEmpty().
     * @see Figure#isEmpty()
     */
    public final boolean isEmpty(Position p) {
	Figure f = getFigure(p);
	return f == null || f.isEmpty();
    } 

    public Dimension getPreferredSize() {
	int maxwidth = 0;
	int maxheight = 0;
	for (Iterator i = iterateNonEmpty(); i.hasNext(); ) {
	    Figure f = (Figure) i.next();
	    Dimension d = f.getPreferredSize();
	    if (d == null)
		continue;
	    if (d.width > maxwidth)
		maxwidth = d.width;
	    if (d.height > maxheight)
		maxheight = d.height;
	}
	Dimension fields = getDimension();
	return new Dimension(fields.width * maxwidth, fields.height * maxheight);
    }

    /**
     * Check whether a Point <code>(x|y)</code> is within the range.
     * @see orbital.robotic.Table#inRange(Point)
     */
    public final boolean inRange(Point p) {
	if (p.x < 0 || p.x >= field[0].length)
	    return false;
	if (p.y < 0 || p.y >= field.length)
	    return false;
	return true;
    } 


    /**
     * Returns an iterator over all Figures. Use the Iterator methods on
     * the returned object to fetch the elements sequentially.
     */
    public final Iterator/*_<Figure>_*/ iterator() {
	Dimension dim = getDimension();
	List	  v = new LinkedList();
	for (int y = 0; y < dim.height; y++)
	    for (int x = 0; x < dim.width; x++) {
		Position p = new Position(x, y);
		v.add(getFigure(p));
	    } 
	return v.iterator();
    } 
    /**
     * Returns an iterator over all non-empty Figures. Use the Iterator methods on
     * the returned object to fetch the elements sequentially.
     */
    public final Iterator/*_<Figure>_*/ iterateNonEmpty() {
	Dimension dim = getDimension();
	List	  v = new LinkedList();
	for (int y = 0; y < dim.height; y++)
	    for (int x = 0; x < dim.width; x++) {
		Position p = new Position(x, y);
		if (!isEmpty(p))
		    v.add(getFigure(p));
	    } 
	return v.iterator();
    } 

    /**
     * Get all reachable states.
     * <p>
     * It is generally encouraged to return only those successor states that result from moves
     * that would succeed at all.
     * So we only consider those figures that are in the league who actually takes the next move.
     * Otherwise, a computer player must sort out illegal moves, later on anyway.</p>
     * <p>
     * A decisive matter of performance for several algorithms like &alpha;-&beta;-pruning adversary search
     * is a <em>fast</em> implementation of this method, and a good ordering of the states returned.
     * Depending upon whose turn it is up to, probably better states should be returned prior to
     * probably worse ones.
     * However, this measure may depend upon whether maximizer or minimizer expands a state.</p>
     * <p>
     * It is almost always recommended to <em>overwrite</em> this method for decisive performance issues.
     * </p>
     * @return an iterator of all options (usually instances of {@link AdversarySearch.Option}) that are the successors of this field.
     * @todo enhance documentation
     */
    public Iterator expand() {
	try {
	    final List l  = new LinkedList();
	    for (Iterator i = iterateNonEmpty(); i.hasNext(); ) {
		final Figure figure = (Figure) i.next();
		for (Iterator j = figure.validMoves(); j.hasNext(); ) {
		    Pair     p = (Pair) j.next();
		    Move     move = (Move) p.A;
		    Position destination = (Position) p.B;
		    Field    field = (Field) clone();
		    //@todo optimize since most part is unnecessary. We already know it is a legal move, that it reaches its destination by a valid path, etc. Only, if most methods were final, we still need to check for "field.getFigure(((Position) figure).moving(move, destination);" and then perform "field.swap(figure, destination);"
		    if (field.move((Position) figure, move))
			l.add(new Option(field, destination, figure, move));
		}
	    }
	    return l.iterator();
    	}
    	catch (CloneNotSupportedException ass) {
	    throw (AssertionError) new AssertionError("invariant: sub classes must support nullary constructor for cloning.").initCause(ass);
    	}
    }

    /**
     * Performs a move on the figure at the specified position.
     * And if the move was successful then it swaps with the destination figure.
     * @return whether source figure was able to move.
     * @see Figure#moveFigure(Move)
     */
    public synchronized boolean move(Position source, Move move) {
	Position destination = getFigure(source).moveFigure(move);
	if (destination == null)
	    return false;
	swap(source, destination);
	return true;
    } 

    /**
     * Swaps two Figures at the given Positions in a Field.
     * This can be used to move Figures.
     * @postconditions EFFECT(swap(a, b)) == EFFECT(swap(b, a))
     */
    protected void swap(Position a, Position b) {
	Figure t = getFigure(a);
	setFigure(a, getFigure(b));
	setFigure(b, t);
    } 

    /**
     * Get the graphical bounds of the figure (i|j).
     * @param box the bounds of the field.
     * @param p the position (i|j) on the field.
     * @return the bounds within box of the figure at (i|j).
     * @see Gameboard#figureOn(int,int)
     */
    Rectangle boundsOf(Rectangle box, Position p) {
	Dimension dim = getDimension();
	int		  wFigure = box.width / dim.width;
	int		  hFigure = box.height / dim.height;
        return new Rectangle(wFigure * p.x, hFigure * p.y, wFigure, hFigure);
    }		

    /**
     * Paint a representation of this Field.
     * <p>
     * This implementation draws a checkboard background and all figures.</p>
     * @param g the Graphics object to paint to.
     * @param box the rectangle within graphics object into which we should paint.
     * @see Figure#paint(Graphics, Rectangle)
     */
    public void paint(Graphics g, Rectangle box) {
	Dimension dim = getDimension();
	int		  wFigure = box.width / dim.width;
	int		  hFigure = box.height / dim.height;
	if (wFigure == 0 || hFigure == 0)
	    throw new IllegalStateException("zero figure dimension: " + wFigure + "|" + hFigure);
	for (int y = 0; y < dim.height; y++)
	    for (int x = 0; x < dim.width; x++) {
		Rectangle fr = boundsOf(box, new Position(x, y));
		Graphics fg = g.create(fr.x, fr.y, fr.width, fr.height);
		// local to fg
		fr.setLocation(0, 0);
		fg.setColor((x & 1 ^ y & 1) == 0 ? Color.white : Color.darkGray);
		fg.fillRect(fr.x, fr.y, fr.width, fr.height);
		if (field[y][x] != null)
		    field[y][x].paint(fg, fr);
		fg.dispose();
	    } 
    } 

    /**
     * Checks whom the move to the given destination would beat.
     * Returns <code>NOONE</code> if this move is no beat or there is no target.
     * <p>
     * Utility method for AIntelligence and Weighting.</p>
     * <p><i><b>Evolves</b>: might be moved to another class</i>.</p>
     * @param move a valid legal Move, <b>really reaching</b> the given destination.
     * @param destination the destination reached by the move.
     * @return the league of the figure the move beats at the given destination.
     * <code>NOONE</code> if the move does not allow a beat or there is only an empty target.
     * @postconditions OLD(this) == this
     * @see Move#isBeating(int)
     * @see #isEmpty(Position)
     */
    public final int isBeating(Move move, Position destination) {
	if (!move.isBeating(move.length() - 1) || isEmpty(destination))	   // no beat or no target
	    return Figure.NOONE;
	return getFigure(destination).getLeague();
    } 

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
	Dimension	 dim = getDimension();
	StringBuffer sb = new StringBuffer(20 * dim.width * dim.height);
	for (int y = 0; y < dim.height; y++) {
	    for (int x = 0; x < dim.width; x++) {
		sb.append(field[y][x]);
		if (x < dim.width - 1)
		    sb.append(',');
	    } 
	    if (y < dim.height - 1)
		sb.append(";\n");
	} 
	return getClass().getName() + "[" + dim + ":" + sb + "]";
    } 
}
