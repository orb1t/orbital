/**
 * @(#)FieldChangeEvent.java 1.1 2003-01-03 Andre Platzer
 *
 * Copyright (c) 2003 Andre Platzer. All Rights Reserved.
 */

package orbital.game;

import java.util.EventObject;

/**
 * Field change event indicates that a <code>Field</code> has changed.
 *
 *
 * @author <a href="mailto:NOSPAM@functologic.com">Andr&eacute; Platzer</a>
 * @version 1.1, 2003-01-03
 * @see Field
 */
public class FieldChangeEvent extends EventObject {
    // enum for type
    /**
     * Field change event type for indicating that a new figure has been set at a position of a field.
     */
    public static final int SET_FIGURE = 1;
    /**
     * Field change event type for indicating that a figure has been moved.
     */
    public static final int MOVE = 2;
    /**
     * Field change event type (true type as additional flag) for indicating that a user action caused this event.
     */
    static final int USER_ACTION = 0x1000;
    /**
     * Field change event type for indicating that we have reached the end of turn.
     */
    public static final int END_OF_TURN = 0x10;
    /**
     * Field change event type for indicating that we have reached the end of the game.
     * The winner then is available in change info encoded as an {@link java.lang.Integer}.
     */
    public static final int END_OF_GAME = 0x20;
    public FieldChangeEvent(Field source, int type, Object changeInfo) {
	super(source);
	this.type = type;
	this.changeInfo = changeInfo;
    }

    private int type;
    public int getType() {
	return type;
    }

    private Object changeInfo;

    /**
     * Gets the value of changeInfo
     *
     * @return the value of changeInfo
     */
    public Object getChangeInfo() {
	return this.changeInfo;
    }

    public Field getField() {
	return (Field) super.getSource();
    }

    public String toString() {
	return getClass().getName() + "[type=" + getType() + ", changeInfo=" + getChangeInfo() + ", field=" + getField() + ']';
    }
    
}// FieldChangeEvent

