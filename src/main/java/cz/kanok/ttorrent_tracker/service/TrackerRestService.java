package cz.kanok.ttorrent_tracker.service;

import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrackerRestService {

    //TRACKER APP PORT 8080
    private Tracker tracker;

    private List<Torrent> torrentList = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(TrackerRestService.class.getName());

    private static final String CREATED_BY = "createdByVitezslavKanok";
    private static final String TORRENT_EXTENSION = ".torrent";
    private static final String PATH_TO_TORRENT_FILE = "c:/Tracker";
    private static final int TRACKER_SERVICE_PORT = 6969;

    public void saveTorrentFile(Torrent torrent, String pathToTorrentFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathToTorrentFile);
        torrent.save(fos);
        fos.close();
    }

    public String createPathToTorrentFile(Torrent torrent) {
        String torrentFileName = FilenameUtils.removeExtension(torrent.getName()) + TORRENT_EXTENSION;
        return PATH_TO_TORRENT_FILE + File.separator + torrentFileName;
    }

    public String addTorrent(File file) throws URISyntaxException, IOException, NoSuchAlgorithmException, InterruptedException {
        Torrent torrent = Torrent.create(file, tracker.getAnnounceUrl().toURI(), CREATED_BY);
        String pathToTorrentFile = createPathToTorrentFile(torrent);
        saveTorrentFile(torrent, pathToTorrentFile);
        tracker.announce(TrackedTorrent.load(new File(pathToTorrentFile)));
        torrentList.add(torrent);
        return "Torrent successfully announced";
    }

    public Optional<Torrent> findTorrentByUUID(String uuid) {
        return torrentList.stream().filter(t -> t.getName().equals(uuid)).findFirst();
    }

    public void initiateTracker() throws IOException, NoSuchAlgorithmException {
        // First, instantiate a Tracker object with the port you want it to listen on.
        // The default tracker port recommended by the BitTorrent protocol is 6969.
        Tracker tracker = new Tracker(new InetSocketAddress(TRACKER_SERVICE_PORT));

        // Then, for each torrent you wish to announce on this tracker, simply created
        // a TrackedTorrent object and pass it to the tracker.announce() method:
        FilenameFilter filter = (dir, name) -> name.endsWith(TORRENT_EXTENSION);

        //Path to .torrent files
        String path = PATH_TO_TORRENT_FILE;

        // Files for tracker
        File[] torrentFiles = new File(path).listFiles(filter);

        if (torrentFiles == null) {
            logger.info("No torrent files found in: {}", path);
        } else {
            for (File f : torrentFiles) {
                tracker.announce(TrackedTorrent.load(f));
            }
        }
        // Once done, you just have to start the tracker's main operation loop:
        tracker.start();

        logger.info("Tracker start successfully.");

        this.tracker = tracker;
    }

    public void stopTracker() {
        tracker.stop();
    }

    public List<Torrent> getTorrents() {
        return torrentList;
    }

    public String getTrackerAnnounceUrlString() {
        logger.info("Request for tracker announce uri. Uri: {}", tracker.getAnnounceUrl().toString());
        return tracker.getAnnounceUrl().toString();
    }
}
