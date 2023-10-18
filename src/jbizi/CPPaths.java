package jbizi;

import com.esotericsoftware.yamlbeans.YamlException;

import java.io.FileNotFoundException;
import java.util.*;

/**
 * class with tools to return a java classPath string compliant.
 * */
public class CPPaths {

    /**
     * the default configuration when compiling the src.
     * */
    public String[] defaultMain = {"./src", "./lib", "./lib/*", "./resources"};

    /**
     * the default class path configuration when compiling for testing.
     * */
    public String[] defaultTests = {"./tests", "./src", "./lib", "./lib/*", "./resources"};

    /**
     * the result, this is the variable which will be modified then returned by the methods.
     * */
    public String classPath = "";

    /**
     * add the additional class paths of the projectDescription key 'additionalClassPaths' to the classPath parameter.
     * */
    public void addAdditional() throws YamlException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        String additionalClassPathsKeyName = "additionalClassPaths";

        Map<String, Object> projectDescription = Util.readYaml("./projectDescription.yaml");
        if ( projectDescription.get(additionalClassPathsKeyName) != null ){
            for (String value: (List<String>) projectDescription.get(additionalClassPathsKeyName)){
                if ( sb.length() > 0 ){
                    sb.append(";");
                }

                sb.append(value);
            }
        }

        if ( this.classPath.length() > 0 ){
            this.classPath = this.classPath + ";";
        }

        this.classPath = this.classPath + sb.toString();
    }

    /**
     * the class path to be added to a normal compilation (src compilation).
     * */
    public String main() throws YamlException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for ( String path: this.defaultMain ){
            if ( sb.length() > 0 ){
                sb.append(";");
            }
            sb.append(path);
        }
        if ( this.classPath.length() > 0 ){
            this.classPath = this.classPath + ";";
        }
        this.classPath = this.classPath + sb.toString();
        this.addAdditional();
        return this.classPath;
    }

    /**
     * the class path string to be added to tests compilation.
     * */
    public String tests() throws YamlException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        for ( String path: this.defaultTests ){
            if ( sb.length() > 0 ){
                sb.append(";");
            }
            sb.append(path);
        }
        if ( this.classPath.length() > 0 ){
            this.classPath = this.classPath + ";";
        }
        this.classPath = this.classPath + sb.toString();
        this.addAdditional();
        return this.classPath;
    }
}
