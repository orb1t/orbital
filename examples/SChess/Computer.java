import orbital.game.*;
import orbital.robotic.strategy.*;
import orbital.robotic.*;
import orbital.logic.functor.Function;
import orbital.util.*;
import orbital.Adjoint;
import orbital.SP;
import java.util.*;
import java.io.Serializable;

class ChessAspects implements Serializable {
    public double offensiveFigure = 0.1;			// offensive kind of figure
    public double beatingEnemies = 3.6;				// beating enemies now
    public double stormingOffensive = 1.0;			// storming to front and further
    public double topStorm = 0.01;					// storm from top flanke down
    public int	  maxDepth = 1;						// maximum search depth
}

/**
 * Implements a naive Computer-Player for Chess to show how it works.
 * To see what can be done with a better implementation there is
 * a Game called Seti.
 * @see <a href="http://beam.to/Twiph/">Seti Game</a>
 * @see orbital.game.AlphaBetaPruning
 * @todo use new AlphaBetaPruning capabilities
 */
public class Computer implements Function {
    ChessAspects aspects;
    public Computer(ChessAspects aspects) {
	this.aspects = aspects;
    }
    public Computer() {
	this(new ChessAspects());
    }

    /**
     * Act as an AIntelligence: make a Move for the Computer.
     */
    public Object apply(Object arg) {
	ChessField field = (ChessField) arg;
        field.rules.setOurLeague(field.rules.getTurn());
	Adjoint.print(Adjoint.DEBUG, "AI", "AI " + field.rules + " action {");
	MoveWeighting.Argument move = determineNextMove(field);
	Adjoint.print(Adjoint.DEBUG, "AI", "} AI " + field.rules + " will perform: " + move);
	return move;
    } 

    /**
     * Calculate the next Move and Figure that this Computer retrieves
     * as being the best for a situation like curField.
     */
    protected MoveWeighting.Argument determineNextMove(Field curField) {
	try {
	    Field	   field = (Field) curField.clone();	// clone a Field for preEvaluation
	    Evaluation ev = new Evaluation(Selection.Selecting.best(), new FieldWeighting(Selection.Selecting.best(), new ChessFigureWeighting(Computer.this, Selection.Selecting.best(), new ChessMoveWeighting(Computer.this))));
	    ev.apply(field);

	    Object eva = ev.evaluate();

	    if (eva == null)
		return null;
	    Pair arg = (Pair) Evaluation.getArg(eva);
	    return (MoveWeighting.Argument) arg.B;
	} catch (CloneNotSupportedException err) {
	    throw new InternalError("panic");
	} 
    } 
}


/**
 * used to choose which of our figures to move.
 */
final class ChessFigureWeighting extends FigureWeighting implements Function /* <Object, Number> */ {
    protected Computer computer;
    public ChessFigureWeighting(Computer c, Selection sel, Function /* <Object, Number> */ w) {
	super(sel, w);
	this.computer = c;
    }
    public Object apply(Object arg) {
	Argument i = (Argument) arg;
	Adjoint.print(Adjoint.DEBUG, "AI", " SF:weighting ... " + "( " + i + " -->");

	// we only move our figures, forget about moving opponents
	if (i.figure.league != ((ChessField) i.field).getTurn())
	    return new Double(Double.NaN);
	double val = ((Number) super.apply(arg)).doubleValue();

	if (i.figure.type != ChessRules.KING)
	    val += computer.aspects.offensiveFigure;

	Adjoint.print(Adjoint.DEBUG, "AI", " SF:  ) is weighted to " + val);
	return new Double(val);
    } 
}

/**
 * used to choose which move to take.
 */
final class ChessMoveWeighting extends MoveWeighting implements Function /* <Object, Number> */ {
    protected Computer computer;
    public ChessMoveWeighting(Computer c) {
	this.computer = c;
    }
    public Object apply(Object arg) {
	try {
	    Argument	i = (Argument) arg;
	    double		val = ((Number) super.apply(arg)).doubleValue();
	    ChessFigure f = (ChessFigure) i.figure;

	    Adjoint.print(Adjoint.DEBUG, "AI", "    SM: --> " + i.destination + "...");

	    int targetLeague = i.field.isBeating(i.move, i.destination);
	    if (targetLeague == i.figure.league)
		return new Double(Double.NaN);	  // disallow, only beat foreigners not cover allies
	    if (targetLeague != Figure.NOONE)	 // beating aspect for current move
		val += computer.aspects.beatingEnemies;

	    // here you can evaluate the result further. Omitted for simplicity

	    // storming aspect of marching figures
	    if (f.stormFront(i.destination) > 0)
		val += f.stormFront(i.destination) * (f.front() > 4 ? 1 : 0.6) * computer.aspects.stormingOffensive;

	    // bottom storm around run aspect
	    val += i.destination.y * computer.aspects.topStorm;
	    Adjoint.print(Adjoint.DEBUG, "AI", "    SM:  " + i.destination.x + "|" + i.destination.y + ": w=" + val);
	    return new Double(val);
	} catch (ClassCastException err) {
	    throw new InternalError("panic");
	} 
    } 
}