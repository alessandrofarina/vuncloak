package git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class GitManager {
    private static final String ROOT = ".repos";
    private static final int MAX_COUNT = 100;
    private static File repository;

    public static void clone(String url) throws GitAPIException {
        repository = new File(ROOT + "/" + url.substring(19));
        if(!repository.exists()) {
            repository.mkdirs();
            Git.cloneRepository().setURI(url).setDirectory(repository).call();
        }
    }

    public static boolean hasPOM() {
        File pom = new File(repository.getPath() + "/pom.xml");
        return pom.exists();
    }

    public static String getPOMPath() {
        return repository.getPath() + "/pom.xml";
    }

    public static Collection<RevCommit> getPOMCommits() throws IOException, GitAPIException, ParseException {
        ArrayList<RevCommit> list = new ArrayList<>();

        Ref defaultBranch = Git.open(repository).branchList().setListMode(ListBranchCommand.ListMode.ALL).call().get(0);
        Iterator<RevCommit> commits = Git.open(repository).log().addPath("pom.xml").add(defaultBranch.getObjectId()).call().iterator();
        for(int i = 0; i < MAX_COUNT && commits.hasNext(); ++i)
            list.add(commits.next());
        Collections.reverse(list);

        return list;
    }

    public static void cherryPick(RevCommit commit) throws IOException, GitAPIException {
        Git temp = Git.open(repository);
        while(commit.getParentCount() > 1)
            commit = commit.getParents()[0];
        temp.cherryPick().include(commit).setContentMergeStrategy(ContentMergeStrategy.THEIRS).call();
        temp.close();
    }

    public static void reset() throws IOException, GitAPIException {
        Git temp = Git.open(repository);
        temp.reset().setRef("HEAD^").setMode(ResetCommand.ResetType.HARD).call();
        temp.close();
    }

}
