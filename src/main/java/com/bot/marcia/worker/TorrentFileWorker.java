package com.bot.marcia.worker;

import com.bot.marcia.common.ApplicationEnums;
import com.bot.marcia.dto.YtsJsonSchema;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TorrentFileWorker {

    //todo return torrent url , quality

    public Map<String, String> returnTorrentUrlsFromYTS(YtsJsonSchema data) {
        Map<String, ApplicationEnums.MovieFileQuality> movieTorrents = new HashMap<>();
        Map<String, String> movieTorrentss = new HashMap<>();
        Map<String, Object> movieObject = (Map<String, Object>) data.getData().getMovies().get(0);
        List<Map<String, String>> torrentObjects = (List<Map<String, String>>) movieObject.get("torrents");
        for (Map<String, String> torrent : torrentObjects
        ) {
            movieTorrentss.put(torrent.get("quality"),torrent.get("url"));
        }
        return movieTorrentss;
    }
}
