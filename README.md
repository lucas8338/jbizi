## jbizi - a directory based easy to use build tool for java.

```
[DIRECTORY STRUCTURE]:
src                      -> store the main code of the project.
lib                      -> store the libraries used by the project.
resources                -> store the resources (e.g. general files) used by the main code of the project.
                            the content of this directory will be added to the root of the generated artifacts.
srcOut
    |---compile          -> store the output of the compilation of the 'src' directory.
    |---artifact         -> store the output of the artifact generation of the 'src' directory.
    |---javadoc          -> store the output of the javadoc generation of the 'src' directory.
    |---etc              -> general purpose directory related to 'src' output.
tests
    |---tests            -> a package, store the tests files, tests to be executed.
testsOut
    |---compile          -> store the output of the compilation of the 'tests' directory.
    |---artifact         -> store the output of the artifact generation of the 'tests' directory.
    |---etc              -> general purpose directory related to 'tests' output.
etc                      -> general purpose directory.
projectDescription.yaml  -> a yaml file describing the project information and configuration.

[COMPILATION CLASS-PATH STRUCTURE]:
src   -> src, lib, lib/*, resources
tests -> tests, src, lib, lib/*, resources

['projectDescription.yaml' FIELDS]:
name                   -> string, the name of the project, will be used to generate the jar files.
version                -> string, the version of the project, will be added to the generated jar files names and manifest file.
mainClass              -> string, the complete name (package and class) of the class with the main method, if dont there a main method just let it as a empty string.
                          this will be added to the generated jars manifest file.
description            -> string, short description of the project. will be added to the generated jars manifest file.
author                 -> string, the author name (person / group), will be added to the generated jars manifest file.
url                    -> string, how to contact the author. will be added to the generated jars manifest file.
additionalCompileArgs  -> key and value, additional parameters to be passed to the 'javac' command. this must be a yaml key value (sub-yaml).
                          the 'key' is the parameter name e.g. ''-target'' (in yaml '-' is a designator for list, so you need to enclose the key in parenthesis
                          if the parameter starts with '-'). if the parameter doest have a value e.g. '-verbose', the key value must be an empty string.

[ARTIFACTS NAMES LOGIC]:
jar      -> {name}-{version}.jar
fatJar   -> {name}-{version}-withDependencies.jar
javadoc  -> {name}-{version}-javadoc.jar

[TODO]:
hash:
    hash the src directory, this will avoid generating artifacts to old compiled code. the build tool would store the src hash at moment the code was compiled
    then when generating new artifacts that hash will be checked. if the src has changed then run 'clean' and 'compile' again.

prePost:
    run pre-task scripts, this was already implemented in private version of jbizi, but was 'deleted'.
    the logic is to run java or shell commands from a 'prePost' subdirectories example: './prePost/pre-compile/MyCode.java' or './prePost/post-compile/myCode-run.txt'.

maven:
    the ability of download things from maven repository solving dependencies and with user configuration download or not optional dependencies.

jpackage:
    generate a java package using the 'jpackage' tool. which can generate a installer.

pretty cmd output:
    now the cmd interface is very ugly. would be good a pretty output.


  -h, --help      Show this help message and exit.
  -v, --verbose   enable debug outputs.
  -V, --version   Print version information and exit.
Commands:
  build    clean, compile, test, generate artifacts, when you call 'build' this mean your project is done and you are ready to distribute the project.
  clean    remove all files in the 'outputs' directories.
  compile
  fatJar   generate a jar file with dependencies.
  init     initialize the project structure.
  jar      generate a jar file without or with dependencies.
  javadoc  generate the javadoc (at directory and jar file).
  test     run tests.
```

## Commands
- **build**: clean, compile, test, generate artifacts, when you call 'build' this mean your project is done and you are ready to distribute the project.
 - **clean**: remove all files in the 'outputs' directories.
  - **compile**
  - **fatJar**: generate a jar file with dependencies.
  - **init**: initialize the project structure.
  - **jar**: generate a jar file without or with dependencies.
  - **javadoc**: generate the javadoc (at directory and jar file).
  - **test**: run tests.

## Usage
place the executable jar in a directory then run:
`java -jar {name of .jar} init`
this will create a default structure, you can read about each directory above.
place some code in the `/src` directory then call:
`java -jar {name of .jar} build`
this will compile the the files then will generate: "a .jar, a executable jar and a javadoc jar". three artifacts in the `/srcOut/artifact` directory.

the executable of this project project is a bit big. so you can use this of two ways.

 - with the executable in the project root.
 - in any directory with a bash script (.bat, .cmd or .sh) with the command to run the .jar (must capture the arguments) and their directory added to the system path.
working example:
   - "**jbizi.cmd**" at `C:/jbizi` directory:
		```
		java -jar C:/jbizi/jbizi-1.0.0.0-withDependencies.jar %*
		```
   - "**jbizi.sh**" at `C:/jbizi` directory (tested with msys2):
		```
		#!/bin/bash
		java -jar C:/jbizi/jbizi-1.0.0.0-withDependencies.jar $@
		```

## Examples
there examples in the `etc/examples` in there, there working examples
and intentional not working examples, this is because this project uses for hour a manual testing to test the build tool.
