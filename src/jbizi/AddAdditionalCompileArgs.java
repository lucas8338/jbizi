package jbizi;

import com.esotericsoftware.yamlbeans.YamlException;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * class to add data from additionalCompileArgs in 'projectDescription.yaml'.
 */
public class AddAdditionalCompileArgs {
    public List<String> target;

    /**
     * add fields for additional compile args of the 'projectDescription.yaml' file to the desired list.
     *
     * @param target: who to add the fields.
     */
    public AddAdditionalCompileArgs(List<String> target) {
        this.target = target;
    }

    public void add() throws YamlException, FileNotFoundException {
        @SuppressWarnings("unchecked")
        Map<String, String> additionalCompileArgs = (Map<String, String>) Util.readYaml("./projectDescription.yaml").get("additionalCompileArgs");

        if (additionalCompileArgs != null) {
            for (String key : additionalCompileArgs.keySet()) {
                target.add(key);
                if (!additionalCompileArgs.get(key).equals("")) {
                    target.add(additionalCompileArgs.get(key));
                }
            }
        }
    }
}
