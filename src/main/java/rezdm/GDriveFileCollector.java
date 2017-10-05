package rezdm;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

class GDriveFileCollector {
    class GFolder {
        private final String path;
        private final String id;
        private final String url;

        GFolder(String path, String id, String url) {
            this.path = path;
            this.id = id;
            this.url = url;
        }

        String getPath() {return path;}
        public String getId() {return id;}
        String getUrl() {return url;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GFolder that = (GFolder) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
    private final static Logger log = LoggerFactory.getLogger(GDriveFileCollector.class);

    private final Drive _drive;
    private final Collection<String> _paths;
    private final Map<GFolder, List<GFile>> _remoteFiles = new ConcurrentHashMap<>();
    private final int _parallelQueries;

    GDriveFileCollector(Drive drive, Collection<String> paths, int parallelQueries) {
        _drive = drive;
        _paths = paths;
        _parallelQueries = parallelQueries;
    }

    boolean collect() {
        try {
            final String rootId = _drive.files().get("root").setFields("id").execute().getId();
            final ForkJoinPool pool = new ForkJoinPool(_parallelQueries);
            pool.submit(() -> _paths.parallelStream().forEach((path) -> processSinglePath(rootId, path))).get();
            return _remoteFiles.size() > 0;
        } catch (IOException | ExecutionException | InterruptedException ex) {
            log.error(String.format("Error collecting Google drive files: [%s]", ex.getMessage()), ex);
            throw new RuntimeException(ex);
        }
    }

    Map<GFolder, List<GFile>> collected() {
        return _remoteFiles;
    }

    private void processSinglePath(String rootId, String path) {
        log.info(String.format("Start processing folder [%s]", path));
        try {
            final GFolder folder = resolveFolder(rootId, path);
            Drive.Files.List request = _drive.files().list().setQ("'" + folder.id + "' in parents").setPageSize(100).setFields("nextPageToken, files(id, name)");
            final List<GFile> folderContents = new ArrayList<>();
            do {
                FileList result = request.execute();
                List<File> files = result.getFiles();
                if (files != null) {
                    files.forEach((f) -> {
                        log.debug(String.format("[%s]: %s", path, f.getName()));
                        folderContents.add(new GFile(f.getId(), folder.id, path, f.getName()));
                    } );
                }
                request.setPageToken(result.getNextPageToken());
            } while(request.getPageToken() != null && request.getPageToken().length() > 0);
            if(folderContents.size()>0) {
                _remoteFiles.put(folder, folderContents);
            }
        } catch (IOException ex) {
            log.error(String.format("Error processing folder [%s]", path), ex);
            throw new UncheckedIOException(ex);
        }
        log.info(String.format("Finish processing folder [%s]", path));
    }

    private GFolder resolveFolder(String startingFolderId, String path) throws IOException {
        final StringTokenizer st = new StringTokenizer(path, "/");
        String parentFolderId = startingFolderId;
        GFolder folder = null;
        do {
            String folderName = st.nextToken();
            if (null != folderName) {
                final List<File> files = _drive.files().list().setQ("name = '" + folderName + "' and '" + parentFolderId + "' in parents").setFields("files(id, name, webViewLink)").execute().getFiles();
                if(files.size() != 1) {
                    throw new InvalidPathException(String.format("Wrong path given: [%s]", path), String.format("Cannot find on Google Drive, result contains [%d] folders/files", files.size()));
                }
                final File file = files.get(0);
                parentFolderId = file.getId();
                folder = new GFolder(path, parentFolderId, file.getWebViewLink());
            }
        } while(st.hasMoreTokens());
        return folder;
    }
}
