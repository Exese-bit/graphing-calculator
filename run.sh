#!/bin/bash 

javac -d bin GraphingCalculator/src/jonah/*.java 

java -Dawt.useSystemAAFontSettings=on -Dswing.aatext==true -cp bin GraphingCalculator/src/jonah/App.java
