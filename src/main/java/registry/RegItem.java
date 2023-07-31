package registry;

public class RegItem {

    private String repository;
    private String dependency;
    private String vulnerability;
    private String commitAdd;
    private String commitFix;

    public RegItem(String repository, String dependency, String vulnerability, String commitAdd) {
        this.repository = repository;
        this.dependency = dependency;
        this.vulnerability = vulnerability;
        this.commitAdd = commitAdd;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(String vulnerability) {
        this.vulnerability = vulnerability;
    }

    public String getCommitAdd() {
        return commitAdd;
    }

    public void setCommitAdd(String commitAdd) {
        this.commitAdd = commitAdd;
    }

    public String getCommitFix() {
        return commitFix;
    }

    public void setCommitFix(String commitFix) {
        this.commitFix = commitFix;
    }
}
