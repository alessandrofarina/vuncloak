package main;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class GitManager {

    public static final String TEMP_POM_FILENAME = "temp_pom.xml";
    private static int MAX_COUNT = 100;
    private static final String ROOT = ".repos";
    private static File pathToRepo;

    public static void clone(String url) throws GitAPIException {
        pathToRepo = new File(ROOT + "/" + url.substring(19));
        if(!pathToRepo.exists()) {
            pathToRepo.mkdirs();
            Git.cloneRepository().setURI(url).setDirectory(pathToRepo).call();
        }
    }

    public static boolean hasPOM() {
        return (new File(pathToRepo.getPath() + "/pom.xml")).exists();
    }

    public static void showPOM(RevCommit commit) throws IOException, GitAPIException, ParseException {
        try(Git git = Git.open(pathToRepo)) {
            try(Repository repository = git.getRepository()) {
                ObjectId commitTreeId = repository.resolve(commit.getTree().getId().getName() + "^{tree}");
                TreeWalk treeWalk = TreeWalk.forPath(repository, "pom.xml", commitTreeId);

                //READ THE COMMIT VERSION POM FILE
                ObjectReader objectReader = repository.newObjectReader();
                byte[] bytes = objectReader.open(treeWalk.getObjectId(0)).getBytes();
                objectReader.close();

                //DOWNLOAD TO LOCAL
                FileOutputStream outputStream = new FileOutputStream(TEMP_POM_FILENAME);
                outputStream.write(bytes);
                outputStream.close();
            }
        }
    }

    public static Collection<RevCommit> getPOMCommits() throws IOException, GitAPIException {
        ArrayList<RevCommit> list = new ArrayList<>();

        try(Git git = Git.open(pathToRepo)) {

            //GET ALL POM RELATED COMMITS IN THE DEFAULT BRANCH
            ObjectId defaultBranchId = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().get(0).getObjectId();
            Iterator<RevCommit> commits = git.log().addPath("pom.xml").add(defaultBranchId).call().iterator();
            while(commits.hasNext())
                list.add(commits.next());
        }

        Collections.reverse(list);
        return list;
    }

    public static void clean() {
        (new File(TEMP_POM_FILENAME)).delete();
        _clean(new File(ROOT));
    }

    public static void _clean(File root) {
        for(File file: root.listFiles()) {
            if(file.isDirectory())
                _clean(file);
            else
                file.delete();
        }
        root.delete();
    }

}
