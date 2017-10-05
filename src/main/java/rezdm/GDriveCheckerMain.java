package rezdm;

import com.google.api.services.gmail.Gmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GDriveCheckerMain {
    private final static Logger log = LoggerFactory.getLogger(GDriveCheckerMain.class);

    public static void main( String[] args ) {
        log.info("GDriveChecker starting");
        String configFileName = null != args && args.length >= 1 ? args[0] : "GDriveChecker-config.xml";

       try {
           final Configuration configuration = Configuration.read(configFileName);
           final GoogleServices services = new GoogleServices(configuration);

           final GDriveFileCollector collector = new GDriveFileCollector(services.drive(), configuration.getFolders(), configuration.getParallelGDriveQueries());
           if(!collector.collect()) {
               log.info("Collecting files information resulted in no files, exiting application");
               return;
           }

           log.info(String.format("Initialize local storage at [%s]", configuration.getDbLocation()));
           final LocalStorage storage = new LocalStorage(configuration.getDbLocation());

           final Map<GDriveFileCollector.GFolder, List<GFile>> remoteFiles = new HashMap<>();
           final List<GFile> storedFiles = new ArrayList<>();

           log.info("Read from Google drive and read from local storage -- begin");
           RunSimultaneously(
               () -> {if(collector.collect()) { remoteFiles.putAll(collector.collected());}},
               () -> storedFiles.addAll(storage.ReadGDriveFileInfo())
           );
           log.info("Read from Google drive and read from local storage -- done");

           final Map<GDriveFileCollector.GFolder, List<GFile>> newFiles = FindNewFiles(storedFiles, remoteFiles);
           PersistUpdates(newFiles, storage);
           ReportNewFiles(configuration, newFiles, services.gmail());
        } catch (IOException | GeneralSecurityException | InterruptedException | MessagingException ex ) {
            log.error("Exception while loading configuration, exiting application", ex);
        }
        log.info("GDriveChecker finished");
    }

    private static void RunSimultaneously(Runnable ... tasks) throws InterruptedException {
        final ExecutorService executor = Executors.newCachedThreadPool();
        Arrays.stream(tasks).forEach(executor::submit);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }

    private static Map<GDriveFileCollector.GFolder, List<GFile>> FindNewFiles(List<GFile> storedFiles, Map<GDriveFileCollector.GFolder, List<GFile>> remoteFiles) {
        final Map<GDriveFileCollector.GFolder, List<GFile>> result = new ConcurrentHashMap<>();
        remoteFiles.entrySet().parallelStream().forEach((entry) -> {
            final GDriveFileCollector.GFolder folder = entry.getKey();
            final List<GFile> remoteFilesInFolder = entry.getValue();
            final List<GFile> absentFiles = remoteFilesInFolder
                .stream()
                .filter((remoteFile) -> storedFiles.stream().noneMatch((storedFile) -> storedFile.getFileId().equals(remoteFile.getFileId())))
                .collect(Collectors.toList())
            ;
            if(!absentFiles.isEmpty()) {
                log.info(String.format("Remote folder [%s] contains [%d] new files", folder.getPath(), absentFiles.size()));
                result.put(folder, absentFiles);
            }
        });

        return result;
    }

    private static void PersistUpdates(Map<GDriveFileCollector.GFolder, List<GFile>> updates, LocalStorage storage) {
        updates.entrySet().parallelStream().forEach((entry) -> storage.WriteGDriveFileInfo(entry.getValue()));
    }

    private static void ReportNewFiles(Configuration configuration, Map<GDriveFileCollector.GFolder, List<GFile>> updates, Gmail mail) throws IOException, MessagingException {
        if(updates.size() > 0) {
            final String body = MessageBody(configuration.getFolderUpdate(), updates);
            GMailHelper.Send(mail,
                configuration.getFrom(),
                configuration.getRecipients(),
                configuration.getSubject(), body
            );
        }
    }

    private static String MessageBody(String lineTemplate, Map<GDriveFileCollector.GFolder, List<GFile>> updates){
        return updates.entrySet()
            .stream().map((e) ->
                lineTemplate
                    .replace("{folder_path}", e.getKey().getPath())
                    .replace("{folder_link}", e.getKey().getUrl())
                    .replace("{new_files_count}", String.valueOf(e.getValue().size()))
            )
            .collect(Collectors.joining(",\r\n"))
        ;
    }
}
