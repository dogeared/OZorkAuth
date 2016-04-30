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
package com.zaxsoft.apps.zax;

import com.zaxsoft.awt.TextScreen;
import com.zaxsoft.zmachine.ZCPU;
import com.zaxsoft.zmachine.ZUserInterface;

import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Zax main class.
 *
 * @author Matt Kimmel
 */
class Zax extends Frame implements ZUserInterface {
	static String versionString = "0.91";
    ZCPU cpu;
    TextScreen screen; // The main screen
    ZaxWindow[] windows;  // Z-Machine Windows.
    ZaxWindow curWindow; // The current window.
    ZaxWindow statusBar; // The status bar, in V1-3
	Dimension screenSize; // Size of the entire screen in characters
    int version = 0;    // Version of this storyfile - 0 if game not yet initialized.
    int moreLines = 0; // Number of lines before next MORE
    Hashtable inputCharacters; // Used to translate between Event input characters and Z-Machine input characters
    Vector terminatingCharacters; // List of terminating characters for READ operations
	Thread cpuThread = null; // Thread of ZMachine CPU
    
    // Main routine.  Just instantiates the class.
    public static void main(String args[])
    {
        Zax j = new Zax();
    }

    // Constructor
	public Zax()
	{
		// Set up the frame.
		setTitle("Zax v" + versionString);

		// Construct a menu bar
		MenuBar mb = new MenuBar();
		Menu m = new Menu("File");
		m.add("Play Story...");
		m.addSeparator();
		m.add("Exit");
		mb.add(m);
//		m = new Menu("Help");
//		m.add("About");
//		mb.setHelpMenu(m);
		setMenuBar(mb);
        
		// Put a screen up-this screen will be replaced later.
        setResizable(false);
        screen = new TextScreen(new Font("Monospaced",Font.PLAIN,12),
                                Color.blue,Color.white,25,80,0);
        add(screen);
        Insets ins = getInsets();
        setSize(ins.left + ins.right + screen.getPreferredSize().width,
                ins.top + ins.bottom + screen.getPreferredSize().height);
        show();
	}

	// Handle menu events
	public boolean action(Event evt,Object what)
	{
		// See if this is a menu event
		if ("Play Story...".equals(what)) { // Play a story file
			playStory();
			return true;
		}
		else if ("Exit".equals(what)) {
			System.exit(0);
		}
		
		return false;
	}

	// Play a story file
    private void playStory()
    {
		FileDialog fd;
		String dir, file, pathname;

		// If a story is already running, ignore this (for now)
		if (cpuThread != null)
			return;

		// Create a ZMachine instance
        cpu = new ZCPU(this);

		// Allow the user to select a story file
		fd = new FileDialog(this,"Open a Storyfile",FileDialog.LOAD);
		fd.show();
		dir = fd.getDirectory();
		file = fd.getFile();
		if ((dir == null) || (file == null))
			return;
		pathname = dir + file;

        cpu.initialize(pathname);
        cpuThread = cpu.start();
	}

    // Private method called when switching windows
    private void switchWindow(ZaxWindow w)
    {
        curWindow.cursorPosition = screen.getCursorPosition();
        curWindow = w;
        screen.setRegion(w.shape);
        setTextStyle(curWindow.tstyle);
        screen.gotoXY(w.cursorPosition.x,w.cursorPosition.y);
        if (w == windows[0])
            screen.setAttributes(TextScreen.KEYECHO | TextScreen.CURSOR);
        else
            screen.setAttributes(0);
    }
    
    
    /////////////////////////////////////////////////////////
    // ZUserInterface methods
    /////////////////////////////////////////////////////////

    // fatal - print a fatal error message and exit
    // Windows must be initialized!
    public void fatal(String errmsg)
    {
        screen.printString("FATAL ERROR: " + errmsg + "\n");
        screen.printString("Hit a key to exit.\n");
        screen.readChar();
        System.exit(1);
    }

