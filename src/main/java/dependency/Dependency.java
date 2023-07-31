package dependency;

import vulnerability.Vulnerability;

import java.util.Collection;
import java.util.Objects;

public class Dependency {

    private String group;
    private String artifact;
    private String version;
    private Collection<Vulnerability> vulnerabilities;

    public Dependency(String group, String artifact, String version) {
        this.group = group;
        this.artifact = artifact;
        this.version = version;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Collection<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(Collection<Vulnerability> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }

    public String resume() {
        return group + "." + artifact + "@" + version;
    }

    @Override
    public String toString() {
        return "Dependency{" +
                "group='" + group + '\'' +
                ", artifact='" + artifact + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(getGroup(), that.getGroup()) && Objects.equals(getArtifact(), that.getArtifact()) && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getArtifact(), getVersion());
    }
}
