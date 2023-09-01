package dependency;

import git.GitManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class POMParser {

    private static final String LIBRARY = "library.csv";
    private static Document document;
    private static Namespace namespace;
    private static HashMap<String, HashMap<String, String>> library;

    public static void init() throws IOException {
        //LOAD IN MEMORY LIBRARY FROM RESOURCES
        library = new HashMap<>();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(POMParser.class.getClassLoader().getResourceAsStream(LIBRARY), StandardCharsets.UTF_8));
        for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
            String[] strings = line.split(",");
            String group = strings[0], artifact = strings[1], version = strings[2];

            if(library.containsKey(group)) {
                HashMap<String, String> map = library.get(group);
                map.put(artifact, version);
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put(artifact, version);
                library.put(group, map);
            }
        }

    }

    public static Collection<Dependency> getDependencies() throws IOException, JDOMException {
        ArrayList<Dependency> list = new ArrayList<>();

        document = new SAXBuilder().build(GitManager.TEMP_POM_FILENAME);
        namespace = document.getRootElement().getNamespace();
        list.addAll(_getDependencies(getChild(document.getRootElement(), "dependencies")));
        list.addAll(_getDependencies(getChild(getChild(document.getRootElement(), "dependencyManagement"), "dependencies")));

        return list;
    }

    private static Collection<Dependency> _getDependencies(Element start) throws IOException, JDOMException {
        ArrayList<Dependency> list = new ArrayList<>();

        if(start != null) {
            for(Element dependency: start.getChildren()) {

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
                    version = searchVersion(version);

                //SEARCH FOR VERSION IN ADDITIONAL LIBRARY
                if(version == null)
                    version = searchVersionLibrary(group, artifact);

                //ADD FOUND DEPENDENCY
                if(version != null)
                    list.add(new Dependency(group, artifact, version));
            }

        }

        return list;
    }

    private static String searchVersion(String version) {
        String temp = version.substring(2, version.length() - 1);
        if(temp.equals("project.version"))
            return getValue(getChild(getChild(document.getRootElement(), "parent"), "version"));
        else
            return getValue(getChild(getChild(document.getRootElement(), "properties"), temp));
    }

    private static String searchVersionLibrary(String group, String artifact) throws IOException {
        String version = null;

        if(library.containsKey(group)) {
            HashMap<String, String> map = library.get(group);
            if(map.containsKey(artifact)) {
                version = map.get(artifact);
            }
        }

        return version;
    }

    private static Element getChild(Element element, String name) {
        return element == null ? null : element.getChild(name, namespace);
    }

    private static String getValue(Element element) {
        return element == null ? null : element.getValue();
    }

}
