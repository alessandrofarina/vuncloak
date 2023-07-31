package registry;

import dependency.Dependency;
import org.eclipse.jgit.revwalk.RevCommit;
import vulnerability.Vulnerability;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Registry {

    private static final String FILENAME = "report.csv";
    private static HashSet<String> set;
    private static LinkedHashMap<String, RegItem> map;

    public static void init() throws IOException {
        File file = new File(FILENAME);
        if(!file.exists())
            file.createNewFile();
        set = new HashSet<>();
        map = new LinkedHashMap<>();
    }

    public static void add(String repository, Dependency dependency, Vulnerability vulnerability, RevCommit commitAdd) {
        String keyD = dependency.resume();
        String keyV = vulnerability.getCve();
        set.add(keyD);
        map.put(keyV, new RegItem(repository, keyD, keyV, commitAdd.getName()));
    }

    public static void update(String repository, Dependency dependency, Vulnerability vulnerability, RevCommit commitAdd) {
        RegItem item = map.get(vulnerability.getCve());
        item.setDependency(dependency.resume());
        item.setCommitAdd(commitAdd.getName());
        item.setCommitFix(null);
    }

    public static void fix(Vulnerability vulnerability, RevCommit commitFix) {
        RegItem item = map.get(vulnerability.getCve());
        item.setCommitFix(commitFix.getName());
    }

    public static void addSafe(Dependency dependency) {
        String keyD = dependency.resume();
        set.add(keyD);
    }

    public static boolean contains(Dependency dependency) {
        String keyD = dependency.resume();
        return set.contains(keyD);
    }

    public static boolean contains(Vulnerability vulnerability) {
        String keyV = vulnerability.getCve();
        return map.containsKey(keyV);
    }

    public static void writeToFile() throws IOException {
        FileWriter fileWriter = new FileWriter(FILENAME, true);
        for(String keyV : map.keySet()) {
            RegItem i = map.get(keyV);
            fileWriter.write(i.getRepository() + "," + i.getCommitAdd() + "," + (i.getCommitFix() == null ? "_" : i.getCommitFix()) + "," + i.getDependency() + "," + i.getVulnerability() + "\n");
        }
        fileWriter.close();
    }

}
