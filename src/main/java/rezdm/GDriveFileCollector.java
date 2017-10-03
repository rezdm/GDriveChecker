package rezdm;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.InvalidPathException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

class GDriveFileCollector {
    private final static Logger log = LoggerFactory.getLogger(GDriveFileCollector.class);

    private final Drive _drive;
    private final Collection<String> _paths;
    private final Map<String, Collection<File>> _remoteFiles = new ConcurrentHashMap<>();
    private final int _parallelQueries;

    GDriveFileCollector(Drive drive, Collection<String> paths, int parallelQueries) {
        _drive = drive;
        _paths = paths;
        _parallelQueries = parallelQueries;
    }

    boolean collect() throws IOException, ExecutionException, InterruptedException {
        final String rootId = _drive.files().get("root").setFields("id").execute().getId();
        final ForkJoinPool pool = new ForkJoinPool(_parallelQueries);
        pool.submit(() -> _paths.parallelStream().forEach((path) -> processSinglePath(rootId, path))).get();
        return _remoteFiles.size() > 0;
    }

    private void processSinglePath(String rootId, String path) {
        log.info("Processing [" + path + "]");
        try {
            final String folderId = resolveFolderId(rootId, path);
            final List<File> files = _drive.files().list().setQ("'" + folderId + "' in parents").execute().getFiles();
            log.info("Path [" + path + "] with id [" + folderId + "] contains [" + files.size() + "]");
            if(files.size() > 0) {
                _remoteFiles.put(path, files);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String resolveFolderId(String startingFolderId, String path) throws IOException {
        final StringTokenizer st = new StringTokenizer(path, "/");
        String parentFolderId = startingFolderId;
        do {
            String folderName = st.nextToken();
            if (null != folderName) {
                final List<File> files = _drive.files().list().setQ("name = '" + folderName + "' and '" + parentFolderId + "' in parents").execute().getFiles();
                if(files.size() != 1) {
                    throw new InvalidPathException("Wrong path given: [" + path + "]", "Cannot find on Google Drive, result contains [" + files.size() + "] folders/files");
                }
                final File file = files.get(0);
                parentFolderId = file.getId();
            }
        } while(st.hasMoreTokens());
        return parentFolderId;
    }
}
