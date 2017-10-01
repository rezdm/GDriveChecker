package rezdm;

import java.util.List;

public class Configuration {
    private String Instance;
    private String SecretFile;
    private String CredentialsStore;
    private List<String> Folders;
    private String From;
    private List<String> Recepients;

    public Configuration() {
    }

    public String getInstance() {
        return this.Instance;
    }

    public void setInstance(String aInstance) {
        this.Instance = aInstance;
    }

    public String getSecretFile() {
        return this.SecretFile;
    }

    public void setSecretFile(String aSecretFile) {
        this.SecretFile = aSecretFile;
    }

    public String getCredentialsStore() {
        return this.CredentialsStore;
    }

    public void setCredentialsStore(String aCredentialsStore) {
        this.CredentialsStore = aCredentialsStore;
    }

    public List<String> getFolders() {
        return this.Folders;
    }

    public void setFolders(List<String> aFolders) {
        this.Folders = aFolders;
    }

    public String getFrom() {
        return this.From;
    }

    public void setFrom(String aFrom) {
        this.From = aFrom;
    }

    public List<String> getRecepients() {
        return this.Recepients;
    }

    public void setRecepients(List<String> aRecepients) {
        this.Recepients = aRecepients;
    }
}
