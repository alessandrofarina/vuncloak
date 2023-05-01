package main;

import csv.CSVWriter;
import dependency.Dependency;
import dependency.XMLParser;
import github.FileManager;
import github.GitHubMiner;
import logger.Logger;
import org.jdom2.JDOMException;
import org.kohsuke.github.GHContent;
import vulnerability.OSSIndexRestAPI;
import vulnerability.Vulnerability;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Scanner;

public class Main {

    //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

    public static void main(String[] args) throws IOException, JDOMException, ParseException, InterruptedException {

        Logger.turnOn();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter GitHub oAuthToken: ");
        String oAuthToken = scanner.nextLine();

        while(true) {

            System.out.print("Enter a GitHub Repository link: ");
            String repository = scanner.nextLine();

            CSVWriter csvWriter = new CSVWriter();

            String filename = "temp.xml";
            Collection<GHContent> poms = GitHubMiner.getAllPOMs(oAuthToken, repository.substring(19));

            //POM
            for(GHContent pom: poms) {

                Logger.log(Logger.Level.FOUND_POM, pom.getDownloadUrl());

                FileManager.download(pom.getDownloadUrl(), filename);

                //DEPENDENCIES
                Collection<Dependency> dependencies = XMLParser.getDependencies(filename);
                for(Dependency dependency: dependencies) {

                    Logger.log(Logger.Level.FOUND_DEPENDENCY, dependency.toString());

                    //VULNERABILITIES
                    Collection<Vulnerability> vulnerabilities = OSSIndexRestAPI.getVulnerabilities(dependency);
                    for(Vulnerability vulnerability: vulnerabilities) {

                        Logger.log(Logger.Level.FOUND_VULNERABILITY, vulnerability.toString());

                        csvWriter.write(repository, dependency, vulnerability);
                    }
                }

                FileManager.delete(filename);
            }

        }
    }

}
