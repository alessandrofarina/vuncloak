package main;

import dependency.Dependency;
import dependency.POMParser;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jdom2.JDOMException;
import org.json.JSONException;
import registry.Registry;
import git.GitManager;
import vulnerability.VulnRestAPI;
import vulnerability.Vulnerability;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Main {

    public static void main2(String[] args) throws GitAPIException, IOException, ParseException, JDOMException, InterruptedException {

        System.out.print("Enter GitHub repository link: ");
        String repository = (new Scanner(System.in)).nextLine();
        GitManager.clone(repository);

        if(!GitManager.hasPOM()) {
            System.out.println("File POM not found");
            System.exit(0);
        }

        Collection<Dependency> current = POMParser.getPOMDependencies(GitManager.getPOMPath());
        for(Dependency dependency : current) {
            dependency.setVulnerabilities(VulnRestAPI.getVulnerabilities(dependency));
            for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                System.out.println(dependency + " - " + vulnerability);
            }
        }

    }

    public static void main(String[] args) throws GitAPIException, IOException, ParseException {

        Registry.init();

        System.out.print("Enter GitHub repository link: ");
        String repository = (new Scanner(System.in)).nextLine();
        GitManager.clone(repository);

        if(!GitManager.hasPOM()) {
            System.out.println("File POM not found");
            System.exit(0);
        }

        Collection<Dependency> previous = new ArrayList<>();

        for(RevCommit commit : GitManager.getPOMCommits()) {
            try
            {
                GitManager.cherryPick(commit);

                //CHECK FOR NEWLY INTRODUCED VULNERABILITIES
                Collection<Dependency> current = POMParser.getPOMDependencies(GitManager.getPOMPath());

                for(Dependency dependency : previous) {
                    if(!current.contains(dependency)) {
                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            Registry.fix(vulnerability, commit);
                        }
                    }
                }

                previous = new ArrayList<>();
                for(Dependency dependency : current) {
                    if(!Registry.contains(dependency)) {

                        dependency.setVulnerabilities(VulnRestAPI.getVulnerabilities(dependency));
                        previous.add(dependency);

                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            if(Registry.contains(vulnerability))
                               Registry.update(repository, dependency, vulnerability, commit);
                            else
                                Registry.add(repository, dependency, vulnerability, commit);
                        }

                    }
                }

            }
            catch (JDOMException e) { ; }
            catch (JSONException e) { ; }
            catch (ParseException e) { ; }
            catch (InterruptedException e) { ; }
            catch (IllegalArgumentException e) { ; }
            finally { GitManager.reset(); }
        }

        Registry.writeToFile();

        //END
    }

}
