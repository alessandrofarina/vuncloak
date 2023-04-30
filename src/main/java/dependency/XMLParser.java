package dependency;

import dependency.Dependency;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XMLParser {
    public static Collection<Dependency> getDependencies(String filename) throws IOException, JDOMException {
        ArrayList<Dependency> dependenciesList = new ArrayList<>();

        Document document = new SAXBuilder().build(filename);
        Namespace namespace = document.getRootElement().getNamespace();

        if(document.hasRootElement() && document.getRootElement().getChild("dependencies", namespace) != null) {
            List<Element> dependencies = document.getRootElement().getChild("dependencies", namespace).getChildren();

            for(Element dependency: dependencies) {

                String group = null;
                org.jdom2.Element groupId = dependency.getChild("groupId", namespace);
                if(groupId != null)
                    group = groupId.getValue();

                String artifact = null;
                org.jdom2.Element artifactId = dependency.getChild("artifactId", namespace);
                if(artifactId != null)
                    artifact = artifactId.getValue();

                String version = null;
                org.jdom2.Element versionId = dependency.getChild("versionId", namespace);
                if(versionId != null)
                    version = versionId.getValue();

                if(group != null && artifact != null && version == null) {

                    BufferedReader bufferedReader = new BufferedReader(new FileReader("library.csv"));
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        String[] strings = line.split(",");
                        if(strings[0].equals(group) && strings[1].equals(artifact)) {
                            version = strings[2];
                            break;
                        }
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                }

                if(group != null && artifact != null && version != null)
                    dependenciesList.add(new Dependency(group, artifact, version));
            }

        }

        return dependenciesList;
    }

}
