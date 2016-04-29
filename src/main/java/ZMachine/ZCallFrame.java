// ZCallFrame - A frame on the ZMachine's call stack.  This class
// has no methods, just variables.
//
// Copyright (c) 1996-1997, Matthew E. Kimmel.
package ZMachine;

import java.util.*;

class ZCallFrame {
    // Constants used with calltype;
    static final int FUNCTION = 0;
    static final int PROCEDURE = 1;
    static final int INTERRUPT = 2;

    // Variables
    int pc; // Program counter
    Stack routineStack; // Routine stack
    int[] localVars = new int[15]; // Local variables
    int numLocalVars; // Number of local variables
    int callType; // How this routine was called
    int argCount; // Argument count
	int frameNumber; // Used in CATCH and THROW.  First frame is 0, increases from there.
}
