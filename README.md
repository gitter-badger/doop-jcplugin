Doop Printer
-------------

1) create package
==============
mvn package

2) javac command
==============
javac -Xbootclasspath/p:./target/doop-printer-1.0-SNAPSHOT.jar -processorpath ./target/doop-printer-1.0-SNAPSHOT.jar -Xplugin:DoopPrinterPlugin ./src/test/java/*.java
