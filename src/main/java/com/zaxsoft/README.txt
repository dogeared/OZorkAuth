ZAX v0.91
Z-Code Application Executor
Matt Kimmel
matt at infinite-jest dot net
9/14/2008

LEGAL STUFF

As of version 0.91, Zax is now open source, licensed under the MIT License.  Please see the accompanying LICENSE.txt file for legal details.

DISCLAIMER

Zax was my first major Java project, and most of it was written in 1996 and 1997. Hence, there are a lot of idioms in the code that are more C-like and less Java-like. Please bear with me; I'm fixing them as I work on the code.

WHAT IS ZAX?

Zax is a Java 2 implementation of the Z-Machine.  An explanation of the Z-Machine is beyond the scope of this document, but in a nutshell, it is a virtual machine that runs programs written in Z-code, which includes all of Infocom's text adventures and a large number of text adventures and other programs written using the Inform compiler.  Information about all of this is available at the Interactive Fiction Archive, at ftp://ftp.gmd.de/if-archive.

Zax is capable of running Z-code programs of versions 1 through 8, with the exception of version 6 programs.  It complies with the Z-Machine standard v0.2 as specified by Graham Nelson (with a couple of exceptions; see below).  It runs all of the non-graphical Infocom games except Beyond Zork (which exhibits some trouble with its on-screen map), and quite a lot of the more modern games written with Inform.

Zax is written entirely in Java, and should run on any Java Virtual Machine of version 1.5 or higher.

Please note that Zax is a Java application, not an applet, and is therefore not suitable for running in a browser.  Zax was developed independently of ZPlet, which is a Java applet implementation of the Z-Machine released around the same time as Zax's first release (circa 1997).


USING ZAX

If you have retrieved the official Zax distribution, you should have a "bin" directory containing a complete binary JAR of Zax, called zax.jar.  To run Zax, you can use the command-line Java launcher included with the Java Runtime Environment, like this:

java -jar zax.jar

On some systems, you may also be able to double-click on the JAR file in a desktop file explorer to run Zax.

Once Zax is running, just select Play Story... under the File menu and you're on your way.


BUILDING ZAX

Zax comes with a very rudimentary ant build file.  You can build all the classes using a command like this:

ant -f zax.xml all

You can also create an Eclipse or IntelliJ IDEA or Netbeans (or IDE of your choice) project around the source files and build that way.  Zax currently has no dependencies outside of the standard Java libraries, which makes things easy.


HOW ZAX WORKS

Zax is composed of three components: A GUI front-end (Zax); a generic Z-Machine implementation (package ZMachine); and a text-screen widget used by the front-end.  The ZMachine package is implemented in such a way that the user interface is completely abstracted.  When the CPU is initialized, it is passed an instance of an object that implements the interface ZUserInterface; this interface is a set of generic methods for various fundamental user-interface functions (such as text output and Z-Machine window management) which can be implemented in any way that the interface programmer finds desirable. My front-end uses a custom AWT widget designed to emulate an IBM Text-Mode-style screen, but pretty much any user interface is possible.

The Zax user interface consists of two classes: Zax, which implements both a high-level interface (providing functions such as "Play Story"), and ZaxWindow, which is used by Zax to keep track of logical Z-Machine windows.  Zax is a subclass of java.awt.Frame.  Zax uses the kimmel.awt.TextScreen class to display and manipulate text.

The ZMachine package consists of several classes.  ZMachine.ZCPU is the heart of the Z-Machine; it implements the CPU and all of its opcodes.  ZMemory is the Z-Machine's memory manager; it is responsible for initializing memory, encapsulating access to it, and dumping it to and reading it from a stream.  ZIOCard encapsulates the Z-Machine I/O streams (in most cases passing read and write requests to and from the user interface).  ZObjectTable encapsulates the Z-Machine's object table stuctures (including properties and attributes); it relies heavily on ZMemory to access the internal Z-Machine data associated with these structures.  ZCallFrame is a class which represents a frame on the Z-Machine call stack.  Finally, as mentioned above, ZUserInterface is an interface which must be implemented by user interface code.

Full source code is included in the Zax distribution.  It is designed in such a way that enhancements could be made by subclassing existing code, rather than modifying it.


BUGS

I know of a few bugs and missing features in Zax.  These include:

o Does not implement transcript or command script I/O streams (this falls short of Nelson's v0.2 specification).
o Problems with window handling in the user interface make "Beyond Zork" unplayable.  Every other game I've tried has been fine, but then, I haven't tried everything that's out there.
o Sometimes when the Zax window goes out of focus and then comes back into focus, it will not accept keystrokes.  This seems to be a bug in the Java AWT; usually it can be fixed by clicking on the text window a few times.
o Calling the user interface "minimal" wuld be an understatement.  But it works.

If you find any bugs, I'd like to hear about them.  Please e-mail me with details; in particular, let me know which game the bug occurs in, what you do to cause the bug to happen, and, if possible, the version number of the game.


FUTURE PLANS:
To be clear: The 0.91 release is intended to put a version of Zax out there that compiles/runs on modern versions of Java, and to make it open source.  There are no other functional differences between 0.9 and 0.91.  The code is essentially the same, and could use plenty of refactoring, cleanup, and maybe even some unit tests.  Don't judge me too harshly--Zax was the first major program I wrote in Java and I've learned an awful lot in the 11 years since it was released.

Things have changed a lot in the IF and Z-code world since Zax was originally released in 1997.  Zax needs a lot of work to make it a "modern" Z-code interpreter.  It should be made Z-machine standard 1.0 compliant.  It needs Quetzal support.  It could use Blorb support.  It probably needs some modification to accommodate story files generated by Inform 7.


THE FINAL WORD

Zax was originally written with the noble intent of creating a truly cross-platform Z-machine.  I had a lot of plans for it, but life circumstances severely diminished the amount of free time I had to spend on the project, so it never developed past its original release.

So why am I re-releasing it 11 years later?  Mainly because I still see it mentioned on RAIF from time to time, and because the original license was pretty restrictive (written before I was an open-source convert!), and because there are faint indications out there that it might still be, in some way, useful.

I'd love to hear any comments, questions, bug reports, rants, harangues, compliments, complaints, limericks, etc.  Please feel free to send me e-mail; my e-mail address is matt at infinite-jest dot net.  There's no web "home" for Zax at the time, but for now, any new versions will certainly be uploaded to the IF-Archive.

-Matt Kimmel
August 25, 1997, and
September 14, 2008.
