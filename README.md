<h1>Doop Printer</h1>
This projects aims to locate all the non-primitive variables in a java source code and retrieve points-to information for these variables from the results of a doop framework analysis. Furthermore, it aims to locate all the method invocations in a java source code and retrieve all the possible method resolutions for such a method invocation, again from the results of an analysis performed using the doop framework.

<h3>create package</h3>
mvn package

<h3>javac command</h3>
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT.jar -Xplugin:DoopPrinterPlugin ./src/test/resources/advancedTest/myTests/*.java

<h3>useful links</h3>
javac sources:
http://www.docjar.com/docs/api/com/sun/tools/javac/tree/JCTree.html
http://cr.openjdk.java.net/~forax/lambda/src/share/classes/com/sun/tools/javac/tree/JCTree.java.html

html templates:
http://velocity.apache.org/
http://velocity.apache.org/engine/releases/velocity-1.7/overview.html



TODO: 
<ul>
<li>Representation of Doop entities in Java (doop-api: https://github.com/plast-lab/doop-api).</li>
<li>Correct handling of packages when generating fully qualified names for nested types. - Almost done</li>
<li>Test with doop exclude implicit reachable flag.</li>
<li>Check spring framework</li>
</ul>


