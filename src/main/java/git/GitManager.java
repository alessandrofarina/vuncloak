package git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class GitManager {
    private static final String ROOT = ".repos";
    private static final int MAX_COUNT = 100;
    private static File repository;
    private static Git git;

    public static void clone(String url) throws GitAPIException, IOException {
        repository = new File(ROOT + "/" + url.substring(19));
        if(!repository.exists()) {
            repository.mkdirs();
            Git.cloneRepository().setURI(url).setDirectory(repository).call();
        }
        git = Git.open(repository);
    }

    public static boolean hasPOM() {
        File pom = new File(repository.getPath() + "/pom.xml");
        return pom.exists();
    }

    public static void showPOM(RevCommit commit, String filename) throws IOException, GitAPIException, ParseException {
        Repository repo = git.getRepository();
        ObjectId treeId = repo.resolve(commit.getTree().getId().getName() + "^{tree}");
        TreeWalk treeWalk = TreeWalk.forPath(repo, "pom.xml", treeId);

        ObjectReader objectReader = repo.newObjectReader();
        ObjectLoader objectLoader = objectReader.open(treeWalk.getObjectId(0));
        byte[] bytes = objectLoader.getBytes();
        objectReader.close();

        File outputFile = new File(filename);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(bytes);
        outputStream.close();
    }

    public static Collection<RevCommit> getPOMCommits() throws IOException, GitAPIException, ParseException {
        ArrayList<RevCommit> list = new ArrayList<>();

        ObjectId defaultBranchId = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().get(0).getObjectId();

        Iterator<RevCommit> commits = git.log().addPath("pom.xml").add(defaultBranchId).call().iterator();
        for(int i = 0; i < MAX_COUNT && commits.hasNext(); ++i)
            list.add(commits.next());
        Collections.reverse(list);

        return list;
    }

}
