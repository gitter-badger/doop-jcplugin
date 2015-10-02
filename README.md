<h1>Doop Plugin for Javac</h1>
This projects aim to locate all the non-primitive variables in a java source code and retrieve points-to information for these variables from the results of an analysis performed using the Doop framework. Furthermore, it aims to locate all the method invocations in a java source code and retrieve all the possible method resolutions for such a method invocation, again from the results of an analysis performed using the doop framework.

<h3>create package</h3>
mvn package

<h3>javac command</h3>
execute:
javac -cp ../advancedTest/ -Xbootclasspath/p:./target/doop-jcplugin-1.0-SNAPSHOT.jar -processorpath ./target/doop-jcplugin-1.0-SNAPSHOT-jar-with-dependencies.jar -Xplugin:TypeInfoPlugin  ../advancedTest/src/main/java/advancedTest/Main.java ../advancedTest/src/main/java/extras/*.java

<h3>useful links</h3>
javac sources:
http://www.docjar.com/docs/api/com/sun/tools/javac/tree/JCTree.html
http://cr.openjdk.java.net/~forax/lambda/src/share/classes/com/sun/tools/javac/tree/JCTree.java.html
