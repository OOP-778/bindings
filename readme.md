# Bindings
![Maven Central](https://img.shields.io/maven-central/v/dev.oop778/bindings)
![Static Badge](https://img.shields.io/badge/java_version-8--latest-brightgreen)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/OOP-778/bindings)


This library gives you the flexibility for managing lifecycle of your objects, you're able to bind objects to another objects, control the order of closing, if there's memory leaks you can find the source of them very quickly using built in dumping tool, which produces a graph of all unclosed bindable instances.

## See in action
You can pull the project, and run the `src/test/oop778/binding/test/TestLive.java` after successful run you should see `dump.html` in the 
project directory. You can open it in any browser.

# Dump Result by running `TestLive` file
![Graph](img/graph_example.png)

If you want to see information about specific node, you can just click on it and it'll show you the class name of the Bindable and stack 
trace where it was created.
![Popup](img/popup.png)