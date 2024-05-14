# Student Elegance Plug-In
_By Andrew Repine (repineap@rose-hulman.edu)_

This tool is meant as a plugin to be used with the Eclipse IDE in classrooms to teach students better programming practices by guiding them towards less complex solutions. I will dive into the interworkings and actual mechanics behind this, but I first want to give a guide for any users intending to program Eclipse plugins on their own, or build upon the one I have created.

## Plugin Development Basic User Guide
### Downloading the correct IDE
To install the tools needed for Eclipse plugin development, you must navigate to the **install new software** option in Eclipse, select from the dropdown **The Eclipse Project Updates**, then select **Eclipse Plugin Development Tools** and install. 

[Online Guide](https://medium.com/@ravi_theja/creating-your-first-eclipse-plugin-9b1b5ba33b58 "Eclipse Plugin Software Installation"): This guide can familiarize you with some of the systems and basic ideas of the plugin development workflow.
[Eclipse Documentation](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.pde.doc.user%2Fguide%2Ftools%2Feditors%2Fmanifest_editor%2Fdependencies.htm "Eclipse Docs"): Not the most useful, but the basic documentation that they provide.

### Dealing with the Elegance Plugin Project
#### Major Plugin Eclipse Dependencies
- [Javaparser](https://javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html "Javaparser Doc") [V 3.25.5]
  - Used to crawl the abstract syntax tree of the source code of files being analyzed to generate a score
- [org.eclipse.core.resources](https://help.eclipse.org/latest/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fcore%2Fresources%2Fpackage-summary.html "Resources Doc") [V 3.17.0]
  - DESCRIPTION
- [org.eclipse.ui](https://archive.eclipse.org/eclipse/downloads/documentation/2.0/html/plugins/org.eclipse.platform.doc.isv/reference/api/org/eclipse/ui/package-summary.html "UI Doc") [V 3.201.0]
  - DESCRIPTION
- [org.eclipse.core.runtime](https://help.eclipse.org/latest/nftopic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/core/runtime/package-summary.html "Runtime Doc") [V 3.25.0]
  - DESCRIPTION

#### Simple Description of Each Java File
- StartUpClass.java
  - In charge of registereing our _IResourceChangeListener_ with the workspace, to keep track of the changes to files to make updates when they are changed, runs on startup leading to the need for the _IStartup_ implementation.
- FileChangeListener.java
  - The _IResourceChangeListener_ that is registered, which detects when any _IResource_ changes, and in a higher functionality description it filters these events to make sure it is only code updates to .java files and then it runs the _FileAnalyzer_ on it.
- FileAnalyzer.java
  - **See Below**
- Method.java
  - Wrapper class used to store _MethodTree_ objects, used to create better code flow and separate out some functionality to simplify the code
- MethodTree.java
  - **See Below**
- DataSaver.java
  - Saves the complexity data to a predefined path _(src/TestResults/{functionName}-compdata.txt)_, used in the _FileAnalyzer_ to save off data for later analysis
- FileMarker.java
  - Used in the _FileAnalyzer_ to generate the underlines in the java files displayed on the user's screen

#### FileAnalyzer.java
- Reads in the complexity values to compare to [lines 97-140]
- Analyzes the code and compares it to the parsed complexity value [lines 142-157]
  - Computes the "complexity" score recursively based on a formula with modifiable parameters that are set using the config files that are read in
    - $L_{Cost}$: The cost associated with containing a **loop** construct (for loop, while loop, etc.)
    - $L_{Depth Cost}$: The multiplier applied to the complexity score of the code inside of the loop construct
    - $B_{Cost}$: The cost associated with containing a **branch** construct (if, else, etc.)
    - $B_{Depth Cost}$: The multiplier applied to the complexity score of the code inside of the branch construct
#### $C = \sum_{Branches}[B_{Cost} + B_{Depth Cost}*B_{Sub Cost}] + \sum_{Loops}[L_{Cost} + L_{Depth Cost}*L_{Sub Cost}]$

  - This score is then compared as a ratio of $\frac{User Cost}{Solution Cost}$ and that is the final complexity score _(Note: This computation is done in the MethodTree.java file)_

##### Configuration Files
These files follow the format of a line containing the word **params** followed by a the values for all of the parameters above and then subsequent lines have the format {MethodName}:{PreComputedMethodScore}

#### MethodTree.java
- This is where a majoirty of the Javaparser, abstract syntax tree, work is done in building a representation of the java file
- The representation takes the form of something that represents the file as a root which has the children being the constructs at the base level of the method
  - Each of these children then has the children of the constructs inside of them, and so on, forming a tree structure
- The parsing is done in the internal class _ControlFlowStatementVisitor_ which overrides the _VoidVisitorAdapter_ methods to specifically visit the constructs we want and generate the tree structure
  - Constructs that are not overwritten, by default, simply loop through and visit all of their children
- The internal class _StatementBlock_ is used to store the tree structure and calculate the complexity using the root with the _calculateComplexity_ method
