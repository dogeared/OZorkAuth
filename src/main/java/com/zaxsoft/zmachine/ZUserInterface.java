/**
 * Copyright (c) 2008 Matthew E. Kimmel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.zaxsoft.zmachine;

import java.awt.*;
import java.util.Vector;

/**
 * ZUserInterface - This interface must be implemented by a programmer
 * using the ZMachine classes, to provide various I/O routines and
 * other routines that will differ with different user interfaces.
 *
 * @author Matt Kimmel
 */
public interface ZUserInterface {
    // This method is called when a fatal error is encountered.
    // It should never return.
    public void fatal(String errmsg);

    // This method is called when the game starts or restarts,
    // to initialize windows and such.  The version number of
    // the current game is passed.
    public void initialize(int ver);

    // This method sets the additional terminating characters for
    // READ operations.  It should be called after initialize, and
    // should be passed a Vector of Integer objects containing
    // the Z-Characters that should be treated as terminating characters.
    public void setTerminatingCharacters(Vector chars);
    
    // This method returns true if the user interface supports a
    // status line.
    public boolean hasStatusLine();

    // This method returns true if the user interface supports an
    // upper window.
    public boolean hasUpperWindow();

    // This method returns true if the default font is variable-width.
    public boolean defaultFontProportional();

	// This method returns true if the user interface supports colors.
	public boolean hasColors();

	// This method returns true if boldface styles are available.
	public boolean hasBoldface();

	// This method returns true if italic styles are available.
	public boolean hasItalic();

	// This method returns true if fixed-width styles are available.
	public boolean hasFixedWidth();

    // This method returns true if timed input is supported.
    public boolean hasTimedInput();
    
	// This method returns the size of the "screen" in lines/columns.
	public Dimension getScreenCharacters();

	// This method returns the size of the "screen" in "units".
	public Dimension getScreenUnits();

	// This method returns the size of the current font in "units".
	public Dimension getFontSize();

    // This method returns the size of the specified window, in characters.
    public Dimension getWindowSize(int window);
    
	// These methods return the default foreground and background colors
	// (as Z-Machine color numbers).
	public int getDefaultForeground();
	public int getDefaultBackground();

	// This method returns the current cursor position.
	public Point getCursorPosition();

    // This method shows the status bar.  This is handled entirely
    // by the user interface, rather than in the CPU or the I/O card,
    // in order to give the user interface programmer some flexibility
    // about where to put the information (in the window title bar,
    // for example).
    public void showStatusBar(String s,int a,int b,boolean flag);

    // Split the screen, as per SPLIT_SCREEN instruction
    public void splitScreen(int lines);
    
    // This method sets the current window, to which all output
    // will be directed.  In V1-3, this implies clearing the window;
    // in V4+, it implies homing the cursor.
    public void setCurrentWindow(int window);

	// This method sets the cursor to position x,y (or turns the cursor on or off--
	// see Z-Machine spec).
	public void setCursorPosition(int x,int y);

	// This method sets the foreground and background colors. 0 means no change.
	public void setColor(int fg,int bg);

	// Set the text style (see Z-Machine spec for meaning of parameter)
	public void setTextStyle(int style);

	// Set the font--again, see Z-Spec.
	public void setFont(int font);

    // This method reads a line of user input and appends it to the
    // supplied StringBuffer; it returns the terminating character.
    // If time is nonzero, the readLine will time out if a terminating
    // character has not been encountered after time tenths of a second.
    // If a timeout occurs, the return value will be 0.
    public int readLine(StringBuffer sb,int time);

    // This method reads a character from the user and returns it.  If time is
    // nonzero, it times out after time/10 seconds.
    public int readChar(int time);

    // Print a string
    public void showString(String s);

    // This method scrolls the current window by the specified
    // number of lines.
    public void scrollWindow(int lines);

    // This method erases a line in the current window.
    public void eraseLine(int s);

	// This method erases the current window.
	public void eraseWindow(int window);

	// This method gets a filename from the user, possibly using a FileDialog.
	// A title for a dialog box is supplied.  If saveFlag is true, then we
	// are saving a file; otherwise, we're loading a file.  The method should
	// return null if there was an error or the user canceled.
	public String getFilename(String title,String suggested,boolean saveFlag);

    // This function is called when the Z-Machine halts.  It
    // should not return.
    public void quit();

	// This function is called when the Z-Machine is about to
	// restart.  The UI should prepare by reseting itself to
	// an initial state.  The function should return.
	public void restart();
}
