package utils;

import dependency.Dependency;
import vulnerability.Vulnerability;
import java.io.*;

public class CSVWriter {

    private static final String FILENAME = "report.csv";

    public static void createReportIfNotExists() throws IOException {
        File file = new File(FILENAME);
        if(!file.exists())
            file.createNewFile();
    }

    public static void write(String repository, Dependency dependency, Vulnerability vulnerability) throws IOException {
        String dependencySTR = dependency.getGroup() + "." + dependency.getArtifact() + "@" + dependency.getVersion();
        String vulnerabilitySTR = vulnerability.getCve();
        String cvssScoreSTR = String.valueOf(vulnerability.getCvssScore());

        if(checkForOK(repository, dependencySTR, vulnerabilitySTR)) {
            FileWriter fileWriter = new FileWriter("report.csv", true);
            fileWriter.write(repository + "," + dependencySTR + "," + vulnerabilitySTR + "," + cvssScoreSTR + "\n");
            fileWriter.close();
        }
    }

    private static boolean checkForOK(String repository, String dependency, String vulnerability) throws IOException {
        boolean check = true;
        BufferedReader bufferedReader = new BufferedReader(new FileReader("report.csv"));
        String line = bufferedReader.readLine();
        while(line != null) {
            String[] strings = line.split(", ");
            if(strings[0].equals(repository) && strings[1].equals(dependency) && strings[2].equals(vulnerability)) {
                check = false;
                break;
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return check;
    }

}