    // Initialize the user interface.  This consists of setting
    // up a status bar and a lower window in V1-2, and an upper
    // and lower window in V3-5,7-8.  Not sure yet what this
    // involves in V6.
    public void initialize(int ver)
    {
        // Code for restarting goes here.  version is non-zero
        // if this is a restart.
        // Initialize the Event-->Z-Character translation table.
        // Warning!  Hardcoded values ahead!
        inputCharacters = new Hashtable(16);
        inputCharacters.put(new Integer(Event.UP),new Integer(129));
        inputCharacters.put(new Integer(Event.DOWN),new Integer(130));
        inputCharacters.put(new Integer(Event.LEFT),new Integer(131));
        inputCharacters.put(new Integer(Event.RIGHT),new Integer(132));
        inputCharacters.put(new Integer(Event.F1),new Integer(133));
        inputCharacters.put(new Integer(Event.F2),new Integer(134));
        inputCharacters.put(new Integer(Event.F3),new Integer(135));
        inputCharacters.put(new Integer(Event.F4),new Integer(136));
        inputCharacters.put(new Integer(Event.F5),new Integer(137));
        inputCharacters.put(new Integer(Event.F6),new Integer(138));
        inputCharacters.put(new Integer(Event.F7),new Integer(139));
        inputCharacters.put(new Integer(Event.F8),new Integer(140));
        inputCharacters.put(new Integer(Event.F9),new Integer(141));
        inputCharacters.put(new Integer(Event.F10),new Integer(142));
        inputCharacters.put(new Integer(Event.F11),new Integer(143));
        inputCharacters.put(new Integer(Event.F12),new Integer(144));
    
        // Set up the terminating characters.  Carriage Return
        // (13) is always a terminating character.  Also LF (10).
        terminatingCharacters = new Vector();
        terminatingCharacters.addElement(new Integer(13));
        terminatingCharacters.addElement(new Integer(10));
        
        // Set up the screen, etc
        version = ver;
        if (screen != null)
            remove(screen);
		screenSize = new Dimension(80,25); // TODO: better way to set this?
        screen = new TextScreen(new Font("Monospaced",Font.PLAIN,12),
                                Color.blue,Color.white,screenSize.height,
                                screenSize.width,0);
        add(screen);
        Insets ins = getInsets();
        setSize(ins.left + ins.right + screen.getPreferredSize().width,
                ins.top + ins.bottom + screen.getPreferredSize().height);
		show();
        screen.setTerminators(terminatingCharacters);
        
        // Depending on which storyfile version this is, we set
        // up differently.
        if ((version == 1) || (version == 2)) { // V1-2
            // For version 1-2, we set up a status bar and a
            // lower window.
            statusBar = new ZaxWindow(0,0,screenSize.width,1);
            windows = new ZaxWindow[1];
            windows[0] = new ZaxWindow(0,1,screenSize.width,screenSize.height-1);
            
            // Start off in window 0
            curWindow = windows[0];
            screen.gotoXY(windows[0].shape.x,windows[0].shape.y);
            screen.setAttributes(TextScreen.KEYECHO | TextScreen.CURSOR);
            screen.setRegion(windows[0].shape);
            windows[0].cursorPosition = screen.getCursorPosition();
            statusBar.cursorPosition = new Point(0,0);
            return;
        }
        if (version == 3) { // V3
            // For V3, we set up a status bar AND two windows.
            // This all may change.
            statusBar = new ZaxWindow(0,0,screenSize.width,1);
            windows = new ZaxWindow[2];
            windows[1] = new ZaxWindow(0,1,screenSize.width,0);
            windows[0] = new ZaxWindow(0,1,screenSize.width,screenSize.height-1);

            // Start off in window 0
            curWindow = windows[0];
            screen.setAttributes(TextScreen.KEYECHO | TextScreen.CURSOR);
            screen.setRegion(windows[0].shape);
            screen.gotoXY(windows[0].shape.x,windows[0].shape.y);
            windows[0].cursorPosition = screen.getCursorPosition();
            windows[1].cursorPosition = new Point(0,1);
            statusBar.cursorPosition = new Point(0,0);
            return;
        }

        if (((version >= 4) && (version <= 8)) && (version != 6)) {
            // V4-5,7-8; Use an upper window and a lower window.
            windows = new ZaxWindow[2];
            windows[0] = new ZaxWindow(0,1,screenSize.width,screenSize.height-1);
            windows[1] = new ZaxWindow(0,0,screenSize.width,1);

            // Start off in window 0
            curWindow = windows[0];
            screen.setAttributes(TextScreen.KEYECHO | TextScreen.CURSOR);
            screen.setRegion(windows[0].shape);
            screen.gotoXY(windows[0].shape.x,windows[0].shape.y);
            windows[0].cursorPosition = screen.getCursorPosition();
            windows[1].cursorPosition = new Point(0,0);
            return;
        }

        // Otherwise, this is an unsupported version.
        System.out.println("Unsupported storyfile version.");
        System.exit(1);
    }
    
