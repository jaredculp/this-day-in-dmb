package com.dreamingtree;

import java.util.Set;

/**
 * Holds the details of a particular show.
 */
final class Show {
    private final String venue;
    private final String url;
    private final Set<String> setlist;

    Show(String venue, String url, Set<String> setlist) {
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

    Set<String> getSetlist() {
        return setlist;
    }
}
