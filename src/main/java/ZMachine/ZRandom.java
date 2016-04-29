// ZRandom - This class implements the random number generating
// routines required by the ZMachine.  It is implemented as a class
// rather than in the ZCPU using the Java random classes directly so that
// the implementation may be changed transparently, if need be.
//
// Copyright (c) 1996-1997, Matthew E. Kimmel.
package ZMachine;

import java.util.*;

class ZRandom extends Object {
    private ZUserInterface zui;
    private Random rnd;

    // The initialize function performs necessary initialization
    // (if any).  It is passed the ZUserInterface object for this
    // ZMachine.
    void initialize(ZUserInterface ui)
    {
        zui = ui;
        rnd = new Random();
    }

    // Seed the random number generator with s.  If s == 0, use
    // an outside source.
    void seed(int s)
    {
        if (s == 0)
            rnd = new Random();
        else
            rnd = new Random((long)s);
    }

    // Get a random number between 1 and s, inclusive.
    int getRandom(int s)
    {
		return (Math.abs(rnd.nextInt()) % s) + 1;
    }
}
