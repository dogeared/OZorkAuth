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
import java.util.StringTokenizer;

/**
 * This class provides various I/O functions to the ZCPU.
 * It uses an object that implements the ZUserInterface interface
 * (passed to it in the initialize method) to provide these functions.
 *
 * @author Matt Kimmel
 */
class ZIOCard extends Object {
    private ZUserInterface zui;
    private ZMemory memory;
    private int version;
    private int inputStream, outputStream;
	private boolean buffer; // If true, we only output text to the screen when the outputFlush method is called.
	private StringBuffer outputBuffer;
    private boolean[] isOpen = {false,true,true,true,true};
    private int baseMemAddr; // Base address of output stream 3 (or start of line in multiline mode)
    private int curMemAddr; // Current address on output stream 3
    private int memWidth; // Width of output stream 3
    private int memCursorX; // X position of cursor on output stream 3
    private boolean memMultiLine; // Is output stream 3 multiline?

    // The initialize method performs necessary initializations.
    // It is passed the ZUserInterface and ZMemory objects for
    // this ZMachine, as well as the version number of the
    // storyfile.
    void initialize(ZUserInterface ui,ZMemory mem,int ver,boolean buf)
    {
        zui = ui;
        memory = mem;
        version = ver;
        outputStream = 1;
        inputStream = 0;
		buffer = buf;
		outputBuffer = new StringBuffer();
    }

    // Print a string to the current output stream
    void printString(String s)
    {
        int n;

        // Ignore anything destined for a closed stream
        if (isOpen[outputStream] == false)
            return;
        switch (outputStream) {
            case 1 : // Screen -- currently, we send this right on to the user interface unless buffering
				if (buffer)
					outputBuffer.append(s);
				else
					zui.showString(s);
                break;
            case 2 : // For now, transcript stuff goes to stdout
                System.out.print(s);
                break;
            case 3 : // Memory
                if (!memMultiLine) { // Single-line mode--this is easy
                    for (n = 0;n < s.length();n++)
                        memory.putByte(curMemAddr+n,s.charAt(n));
                    curMemAddr += s.length();
                    memory.putWord(baseMemAddr,(memory.fetchWord(baseMemAddr) + s.length()));
                }
                else { // Multi-line mode.  Bleh.
                    // First, make a StringTokenizer out of this string
                    StringTokenizer st = new StringTokenizer(s,"\n ",true);
                    while (st.hasMoreTokens()) {
                        String tok = st.nextToken();
                        if (tok.equals("\n")) { // Start a new line
                            memory.putWord(curMemAddr,0);
                            baseMemAddr = curMemAddr;
                            curMemAddr += 2;
                        }
                        else { // Add to this line, wrap if necessary
                            if ((memCursorX + tok.length()) > (memWidth - 2)) { // Wrap
                                memory.putWord(curMemAddr,0);
                                baseMemAddr = curMemAddr;
                                curMemAddr += 2;
                            }
                            for (n = 0;n < tok.length();n++)
                                memory.putByte(curMemAddr+n,tok.charAt(n));
                            curMemAddr += tok.length();
                            memory.putWord(baseMemAddr,(memory.fetchWord(baseMemAddr) + tok.length()));
                        }
                    }
                }
                break;
            case 4 : // Command script--not implemented
                break;
        }
    }

	// Flush the output buffer by sending its contents to the user interface.
	// If we're not buffering, ignore.
	void outputFlush()
	{
		if (!buffer)
			return;
		if (outputBuffer.length() == 0)
			return;

		zui.showString(outputBuffer.toString());
		outputBuffer = new StringBuffer();
	}

    // Set output stream.
    void setOutputStream(int s,int baddr,int w,boolean multiLine)
    {
        if (s < 0) {
            isOpen[-s] = false;
            outputStream = 1; // This doesn't seem to be in the specification--or am I missing it?
            return;
        }

        if ((s == 0) || (s > 4))
            zui.fatal("Illegal output stream: " + s);

        if (s == 3) { // Open a memory stream
            memMultiLine = multiLine;
            baseMemAddr = baddr;
            memory.putWord(baseMemAddr,0);
            curMemAddr = baseMemAddr + 2;
            if (memMultiLine) {
                if (w < 0)
                    memWidth = -w;
                else {
                    Dimension d = zui.getWindowSize(w);
                    memWidth = d.width;
                }
                memCursorX = 0;
            }
			outputStream = 3;
			isOpen[3] = true;
        }
        else if (s == 4)
            zui.fatal("Output stream 4 not yet supported");
        else {
            outputStream = s;
            isOpen[s] = true;
        }
    }

    // Set input stream
    void setInputStream(int s)
    {
        if ((s < 0) || (s > 1))
            zui.fatal("Illegal input stream: " + s);

        if (s == 1)
            zui.fatal("Input stream 1 unsupported");
        else
            inputStream = s;
    }

    // Read from current input stream.  Currently just uses zui.readString().
    // If time is nonzero, time out after time tenths of a second.
    // Return 0 if there was a timeout.
    int readLine(StringBuffer sb,int time)
    {
        return zui.readLine(sb,time);
    }

	// Read a character from the current input stream.
	int readChar(int time)
	{
		int c;

		c = zui.readChar(time);
		return c;
	}
}
