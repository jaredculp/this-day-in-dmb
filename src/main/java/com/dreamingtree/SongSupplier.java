package com.dreamingtree;

import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the last time a song was played.
 *
 * Utilizes a cache so as to not flood DMBAlmanac with requests, and to provide a faster user experience.
 */
final class SongSupplier {

    /**
     * Cache a reference to song last played.
     */
    private static final Map<Integer, String> SONG_LAST_PLAYED = new HashMap<>();

    private final Almanac almanac;

    SongSupplier(Almanac almanac) {
        this.almanac = almanac;
    }

    String get(String songName) throws IOException {
        final Integer songId = Songs.get(songName);
        if (songId == null) {
            throw new IOException("Song " + songName + " does not exist");
        }

        if (SONG_LAST_PLAYED.containsKey(songId)) {
            return SONG_LAST_PLAYED.get(songId);
        }

        final Response<ResponseBody> res = this.almanac.song(songId).execute();
        if (!res.isSuccessful()) {
            throw new IOException("Error retrieving song stats");
        }

        final String lastPlayed = Jsoup.parse(res.body().string())
                .select("span.newsitem")
                .stream()
                .filter(e -> e.text().contains("Last Played:"))
                .map(Element::nextElementSibling)
                .map(Element::text)
                .map(String::trim)
                .findFirst()
                .orElseThrow(() -> new IOException("Could not find last played info for " + songName));

        SONG_LAST_PLAYED.put(songId, lastPlayed);

        return lastPlayed;
    }
}
