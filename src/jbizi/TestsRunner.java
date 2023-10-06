package jbizi;

import org.apache.commons.io.FileUtils;
import org.testng.TestNG;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
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

        // class loader to be the parent
        URLClassLoader parentCl = new URLClassLoader(new URL[]{new File(this.dir).getAbsoluteFile().toURI().toURL()}, this.getClass().getClassLoader());

        // add the 'this.dir' to the class path.
        URLClassLoader ucl = new URLClassLoader(new URL[]{new File(this.dir).getAbsoluteFile().toURI().toURL()}, parentCl);

        /*
        * above was required a UrlClassLoader as parent of another UrlClass loader because the 'UrlClassLoader' doesn't
        * work recursively, example, if a class loaded by a UrlClassLoader has a requirement that is even inside the
        * UrlClassLoader path but that required class will not be found here is the reason:
        * example (read line by line each line is a step):
        * ClassLoad loads url: './testsOut/compile'
        * ClassLoad loads class: 'tests.Test1'
        * 'tests.Test1' ask for a class named 'main.Calculate'  //this will cause an error, because the class load cant find 'test.Test1' even if that class is inside the url given to the class loader.
        *                                                         to solve this problem is needed to use a class loader which contains that url as parent of the class loader which will load the class.
        *                                                         the class load which is loading the class will ask by the desired class to the parent class load that now can load that class successful.
        *                                                         and look out parent class load also has a parent, the class loader of this class, this way the parent class loader can access the 'systemClassLoader' too.
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