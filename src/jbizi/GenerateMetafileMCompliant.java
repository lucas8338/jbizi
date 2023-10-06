package jbizi;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * generate a Metafile at destination which is compliant with the '-m' parameter of java jar tool.
 */
public class GenerateMetafileMCompliant {
    String destination;

    public GenerateMetafileMCompliant(String destination) {
        this.destination = destination;
    }

    public void generate() throws IOException {
        Map<String, Object> projectDescription = Util.readYaml("./projectDescription.yaml");

        // generate the metafile fields as required by java documentation.
        Map<String, Object> metafileCompliant = new LinkedHashMap<String, Object>();
        if (!projectDescription.get("mainClass").toString().equals("")) {
            metafileCompliant.put("Main-Class", projectDescription.get("mainClass").toString());
        }
        metafileCompliant.put("Name", projectDescription.get("name").toString());
        metafileCompliant.put("Specification-Title", projectDescription.get("description").toString());
        metafileCompliant.put("Specification-Version", projectDescription.get("version").toString());
        metafileCompliant.put("Specification-Vendor", projectDescription.get("author").toString());
        metafileCompliant.put("Implementation-URL", projectDescription.get("url").toString());

        Util.writeYaml(metafileCompliant, this.destination);
    }
}