    // Sets the terminating characters for READ operations (other than
    // CR).  Translates from Z-Characters to Event characters by
    // enumerating through the inputCharacter table.
    public void setTerminatingCharacters(Vector chars)
    {
        Integer c;
        Integer key, element;
        Enumeration e;
        boolean found;
        
        for (int i = 0;i < chars.size();i++) {
            c = (Integer)chars.elementAt(i);

            // We don't bother using the Hashtable contains() method--
            // that just makes this whole thing more expensive.
            e = inputCharacters.keys();
            found = false;
            while (e.hasMoreElements()) {
                key = (Integer)e.nextElement();
                element = (Integer)inputCharacters.get(key);
                if (element == c) {
                    terminatingCharacters.addElement(key);
                    found = true;
                    break;
                }
            }
            
            if (!found)
                terminatingCharacters.addElement(c);
        }
        screen.setTerminators(terminatingCharacters);
    }

    // We support a status line in V1-3 only.
    public boolean hasStatusLine()
    {
        if ((version >= 1) && (version <= 3))
            return true;
        else
            return false;
    }

    // We support an upper window starting at V3.
    public boolean hasUpperWindow()
    {
        if (version >= 3)
            return true;
        else
            return false;
    }

    // For now, we always use a fixed-width font.
    public boolean defaultFontProportional()
    {
        return false;
    }

	public boolean hasFixedWidth()
	{
		return true;
	}

	// Yes, we have colors
	public boolean hasColors()
	{
		return true;
	}

	// Yes, we have italic
	public boolean hasItalic()
	{
		return true;
	}

	// Yes, we have boldface
	public boolean hasBoldface()
	{
		return true;
	}

    // Yes, we have timed input
    public boolean hasTimedInput()
    {
        return true;
    }
    
	// Our default background color is blue right now. FIX THIS
	public int getDefaultBackground()
	{
		return 6;
	}

	// Our default foreground color is white for now
	public int getDefaultForeground()
	{
		return 9;
	}

    // Show the status bar (it is guaranteed that this will only
    // be called during a V1-3 game).
    public void showStatusBar(String s,int a,int b,boolean flag)
    {
		String status;
		String s1, s2, s3;
        ZaxWindow lastWindow;
        
        // This is kinda hacky for now.
        lastWindow = curWindow;
        switchWindow(statusBar);
        screen.reverseVideo(true);

		s1 = new String(" " + s + " ");
		if (flag) {
			s2 = new String(" Time: " + a + ":");
			if (b < 10)
				s2 += "0";
			s2 = s2 + b;
			s3 = new String(" ");
		}
		else {
			s2 = new String(" Score: " + a + " ");
			s3 = new String(" Turns: " + b + " ");
		}

		status = new String(s1);
		for (int i=0;i<(screenSize.width - (s1.length() + s2.length() + s3.length()));i++)
			status = status + " ";
		status = status + s2 + s3;
		screen.printString(status);
		screen.reverseVideo(false);
		switchWindow(lastWindow);
    }

    // Split the screen, as per SPLIT_SCREEN
    public void splitScreen(int lines)
    {
        if (lines > screenSize.height)
            lines = screenSize.height;
        
        // Make sure the current cursor position is saved.
        curWindow.cursorPosition = screen.getCursorPosition();
        
        // Set window 1 to the desired size--it is always at (0,0).
        // Reposition the cursor if it is now outside the window.
        windows[1].shape = new Rectangle(0,0,screenSize.width,lines);
        if (windows[1].cursorPosition.y >= windows[1].shape.height)
            windows[1].cursorPosition = new Point(0,0);
        
        // Ditto for window 0, which always covers the bottom part of
        // the screen.
        windows[0].shape = new Rectangle(0,lines,screenSize.width,(screenSize.height - lines));
        if (windows[0].cursorPosition.y < windows[0].shape.y)
            windows[0].cursorPosition = new Point(0,windows[0].shape.y);
        
        // Make sure the cursor and region are in the right place.
        screen.gotoXY(curWindow.cursorPosition.x,curWindow.cursorPosition.y);
        screen.setRegion(curWindow.shape);
    }
    
