package dependency;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

                //SCOPE
                org.jdom2.Element scope = dependency.getChild("scope", namespace);
                if(scope != null && scope.getValue().equals("test"))
                    continue;

                //GROUP
                String group = null;
                org.jdom2.Element groupId = dependency.getChild("groupId", namespace);
                if(groupId != null)
                    group = groupId.getValue();


                //ARTIFACT
                String artifact = null;
                org.jdom2.Element artifactId = dependency.getChild("artifactId", namespace);
                if(artifactId != null)
                    artifact = artifactId.getValue();

                //VERSION
                String version = null;
                org.jdom2.Element versionId = dependency.getChild("version", namespace);
                if(versionId != null)
                    version = versionId.getValue();

                //SEARCH FOR VERSION IN PROPERTIES OR PARENT
                if(group != null && artifact != null && version != null && version.startsWith("$")) {
                    String tempVersion = version.substring(2, version.length() - 1);

                    if(tempVersion.equals("project.version")) {
                        org.jdom2.Element parent = document.getRootElement().getChild("parent", namespace);
                        if(parent != null) {
                            org.jdom2.Element element = parent.getChild("version", namespace);
                            if(element != null)
                                version = element.getValue();
                        }
                    }
                    else {
                        org.jdom2.Element properties = document.getRootElement().getChild("properties", namespace);
                        if(properties != null) {
                            org.jdom2.Element element = properties.getChild(tempVersion, namespace);
                            if(element != null)
                                version = element.getValue();
                        }
                    }
                }

                //SEARCH FOR VERSION IN ADDITIONAL LIBRARY
                if(group != null && artifact != null && version == null) {

                    //READ LIBRARY.CSV FROM RESOURCES
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(XMLParser.class.getClassLoader().getResourceAsStream("library.csv"), StandardCharsets.UTF_8));

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

                //ADD FOUND DEPENDENCY
                if(group != null && artifact != null && version != null)
                    dependenciesList.add(new Dependency(group, artifact, version));
            }

        }

        return dependenciesList;
    }

}
