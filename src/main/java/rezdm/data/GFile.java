package rezdm.data;

public class GFile {
    private int id;

    private String fileId;
    private String folderId;
    private String path;
    private String name;

    public GFile() {
    }

    public GFile(String file_id, String folder_id, String path, String name) {
        this.fileId = file_id;
        this.folderId = folder_id;
        this.path = path;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
