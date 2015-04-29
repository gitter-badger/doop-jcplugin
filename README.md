<h1>Doop Printer</h1>
This projects aims to locate all the non-primitive variables in a java source code and retrieve points-to information for these variables from the results of a doop framework analysis. Furthermore, it aims to locate all the method invocations in a java source code and retrieve all the possible method resolutions for such a method invocation, again from the results of an analysis performed using the doop framework.

<h3>create package</h3>
mvn package

<h3>javac command</h3>
execute:
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT.jar -Xplugin:DoopPrinterPlugin ../advancedTest/Main.java ../advancedTest/extras/*.java
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT-jar-with-dependencies.jar -Xplugin:DoopPrinterPlugin ../advancedTest/Main.java ../advancedTest/extras/*.java

<h3>useful links</h3>
javac sources:
http://www.docjar.com/docs/api/com/sun/tools/javac/tree/JCTree.html
http://cr.openjdk.java.net/~forax/lambda/src/share/classes/com/sun/tools/javac/tree/JCTree.java.html
