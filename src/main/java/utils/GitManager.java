package utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

public class GitManager {
    private static final String ROOT = ".repos";
    private static File repository;

    public static void clone(String url) throws GitAPIException {
        String path = ROOT + "/" + url.substring(19);

        repository = new File(path);
        if(!repository.exists())
            repository.mkdirs();

        //Git.cloneRepository().setURI(url).setDirectory(repository).call();
    }

    public static boolean hasPOM() {
        File pom = new File(repository.getPath() + "/pom.xml");
        return pom.exists();
    }

    public static String getPOMPath() {
        return repository.getPath() + "/pom.xml";
    }

    public static Collection<RevCommit> getPOMCommits() throws IOException, GitAPIException {
        ArrayList<RevCommit> list = new ArrayList<>();

        Iterable<RevCommit> commits = Git.open(repository).log().addPath("pom.xml").call();
        for(RevCommit commit : commits)
            list.add(commit);

        Collections.reverse(list);
        return list;
    }

    public static void cherryPick(RevCommit commit) throws IOException, GitAPIException {
        Git temp = Git.open(repository);
        temp.cherryPick().include(commit).setContentMergeStrategy(ContentMergeStrategy.THEIRS).call();
        temp.close();
    }

    public static void reset() throws IOException, GitAPIException {
        Git temp = Git.open(repository);
        temp.reset().setRef("HEAD^").setMode(ResetCommand.ResetType.HARD).call();
        temp.close();
    }

}
