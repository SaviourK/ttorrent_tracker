package cz.kanok.ttorrent_tracker.controller;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;

@RestController
public class TrackerController {

	private static Logger logger = LoggerFactory.getLogger(TrackerController.class.getName());

	//.torrent extension
	private static final String TORRENT_EXTENSION = ".torrent";
	//Path to .torrent file
	private static final String PATH_TO_TORRENT_FILE = "c:/Ttorrent/Torrent";
	//tracker service port
	private static final int TRACKER_SERVICE_PORT = 6969;

	//TRACKER APP PORT 8080
	private Tracker tracker;

	@GetMapping("get-uri")
	public String getUri() {
		return tracker.getAnnounceUrl().toString();
	}

	@GetMapping("get-torrents")
	public String getTorrent() {
		StringBuilder torrents = new StringBuilder();
		for (TrackedTorrent trackedTorrent : tracker.getTrackedTorrents()) {
			torrents.append(trackedTorrent.getName());
		}
		return torrents.toString();
	}

	@GetMapping("start-tracker")
	public void startTorrent() throws IOException, NoSuchAlgorithmException {
		this.tracker = initiateTracker();
	}

	private static Tracker initiateTracker() throws IOException, NoSuchAlgorithmException {
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

		return tracker;
	}

	@GetMapping("stop-tracker")
	public void stopTracker() {
		// You can stop the tracker when you're done with:
		tracker.stop();
	}
}
