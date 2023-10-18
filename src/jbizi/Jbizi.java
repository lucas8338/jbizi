package jbizi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;
import org.testng.Assert;
import picocli.CommandLine;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@CommandLine.Command(
        name = "jbizi",
        mixinStandardHelpOptions = true,
        description =
                """
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
                            |---testsResources   -> directory to store tests data.
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
                        additionalClassPaths   -> a list with additional class path to add to the compilation command.
                                        
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
                             
                        """
)
public class Jbizi {
    @CommandLine.Option(names = {"--verbose", "-v"}, arity = "0", description = "enable debug outputs.")
    public boolean verbose;

    public static void main(String... args) {
        CommandLine cl = new CommandLine(new jbizi.Jbizi());
        cl.setUsageHelpAutoWidth(true);
        int exitCode = cl.execute(args);
        System.exit(exitCode);
    }

    public Log getLogger() {
        SimpleLog logger = new SimpleLog("jbizi");
        if (this.verbose) logger.setLevel(SimpleLog.LOG_LEVEL_ALL);
        return logger;
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "initialize the project structure.")
    public void init(@CommandLine.Option(names = {"--silent", "-s"}, arity = "0", description = "set execution mode to silent.") boolean silent) throws IOException {
        if (!silent) {
            this.getLogger().info("executing init...");
        }

        if (!silent) {
            this.getLogger().info("creating directories");
        }
        new File("./src").mkdir();
        new File("./lib").mkdir();
        new File("./resources").mkdir();
        new File("./srcOut").mkdir();
        new File("./srcOut/compile").mkdir();
        new File("./srcOut/artifact").mkdir();
        new File("./srcOut/javadoc").mkdir();
        new File("./srcOut/etc").mkdir();
        new File("./tests").mkdir();
        new File("./tests/tests").mkdir();
        new File("./testsOut").mkdir();
        new File("./testsOut/compile").mkdir();
        new File("./testsOut/artifact").mkdir();
        new File("./testsOut/etc").mkdir();
        new File("./etc").mkdir();
        new File("./etc/testsResources").mkdir();

        if (!silent) {
            this.getLogger().info("creating project files");
        }
        if (!new File("./projectDescription.yaml").exists()) {
            InputStream projectDescriptionInStream = ClassLoader.getSystemResourceAsStream("defaultStructure/projectDescription.yaml");
            try (FileOutputStream projectDescriptionOutStream = new FileOutputStream(new File("./projectDescription.yaml"))) {
                projectDescriptionOutStream.write(projectDescriptionInStream.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!silent){
            this.getLogger().info("copying default libs (will not overwrite).");
        }

        new CopyFromClassPath("defaultLibs/testng-7.8.0-withDependencies.jar", "./lib").copy();

        new CopyFromClassPath("defaultLibs/testng-7.8.0-javadoc.jar", "./lib").copy();

        if (!silent){
            this.getLogger().info("copying the .gitignore (will not overwrite).");
        }

        new CopyFromClassPath("defaultGit/.gitignore", ".").copy();

        if (!silent) {
            this.getLogger().info("init done.");
        }
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "remove all files in the 'outputs' directories.")
    public void clean() throws IOException {
        this.getLogger().info("executing clean...");

        FileUtils.forceDelete(new File("./srcOut"));
        FileUtils.forceDelete(new File("./testsOut"));

        this.init(true);

        this.getLogger().info("clean done.");
    }

    @CommandLine.Command
    public void compile() throws IOException, InterruptedException {

        List<String> compileCommand = new ArrayList<String>();
        compileCommand.addAll(
                Arrays.asList(
                        "javac", "-d", "./srcOut/compile", "--class-path"
                )
        );

        compileCommand.add(new CPPaths().main());

        new AddAdditionalCompileArgs(compileCommand).add();

        List<File> filesToCompile = FileUtils.listFiles(new File("./src"), new String[]{"java"}, true).stream().toList();

        compileCommand.addAll(filesToCompile.stream().map(x -> x.getAbsolutePath()).toList());

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(compileCommand);

        this.getLogger().debug(
                String.format(
                        "compiling src directory with the process builder command: %s",
                        pb.command()
                )
        );

        Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "the compile command has returned an error");

        this.getLogger().info("compile done.");
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "generate a jar file without or with dependencies.")
    public void jar(
            @CommandLine.Option(names = {"--withDependencies"}, arity = "0")
            boolean withDependencies
    ) throws IOException, InterruptedException {
        this.getLogger().info(withDependencies ? "executing fatJar..." : "executing jar...");

        File tmpDir = new File(
                String.format(
                        "./tmpDir-jar-%d",
                        (int) (Math.random() * 1000)
                )
        );

        this.getLogger().debug("create temporary directories.");

        Assert.assertTrue(tmpDir.mkdir(), "was not possible to create the temporary dir for jar creation");

        File tmpDirAssemply = new File(tmpDir.getAbsolutePath() + "/assembly");
        Assert.assertTrue(tmpDirAssemply.mkdir(), "was not possible to create the assembly subdirectory in the tmpDir.");

        // block only required if you want to add dependencies.
        if (withDependencies) {
            File tmpDirLib = new File(tmpDir.getAbsolutePath() + "/lib");
            Assert.assertTrue(tmpDirLib.mkdir(), "was not possible to create the lib subdirectory in the tmpDir.");

            this.getLogger().debug("copy ./lib files to the tmpDir/lib");

            FileUtils.copyDirectory(new File("./lib"), tmpDirLib);

            this.getLogger().debug("extract jar files then delete their META-INF.");
            for (File file : tmpDirLib.listFiles()) {
                // if contains the word 'javadoc' delete that file, don't matter the file type.
                if (file.getName().contains("javadoc")) {
                    FileUtils.forceDelete(file);
                    continue;
                }
                if (file.getName().endsWith(".jar")) {
                    ProcessBuilder pb = new ProcessBuilder();
                    pb.directory(tmpDirLib);
                    pb.command(Arrays.asList("jar", "-xf", file.getName()));
                    Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "jar extract command has returned exit code different than 0.");
                    this.getLogger().debug("delete the original jar file.");
                    Assert.assertTrue(file.delete(), "the jar file was not possible to be deleted.");
                    this.getLogger().debug("delete the META-INF directory. if exits.");
                    File metaInf = new File(tmpDirLib.getAbsolutePath() + "/META-INF");
                    // delete the META-INF directory if it exits.
                    if (metaInf.exists()) {
                        FileUtils.forceDelete(metaInf);
                    }
                }
            }

            Assert.assertFalse(new File(tmpDirLib.getAbsolutePath() + "/META-INF").exists(), "seems the 'META-INF' directory was not remove from the tmpDir/lib.");

            this.getLogger().debug("copy the tempDir/lib to tempDir/assembly directory");
            FileUtils.copyDirectory(tmpDirLib, tmpDirAssemply);
        }

        this.getLogger().debug("copy the resources directory content to the tempDir/assembly");
        FileUtils.copyDirectory(new File("./resources"), tmpDirAssemply);

        this.getLogger().debug("copy the srcOut/compile directory to the tempDir/assembly");
        FileUtils.copyDirectory(new File("./srcOut/compile"), tmpDirAssemply);

        this.getLogger().debug("generate the metafile data");
        String metafileLocation = tmpDir.getAbsolutePath() + "/metafile.txt";
        new GenerateMetafileMCompliant(metafileLocation).generate();

        Map<String, Object> projectDescription = Util.readYaml("./projectDescription.yaml");

        List<String> jarBuildCommand = new ArrayList<String>();
        jarBuildCommand.addAll(
                Arrays.asList(
                        "jar",
                        "-cmf",
                        metafileLocation,
                        String.format(
                                "%s/%s-%s%s",
                                new File("./srcOut/artifact").getAbsolutePath(),
                                projectDescription.get("name").toString(),
                                projectDescription.get("version").toString(),
                                withDependencies ? "-withDependencies.jar" : ".jar"
                        ),
                        "./*"
                )
        );

        ProcessBuilder pb = new ProcessBuilder(jarBuildCommand);
        pb.directory(tmpDirAssemply);

        this.getLogger().debug(
                String.format("generating jar file with the process builder command (working dir at: '%s'): '%s'.",
                        pb.directory().getPath(),
                        pb.command()
                )
        );

        Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "error while generating the jar file.");

