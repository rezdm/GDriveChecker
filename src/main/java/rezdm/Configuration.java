package rezdm;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Configuration {
    private String Instance;
    private String SecretFile;
    private String CredentialsStore;
    private List<String> Folders;
    private String From;
    private List<String> Recepients;

    private int ParallelGDriveQueries;
    private String DbLocation;

    private String Subject;
    private String FolderUpdate;

    public Configuration() {
    }

    public static Configuration read(String configurationFileName) throws IOException {
        final XmlMapper xmlMapper = new XmlMapper();
        final String xml = new String(Files.readAllBytes(Paths.get(configurationFileName)));
        return xmlMapper.readValue(xml, Configuration.class);
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

    public int getParallelGDriveQueries() {
        return ParallelGDriveQueries;
    }

    public void setParallelGDriveQueries(int parallelGDriveQueries) {
        ParallelGDriveQueries = parallelGDriveQueries;
    }

    public String getDbLocation() {
        return DbLocation;
    }

    public void setDbLocation(String dbLocation) {
        DbLocation = dbLocation;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getFolderUpdate() {
        return FolderUpdate;
    }

    public void setFolderUpdate(String folderUpdate) {
        FolderUpdate = folderUpdate;
    }
}
