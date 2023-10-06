package jbizi;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.testng.Assert;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class Util {
    public static Map<String, Object> readYaml(String path) throws FileNotFoundException, YamlException {
        File pathFile = new File(path).getAbsoluteFile();

        Assert.assertTrue(pathFile.exists(), "your path you provided doest exist.");

        YamlReader yreader = new YamlReader(new FileReader(pathFile));
        return (Map<String, Object>) yreader.read(Map.class);
    }

    public static void writeYaml(Map<String, Object> data, String path) throws IOException {
        File pathFile = new File(path);

        YamlConfig yconfig = new YamlConfig();
        yconfig.writeConfig.setWriteRootTags(false);
        yconfig.writeConfig.setWriteRootElementTags(false);
        YamlWriter ywriter = new YamlWriter(new FileWriter(pathFile), yconfig);
        ywriter.write(data);
        ywriter.close();
    }
}