        FileUtils.forceDeleteOnExit(tmpDir);

        this.getLogger().info(withDependencies ? "fatJar done." : "jar done.");
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "generate a jar file with dependencies.")
    public void fatJar() throws IOException, InterruptedException {
        this.jar(true);
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "generate the javadoc (at directory and jar file).")
    public void javadoc() throws IOException, InterruptedException {
        this.getLogger().info("executing javadoc...");

        File tmpDir = new File(
                String.format(
                        "tmpDir-javadoc-%d",
                        (int) (Math.random() * 1000)
                )
        );

        File tmpDirAssembly = new File(tmpDir.getAbsolutePath() + "/assembly");

        List<String> javadocCommand = new ArrayList<String>(
                Arrays.asList(
                        "javadoc",
                        "-d",
                        "./srcOut/javadoc",
                        "--class-path"
                )
        );

        javadocCommand.add(new CPPaths().main());

        List<String> javaFilesInSrc = FileUtils.listFiles(new File("./src"), new String[]{"java"}, true).stream().map(x -> x.getAbsolutePath()).toList();

        javadocCommand.addAll(javaFilesInSrc);

        ProcessBuilder pb = new ProcessBuilder(javadocCommand);

        this.getLogger().debug(
                String.format("executing process build command to generate javadoc: '%s'.", pb.command())
        );

        Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "javadoc generation command has returned exit code different than 0.");

        this.getLogger().debug("copy the javadoc out to the tmpDir/assembly directory");
        FileUtils.copyDirectory(new File("./srcOut/javadoc"), tmpDirAssembly);

        Map<String, Object> projectDescription = Util.readYaml("./projectDescription.yaml");

        File manifestLocation = new File(tmpDir.getAbsolutePath() + "/manifest.txt");

        new GenerateMetafileMCompliant(manifestLocation.getAbsolutePath()).generate();

        pb.directory(tmpDirAssembly);

        pb.command(
                Arrays.asList(
                        "jar",
                        "-cmf",
                        manifestLocation.getAbsolutePath(),
                        String.format(
                                "%s/%s-%s-javadoc.jar",
                                new File("./srcOut/artifact").getAbsolutePath(),
                                projectDescription.get("name").toString(),
                                projectDescription.get("version").toString()
                        ),
                        "./*"
                )
        );

        Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "javadoc jar creation has returned exit code different of 0.");

        FileUtils.forceDeleteOnExit(tmpDir);

        this.getLogger().info("javadoc done.");
    }

    @CommandLine.Command(
            mixinStandardHelpOptions = true,
            description = "clean, compile, test, generate artifacts, when you call 'build' this mean your project is done and you are ready to distribute the project."
    )
    public void build(@CommandLine.Option(names = {"--skipTests", "-t"}, arity = "0") boolean skipTests) throws Exception {
        this.getLogger().info("executing build...");

        this.clean();
        if (!skipTests) {
            this.test();
        }
        this.compile();
        this.jar(false);
        this.fatJar();
        this.javadoc();

        this.getLogger().info("build done.");
    }

    @CommandLine.Command(mixinStandardHelpOptions = true, description = "run tests.")
    public void test() throws Exception {
        List<String> commandCompileTests = new ArrayList<>(
                Arrays.asList("javac", "-d", "./testsOut/compile", "--class-path")
        );

        commandCompileTests.add(new CPPaths().tests());

        new AddAdditionalCompileArgs(commandCompileTests).add();

        List<String> testsFilesToCompile = FileUtils.listFiles(new File("./tests"), new String[]{"java"}, true).stream().map(x -> x.getAbsolutePath()).toList();

        if ( testsFilesToCompile.size() == 0 ){
            this.getLogger().info("there not tests to be compiled, so SKIPPING TESTS.");
            return;
        }

        commandCompileTests.addAll(testsFilesToCompile);

        ProcessBuilder pb = new ProcessBuilder(commandCompileTests);

        this.getLogger().debug(
                String.format("compiling tests with the process builder command: '%s'.", pb.command())
        );

        Assert.assertEquals(pb.inheritIO().start().waitFor(), 0, "command to compile the './tests/**/*.java' has returned exit code different of 0");

        new TestsRunner("./testsOut/compile").run();

        this.getLogger().info("tests done.");
    }
}