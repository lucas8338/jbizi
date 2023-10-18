package jbizi;

import org.apache.commons.io.FileUtils;
import org.testng.TestNG;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class to run tests from the compiled tests.
 * */
public class TestsRunner {
    /**
     * should to stop the tests when a failure happened
     */
    public boolean stopOnFailure = true;
    String dir;
    String testOutputDir = "./testsOut/etc/testng" + "_" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));

    /**
     * @param dir: the root directory of the compiled tests project.
     */
    public TestsRunner(String dir) {
        this.dir = dir;
    }

    public void run(String... args) throws Exception {

        Pattern classNameExtract = Pattern.compile("^(?<className>.*?)(\\$|.class)");

        List<Class<?>> testClasses = new ArrayList<Class<?>>();

        // contains the 'lib' directory and each jar in the './lib' direcotyr.
        List<File> lib = new ArrayList<File>();
        lib.add(new File("./lib"));
        lib.addAll(FileUtils.listFiles(new File("./lib"), new String[]{"jar"}, false));

        File targetRoot = new File(this.dir);

        List<URL> urls = new ArrayList<URL>();

        for (File file : lib) {
            urls.add(file.toURI().toURL());
        }

        urls.add(targetRoot.toURI().toURL());

        // add the additionalClassPaths project description paths to the 'url' variable.

        CPPaths cppPaths = new CPPaths();

        // to get the paths added this will get the difference between the 'defaultTests' parameter variable
        // of the CPPaths class and the path with added class paths.

        String[] standard = cppPaths.defaultTests;

        String[] afterAddition = cppPaths.tests().split(";");

        List<String> added = new ArrayList<String>();

        for ( String path: afterAddition ){
            if ( !new ArrayList<String>(List.of(standard)).contains(path) ){
                added.add(path);
            }
        }

        for ( String path: added ){
            if ( path.endsWith("*") ){
                // will select '-2' to avoid the '*' character.
                File file = new File(path.substring(0, path.length() - 2));
                List<File> files = FileUtils.listFiles(file, new String[]{"jar"}, false).stream().toList();
                for ( File jar: files ){
                    urls.add(jar.toURI().toURL());
                }
            }else{
                File file = new File(path);
                urls.add(file.toURI().toURL());
            }
        }

        // the parent class loader where the dependencies will look for dependencies.
        URLClassLoader purl = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());

        // the main loader used to load classes.
        URLClassLoader ucl = new URLClassLoader(urls.toArray(new URL[0]), purl);

        /*
         * above was required a UrlClassLoader as parent of another UrlClass loader because the 'UrlClassLoader' doesn't
         * work recursively, example, if a class loaded by a UrlClassLoader has a requirement that is even inside the
         * UrlClassLoader path but that required class will not be found here is the reason:
         * example (read line by line each line is a step):
         * ClassLoad loads url: './testsOut/compile'
         * ClassLoad loads class: 'tests.Test1'
         * 'tests.Test1' ask for a class named 'main.Calculate'  //'main.Calculate' is located inside './testsOut/compile'
         *                                                         this will cause an error, because the class load cant find 'main.Calculate' even if that class is inside the url given to the class loader.
         *                                                         to solve this problem is needed to use a class loader which contains that url as parent of the class loader which will load the class.
         *                                                         because dependencies are all searched in the parent classloader.
         * */

        // get all files in the 'this.dir/tests' will apply a regex to extract the class name.
        for (File file : FileUtils.listFiles(new File(this.dir + "/tests"), null, false)) {
            Matcher m = classNameExtract.matcher(file.getName());
            if (m.matches()) {
                String className = m.group("className");
                testClasses.add(ucl.loadClass("tests." + className));
            }
        }

        ucl.close();

        TestNG testng = new TestNG();
        testng.setOutputDirectory(this.testOutputDir);
        testng.setTestClasses(testClasses.toArray(new Class<?>[0]));
        testng.run();

        // stop tests if there a failure
        if (this.stopOnFailure) {
            if (testng.hasFailure()) {
                System.out.printf(
						"[TEST] was there failure in tests. check the tests output at: '%s'.%n",
						testng.getOutputDirectory()
				);
                System.exit(1);
            }
        }
    }
}