    // Set the current window, possibly clearing it.
    public void setCurrentWindow(int window)
    {
        switchWindow(windows[window]);
        if (window == 1) {
            if (version < 4)
                screen.clearScreen();
            screen.gotoXY(curWindow.shape.x,curWindow.shape.y);
        }
    }

    // Read a line of input from the current window.  If time is
    // nonzero, time out after time tenths of a second.  Return 0
    // if a timeout occurred, or the terminating character if it
    // did not.
    public int readLine(StringBuffer sb,int time)
    {
        int rc;
        Integer zc;
        
        moreLines = 0;
        screen.requestFocus();
        if (time == 0)
            rc = screen.readLine(sb);
        else
            rc = screen.readLine(sb,(long)(time * 100));
        if (rc == -2)
            fatal("Unspecified input error");
        if (rc == -1)
            return -1;
        zc = (Integer)inputCharacters.get(new Integer(rc));
        if (zc == null)
            return rc;
        else
            return zc.intValue();
    }

    // Read a single character from the current window
    public int readChar(int time)
    {
        int key;
        Integer zchar;
        
        moreLines = 0;
		screen.requestFocus();
		if (time == 0)
            key = screen.readChar();
        else
            key = screen.readChar((long)(time * 100));
        if (key == -2)
            fatal("Unspecified input error");
        if (key == -1)
            return -1;
        zchar = (Integer)inputCharacters.get(new Integer(key));
        if (zchar == null)
            return key;
        else
            return zchar.intValue();
    }

    // Display a string -- this method does a number of things, including scrolling only
	// as necessary, word-wrapping, and "more".
    public void showString(String s)
    {
		Point cursor;
		String outstr, curtoken;
		StringTokenizer intokens;
		int scrollLines = 0; // We don't always do a more and a scroll at the same time.
		int curCol;

		// If this is not window 0, the output window, then we don't do any special
		// handling.  Is this correct?  Probably not in V6.
		if (curWindow != windows[0]) {
			screen.printString(s);
			return;
		}

		// Get the current dimensions and cursor position of the screen.
		cursor = screen.getCursorPosition();
		cursor.x -= curWindow.shape.x;
		cursor.y -= curWindow.shape.y;
		curCol = cursor.x;
		if (cursor.y < (curWindow.shape.height - 1))
			scrollLines = -(curWindow.shape.height - cursor.y - 1);
		else
			scrollLines = 0;
//		moreLines = 0;

		// Now, go through the string as a series of tokens.  Word-wrap, scroll, and
		// "more" as necessary.
		outstr = new String();
		intokens = new StringTokenizer(s,"\n ",true);
		while (intokens.hasMoreTokens()) {
			curtoken = intokens.nextToken();
			if (curtoken.equals("\n")) {
				outstr = outstr + "\n";
				scrollLines++;
				moreLines++;
				curCol = 0;
				if (moreLines >= (curWindow.shape.height - 1)) {
					screen.scrollUp(scrollLines);
					scrollLines = 0;
					screen.printString(outstr);
					outstr = new String();
					screen.reverseVideo(true);   // May need to check current state of screen
					screen.printString("[--More--]");
					screen.reverseVideo(false);
					int oldattrs = screen.getAttributes();
					screen.setAttributes(oldattrs & ~(TextScreen.KEYECHO));
					screen.requestFocus();
					int junk = screen.readChar();
					screen.setAttributes(oldattrs);
					screen.printString("\b\b\b\b\b\b\b\b\b\b");
					moreLines = 0;
				}
				continue;
			}
			if ((curtoken.length() + curCol) > (curWindow.shape.width - 2)) { // word wrap
				if (curtoken.equals(" ")) // Skip spaces at ends of lines.
					continue;
				outstr = outstr + "\n";
				scrollLines++;
				moreLines++;
				curCol = 0;
				if (moreLines >= (curWindow.shape.height - 1)) {
					screen.scrollUp(scrollLines);
					scrollLines = 0;
					screen.printString(outstr);
					outstr = new String();
					screen.reverseVideo(true);   // May need to check current state of screen
					screen.printString("[--More--]");
					screen.reverseVideo(false);
					int oldattrs = screen.getAttributes();
					screen.setAttributes(oldattrs & ~(TextScreen.KEYECHO));
					screen.requestFocus();
					int junk = screen.readChar();
					screen.setAttributes(oldattrs);
					screen.printString("\b\b\b\b\b\b\b\b\b\b");
					moreLines = 0;
				}
			}
			outstr = outstr + curtoken;
			curCol += curtoken.length();
		}

		// Output whatever's left.
		if (scrollLines > 0)
			screen.scrollUp(scrollLines);
		if (outstr.length() > 0)
			screen.printString(outstr);
    }

