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

    public static void main(String[] args) throws GitAPIException, IOException, ParseException {

        System.out.println("■ GANT");

        //INPUT REPOSITORY
        System.out.print("■ Enter a GitHub Repository URL: ");
        String repository = (new Scanner(System.in)).nextLine();

        //CLONE
        System.out.print("\n■ Cloning Repository...");
        GitManager.clone(repository);

        //FILE POM.XML NOT FOUND
        if(!GitManager.hasPOM()) {
            System.out.println("■ File POM.XML Not Found...");
            System.out.println("■ Press Enter to Exit");
            System.in.read();
            System.exit(0);
        }
        Registry.init();

        //COMMIT HISTORY NAV
        Collection<Dependency> previous = new ArrayList<>();
        for(RevCommit commit : GitManager.getPOMCommits()) {
            try
            {
                //CHERRY PICK
                GitManager.cherryPick(commit);
                System.out.println("\n■ Checking Commit " + commit.getName());

                //CHECK FOR NEWLY INTRODUCED VULNERABILITIES
                Collection<Dependency> current = POMParser.getPOMDependencies(GitManager.getPOMPath());

                for(Dependency dependency : previous) {
                    if(!current.contains(dependency)) {
                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            Registry.fix(vulnerability, commit);
                            System.out.println("■ Fixed Vulnerability " + vulnerability.getCve());
                        }
                    }
                }

                previous = new ArrayList<>();
                for(Dependency dependency : current) {
                    if(!Registry.contains(dependency)) {

                        dependency.setVulnerabilities(VulnRestAPI.getVulnerabilities(dependency));
                        previous.add(dependency);

                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            if(Registry.contains(vulnerability)) {
                                Registry.update(repository, dependency, vulnerability, commit);
                            }
                            else {
                                Registry.add(repository, dependency, vulnerability, commit);
                                System.out.println("■ Found Vulnerability " + vulnerability.getCve() + " -- Dependency " + dependency.resume());
                            }

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
