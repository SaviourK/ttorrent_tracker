package cz.kanok.ttorrent_tracker.controller;

import com.turn.ttorrent.common.Torrent;
import cz.kanok.ttorrent_tracker.service.TrackerRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
public class TrackerRestController {

    private static final Logger logger = LoggerFactory.getLogger(TrackerRestController.class.getName());

    @Autowired
    private TrackerRestService trackerRestService;

    @GetMapping("/get-announce-uri")
    public ResponseEntity<String> getAnnounceUri() {
        try {
            String trackerAnnounceUrl = trackerRestService.getTrackerAnnounceUrlString();
            return ResponseEntity.ok(trackerAnnounceUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal service error");
        }
    }

    @GetMapping("/torrents/{uuid}")
    public ResponseEntity<Torrent> getTorrent(@PathVariable String uuid) {
        try {
            logger.info("Request for torrent with UUID: {}", uuid);
            Optional<Torrent> torrent = trackerRestService.findTorrentByUUID(uuid);
            return ResponseEntity.ok(torrent.orElse(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/torrents")
    public List<Torrent> getTorrents() {
        return trackerRestService.getTorrents();
    }

    @PostMapping("/torrents")
    public ResponseEntity<String> addTorrent(@RequestBody File file) {
        try {
            String returnMessage = trackerRestService.addTorrent(file);
            return ResponseEntity.ok(returnMessage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal service error");
        }
    }

    @GetMapping("/start-tracker")
    public void startTorrent() throws IOException, NoSuchAlgorithmException {
        trackerRestService.initiateTracker();
    }

    @GetMapping("/stop-tracker")
    public void stopTracker() {
        trackerRestService.stopTracker();
    }
}
