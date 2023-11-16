package main;

import dependency.Dependency;
import dependency.POMParser;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jdom2.JDOMException;
import org.json.JSONException;
import registry.RegItem;
import registry.Registry;
import vulnerability.VulnAPI;
import vulnerability.Vulnerability;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws GitAPIException, IOException, ParseException {

        System.out.println("■ VUNCLOAK");

        //SONATYPE API LOGIN
        BufferedReader credentials = new BufferedReader(new InputStreamReader(new FileInputStream("credentials.txt")));
        String email = credentials.readLine().split(":")[1];
        String token = credentials.readLine().split(":")[1];
        credentials.close();
        VulnAPI.authorize(email, token);

        //INPUT REPOSITORY
        ArrayList<String> repos = new ArrayList<>();
        BufferedReader repositories = new BufferedReader(new InputStreamReader(new FileInputStream("repositories.txt")));
        for(String line = repositories.readLine(); line != null; line = repositories.readLine())
            repos.add(line);
        repositories.close();

        for(String repository : repos) {

            //CLONE
            System.out.print("\n■ Cloning Repository: " + repository + "\n");
            GitManager.clone(repository);

            //FILE POM.XML NOT FOUND
            if (!GitManager.hasPOM()) {
                System.out.println("■ File POM.XML Not Found...");
                continue;
            }

            //ANALYSIS START
            long start = System.currentTimeMillis();
            Registry.init();
            POMParser.init();

            //COMMIT HISTORY NAV
            ArrayList<Dependency> previous = new ArrayList<>();
            for (RevCommit commit : GitManager.getPOMCommits()) {
                try {
                    //SHOW POM
                    GitManager.show(commit);
                    System.out.println("\n■ Checking Commit " + commit.getName());

                    //CHECK FOR NEWLY INTRODUCED VULNERABILITIES
                    ArrayList<Dependency> current = POMParser.getDependencies(GitManager.TEMP_POM_FILENAME);

                    //CHECK FOR FIX COMMITS
                    for (Dependency dependency : previous) {
                        if (!current.contains(dependency)) {
                            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                                if(Registry.get(vulnerability).getCommitFix() == null) {
                                    Registry.fix(vulnerability, commit);
                                    System.out.println("■ Fixed Vulnerability " + vulnerability.getCve());
                                }
                            }
                        }
                    }

                    //GET VULNERABILITIES
                    ArrayList<ArrayList<Vulnerability>> book = VulnAPI.getVulnerabilities(current);
                    for(int i = 0; i < current.size(); ++i)
                        current.get(i).setVulnerabilities(book.get(i));

                    previous = new ArrayList<>();
                    for (Dependency dependency : current) {
                        if (!Registry.contains(dependency)) {

                            previous.add(dependency);

                            for (Vulnerability vulnerability : dependency.getVulnerabilities()) {
                                if (Registry.contains(vulnerability)) {
                                    RegItem item = Registry.get(vulnerability);
                                    if (item.getCommitFix() != null)
                                        System.out.println("■ Reintroduced Vulnerability " + vulnerability.getCve() + " -- Dependency " + dependency.resume());
                                    Registry.update(repository, dependency, vulnerability, commit);
                                } else {
                                    Registry.add(repository, dependency, vulnerability, commit);
                                    System.out.println("■ Found Vulnerability " + vulnerability.getCve() + " -- Dependency " + dependency.resume());
                                }

                            }

                        }
                    }

                } catch (JDOMException e) {
                } catch (JSONException e) {
                } catch (ParseException e) {
                } catch (IOException e) {
                } catch (NullPointerException e) {
                } catch (IllegalArgumentException e) {}
            }

            Registry.writeToFile();

            //ANALYSIS END
            long end = System.currentTimeMillis();
            long min = (end - start) / 1000 / 60;
            long sec = (end - start) / 1000 % 60;
            long mil = (end - start) % 1000;

            System.out.print("\n■ Analysis Completed. Time Elapsed: ");
            System.out.print(min < 10 ? ("0" + min + ":") : (min + ":"));
            System.out.print(sec < 10 ? ("0" + sec + ":") : (sec + ":"));
            System.out.print(mil >= 100 ? (mil) : mil > 10 ? ("0" + mil) : "00" + mil);
            System.out.println("");
        }

        GitManager.clean();

        //END
        System.out.print("\n■ Press Enter to Exit");
        System.in.read();
        System.exit(0);
    }

}
