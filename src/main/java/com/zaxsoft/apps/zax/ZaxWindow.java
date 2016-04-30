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

import java.awt.*;

/**
 * Class containing information about the Z-Machine's logical
 * windows.  In the future, there may be more data here.
 *
 * @author Matt Kimmel
 */
class ZaxWindow extends Object {
    // Various state information maintained when window is not current
    public Rectangle shape; // This Z-window's position and size
    public Point cursorPosition; // Real cursor position associated with window
    public int tstyle; // Current text style
    // Constructor
    public ZaxWindow()
    {
        shape = new Rectangle(0,0,0,0);
        cursorPosition = new Point(0,0);
        tstyle = 0;
    }

    public ZaxWindow(int x,int y,int w,int h)
    {
        shape = new Rectangle(x,y,w,h);
        cursorPosition = new Point(0,0);
        tstyle = 0;
    }
}
