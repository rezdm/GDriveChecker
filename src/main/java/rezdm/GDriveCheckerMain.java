package rezdm;

import com.google.api.services.gmail.Gmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rezdm.data.GDriveFileInfo;

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

           final Map<String, List<GDriveFileInfo>> remoteFiles = new HashMap<>();
           final List<GDriveFileInfo> storedFiles = new ArrayList<>();

           log.info("Read from Google drive and read from local storage -- begin");
           RunSimultaneously(
               () -> {if(collector.collect()) { remoteFiles.putAll(collector.collected());}},
               () -> storedFiles.addAll(storage.ReadGDriveFileInfo())
           );
           log.info("Read from Google drive and read from local storage -- done");

           final Map<String, List<GDriveFileInfo>> newFiles = FindNewFiles(storedFiles, remoteFiles);
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

    private static Map<String, List<GDriveFileInfo>> FindNewFiles(List<GDriveFileInfo> storedFiles, Map<String, List<GDriveFileInfo>> remoteFiles) {
        final Map<String, List<GDriveFileInfo>> result = new ConcurrentHashMap<>();
        remoteFiles.entrySet().parallelStream().forEach((entry) -> {
            final String folderName = entry.getKey();
            final List<GDriveFileInfo> remoteFilesInFolder = entry.getValue();
            final List<GDriveFileInfo> absentFiles = remoteFilesInFolder
                .stream()
                .filter((remoteFile) -> storedFiles.stream().noneMatch((storedFile) -> storedFile.getFileId().equals(remoteFile.getFileId())))
                .collect(Collectors.toList())
            ;
            if(!absentFiles.isEmpty()) {
                log.info(String.format("Remote folder [%s] contains [%d] new files", folderName, absentFiles.size()));
                result.put(folderName, absentFiles);
            }
        });

        return result;
    }

    private static void PersistUpdates(Map<String, List<GDriveFileInfo>> updates, LocalStorage storage) {
        updates.entrySet().parallelStream().forEach((entry) -> storage.WriteGDriveFileInfo(entry.getValue()));
    }

    private static void ReportNewFiles(Configuration configuration, Map<String, List<GDriveFileInfo>> updates, Gmail mail) throws IOException, MessagingException {
        if(updates.size() > 0) {
            final String body = MessageBody(configuration.getFolderUpdate(), updates);
            GMailHelper.Send(mail,
                configuration.getFrom(),
                configuration.getRecipients(),
                configuration.getSubject(), body
            );
        }
    }

    private static String MessageBody(String lineTemplate, Map<String, List<GDriveFileInfo>> updates){
        return updates.entrySet()
            .stream().map((e) ->
                lineTemplate
                    .replace("{folder_path}", e.getKey())
                    .replace("{folder_link}", "link")
                    .replace("{new_files_count}", String.valueOf(e.getValue().size()))
            )
            .collect(Collectors.joining(",\r\n"))
        ;
    }
}
