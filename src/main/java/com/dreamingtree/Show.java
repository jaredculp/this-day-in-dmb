package com.dreamingtree;

import java.util.List;

/**
 * Holds the details of a particular show.
 */
final class Show {
    private final String venue;
    private final String url;
    private final List<String> setlist;

    Show(String venue, String url, List<String> setlist) {
        this.venue = venue;
        this.url = url;
        this.setlist = setlist;
    }

    String getVenue() {
        return venue;
    }

    String getUrl() {
        return url;
    }

    List<String> getSetlist() {
        return setlist;
    }
}