    // Scroll the current window
    public void scrollWindow(int lines)
    {
        if (lines < 0)
            fatal("Scroll down not yet implemented");

        screen.scrollUp(lines);
    }

    // Erase a line in the current window
    public void eraseLine(int size)
    {
        fatal("eraseLine not yet implemented");
    }

	// Erase a window
	public void eraseWindow(int window)
	{
	    ZaxWindow lastWindow;

	    lastWindow = curWindow;
	    switchWindow(windows[window]);
		screen.clearScreen();
		switchWindow(lastWindow);
	}

	// Get a filename for save or restore
	public String getFilename(String title,String suggested,boolean saveFlag)
	{
		FileDialog fd;
		String s;
		int i;

		fd = new FileDialog(this,title,(saveFlag?FileDialog.SAVE:FileDialog.LOAD));
		if (suggested != null)
			fd.setFile(suggested);
		else if (saveFlag)
			fd.setFile("*.zav");
		fd.show();
		s = fd.getFile();
		if (s == null)
		    return null;
		if (s.length() == 0)
		    return null;
		    
		// The Windows 95 peer sometimes appends crud to the filename.
		i = s.indexOf(".*",0);
		if (i > -1)
			s = s.substring(0,i);
		return fd.getDirectory() + s;
	}

	// Return the cursor position, 1-based
	public Point getCursorPosition()
	{
	    Point p = screen.getCursorPosition();
	    p.x -= curWindow.shape.x;
	    p.y -= curWindow.shape.y;
	    p.x++;
	    p.y++;
	    return p;
	}

	// Set the cursor position (1-based)
	public void setCursorPosition(int x,int y)
	{
	    x--;
	    y--;
	    x += curWindow.shape.x;
	    y += curWindow.shape.y;
		screen.gotoXY(x,y);
	}

	// Set the font
	public void setFont(int font)
	{
		System.out.println("Ignored a SET_FONT!");
	}

	// Return the size of the current font.
	public Dimension getFontSize()
	{
		Dimension d;

		d = screen.getFontSize();
		return d;
	}

    // Return the size of the specified window
    public Dimension getWindowSize(int window)
    {
        return new Dimension(windows[window].shape.width,windows[window].shape.height);
    }
    
	// Set the current colors
	public void setColor(int fg,int bg)
	{
		System.out.println("Ignored a SET_COLOR!");
	}

	// Set the text style
	public void setTextStyle(int style)
	{
	    curWindow.tstyle = style;
	 
	    // If the style is 0, just clear everything
	    if (style == 0) {
	        screen.reverseVideo(false);
	        screen.setFontStyle(Font.PLAIN);
	        return;
	    }
	    
	    // Otherwise, set things according to the bits we
	    // were passed.  Bit 3 (fixed-width) is currently
	    // unimplemented.
	    if ((style & 0x01) == 0x01)
	        screen.reverseVideo(true);
	    if ((style & 0x02) == 0x02)
	        screen.setFontStyle(Font.BOLD);
	    if ((style & 0x04) == 0x04)
	        screen.setFontStyle(Font.ITALIC);
	    // Just ignore any styles we don't know about.
	}

	// Get the size of the current window in characters
	public Dimension getScreenCharacters()
	{
		return screenSize;
	}

	// Get the size of the screen in units
	public Dimension getScreenUnits()
	{
		Dimension d, scr, fs;

		d = new Dimension();
		fs = getFontSize();
		scr = getScreenCharacters();
		d.width = scr.width * fs.width;
		d.height = scr.height * fs.height;
		return d;
	}

    // quit--end the program
    public void quit()
    {
        Thread curThread = cpuThread;
		cpuThread = null;
		curThread.stop();
    }

	// restart--prepare for a restart
	public void restart()
	{
		initialize(version);
	}
}
