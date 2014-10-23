<h1>Doop Printer</h1>
This projects aims to locate all the non-primitive variables in a java source code and retrieve points-to information for these variables from the results of a doop framework analysis. Furthermore, it aims to locate all the method invocations in a java source code and retrieve all the possible method resolutions for such a method invocation, again from the results of an analysis performed using the doop framework.

<h3>create package</h3>
mvn package

<h3>javac command</h3>
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT.jar -Xplugin:DoopPrinterPlugin ./src/test/java/*.java

<h3>useful links</h3>
javac sources:
http://www.docjar.com/docs/api/com/sun/tools/javac/tree/JCTree.html
http://cr.openjdk.java.net/~forax/lambda/src/share/classes/com/sun/tools/javac/tree/JCTree.java.html

html templates:
http://velocity.apache.org/
http://velocity.apache.org/engine/releases/velocity-1.7/overview.html

Question: Distinguish namesake variables by line, column number? Is there a point? 

TODO: Doop DAOs, connect to database and retrieve doop local variable objects.
