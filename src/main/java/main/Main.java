package main;

import dependency.Dependency;
import dependency.XMLParser;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.MultipleParentsNotAllowedException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jdom2.JDOMException;
import org.json.JSONException;
import utils.GitManager;
import vulnerability.RestAPI;
import vulnerability.Vulnerability;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws GitAPIException, IOException {

        GitManager.clone("https://github.com/a");

        if(!GitManager.hasPOM()) {
            System.out.println("File POM not found");
            System.exit(0);
        }

        Collection<Dependency> current, previous = new ArrayList<>();

        for(RevCommit commit : GitManager.getPOMCommits()) {
            try
            {
                GitManager.cherryPick(commit);

                //CHECK FOR NEWLY INTRODUCED VULNERABILITIES
                current = XMLParser.getPOMDependencies();
                for(Dependency dependency : current) {
                    if(!previous.contains(dependency)) {
                        dependency.setVulnerabilities(RestAPI.getVulnerabilities(dependency));
                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            System.out.println(dependency + " - " + vulnerability + " - " + "ADDED");
                        }
                    }
                }

                //CHECK FOR FIXED VULNERABILITIES
                for(Dependency dependency : previous) {
                    if(dependency.getVulnerabilities() != null && !current.contains(dependency)) {
                        for(Vulnerability vulnerability : dependency.getVulnerabilities()) {
                            System.out.println(dependency + " - " + vulnerability + " - " + "FIXED");
                        }
                    }
                }
                previous = current;

            }
            catch (JDOMException e) { ; }
            catch (JSONException e) { ; }
            catch (ParseException e) { ; }
            catch (InterruptedException e) { ; }
            catch (IllegalArgumentException e) { ; }
            catch (MultipleParentsNotAllowedException e) { ; }
            finally { GitManager.reset(); }
        }

        //END
    }

}
