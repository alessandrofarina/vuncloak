package dependency;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import utils.GitManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XMLParser {

    private static final String LIBRARY = "library.csv";

    private static Document document;
    private static Namespace namespace;

    public static Collection<Dependency> getPOMDependencies() throws IOException, JDOMException {
        ArrayList<Dependency> list = new ArrayList<>();

        document = new SAXBuilder().build(GitManager.getPOMPath());
        namespace = document.getRootElement().getNamespace();
        list.addAll(_getPOMDependencies(getChild(document.getRootElement(), "dependencies")));
        list.addAll(_getPOMDependencies(getChild(getChild(document.getRootElement(), "dependencyManagement"), "dependencies")));

        return list;
    }


    private static Collection<Dependency> _getPOMDependencies(Element start) throws IOException, JDOMException {
        ArrayList<Dependency> list = new ArrayList<>();

        if(start != null) {
            List<Element> dependencies = start.getChildren();

            for(Element dependency: dependencies) {

                //SCOPE
                String scope = getValue(getChild(dependency, "scope"));
                if(scope != null && scope.equals("test"))
                    continue;

                //GROUP - ARTIFACT - VERSION
                String group = getValue(getChild(dependency, "groupId"));
                String artifact = getValue(getChild(dependency, "artifactId"));
                String version = getValue(getChild(dependency, "version"));

                //SEARCH FOR VERSION IN PARENT OR PROPERTIES
                if(version != null && version.startsWith("$"))
                    version = searchForVersionPOM(version);

                //SEARCH FOR VERSION IN ADDITIONAL LIBRARY
                if(version == null)
                    version = searchForVersionLibrary(group, artifact);

                //ADD FOUND DEPENDENCY
                if(version != null)
                    list.add(new Dependency(group, artifact, version));
            }

        }

        return list;
    }

    private static String searchForVersionPOM(String version) {
        String temp = version.substring(2, version.length() - 1);
        if(temp.equals("project.version"))
            return getValue(getChild(getChild(document.getRootElement(), "parent"), "version"));
        else
            return getValue(getChild(getChild(document.getRootElement(), "properties"), temp));
    }

    private static String searchForVersionLibrary(String group, String artifact) throws IOException {
        String version = null;

        //READ LIBRARY FROM RESOURCES
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(XMLParser.class.getClassLoader().getResourceAsStream(LIBRARY), StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        while (line != null && version == null) {
            String[] strings = line.split(",");
            if(strings[0].equals(group) && strings[1].equals(artifact))
                version = strings[2];
            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        return version;
    }

    private static Element getChild(Element element, String name) {
        return element == null ? null : element.getChild(name, namespace);
    }

    private static String getValue(Element element) {
        return element == null ? null : element.getValue();
    }

}
