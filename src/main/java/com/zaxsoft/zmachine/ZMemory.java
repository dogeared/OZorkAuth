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

import java.io.*;

class ZMemory extends Object {
    private ZUserInterface zui;
    byte[] data;
    int dataLength;

    // The initialize routine sets things up and loads a game
    // into memory.  It is passed the ZUserInterface object
    // for this ZMachine and the filename of the story-file.
    void initialize(ZUserInterface ui,String storyFile)
    {
        File f;
        FileInputStream fis;
        DataInputStream dis;
        zui = ui;

        // Read in the story file
        f = new File(storyFile);
        if((!f.exists()) || (!f.canRead()) || (!f.isFile()))
            zui.fatal("Storyfile " + storyFile + " not found.");
        dataLength = (int)f.length();
        data = new byte[dataLength];
        try {
            fis = new FileInputStream(f);
            dis = new DataInputStream(fis);
            dis.readFully(data,0,dataLength);
            fis.close();
        }
        catch (IOException ioex) {
            zui.fatal("I/O error loading storyfile.");
        }
    }

    // Fetch a byte from the specified address
    int fetchByte(int addr)
    {
        if (addr > (dataLength - 1))
            zui.fatal("Memory fault: address " + addr);
        int i = (data[addr] & 0xff);
        return i;
    }

    // Store a byte at the specified address
    void putByte(int addr,int b)
    {
        if (addr > (dataLength - 1))
            zui.fatal("Memory fault: address " + addr);
        data[addr] = (byte)(b & 0xff);
    }

    // Fetch a word from the specified address
    int fetchWord(int addr)
    {
        int i;

        if (addr > (dataLength - 1))
            zui.fatal("Memory fault: address " + addr);
        i = (((data[addr] << 8) | (data[addr+1] & 0xff)) & 0xffff);
        return i;
    }

    // Store a word at the specified address
    void putWord(int addr,int w)
    {
        if (addr > (dataLength - 1))
            zui.fatal("Memory fault: address " + addr);
        data[addr] = (byte)((w >> 8) & 0xff);
        data[addr+1] = (byte)(w & 0xff);
    }

	// Dump the specified amount of memory, starting at the specified address,
	// to the specified DataOutputStream.
	void dumpMemory(DataOutputStream dos,int addr,int len) throws IOException
	{
		dos.write(data,addr,len);
	}

	// Read in memory stored by dumpMemory.
	void readMemory(DataInputStream dis,int addr,int len) throws IOException
	{
		dis.read(data,addr,len);
	}
}
