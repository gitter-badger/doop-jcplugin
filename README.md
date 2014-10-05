Doop Printer
-------------
This projects aims to locate all the reference variables in a java source code and retrieve points-to information for these variables from the results of a doop framework analysis. Furthermore, it aims to locate all the method invocations in a java source code and retrieve all the possible method resolutions for such a method invocation, again from the results of an analysis performed using the doop framework.

1) create package
=====================
mvn package

2) javac command
=====================
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT.jar -Xplugin:DoopPrinterPlugin ./src/test/java/*.java

3) Useful links
====================
javac sources:
http://www.docjar.com/docs/api/com/sun/tools/javac/tree/JCTree.html
http://cr.openjdk.java.net/~forax/lambda/src/share/classes/com/sun/tools/javac/tree/JCTree.java.html

html templates:
http://velocity.apache.org/
http://velocity.apache.org/engine/releases/velocity-1.7/overview.html
