package com.dreamingtree;

import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

/**
 * Provides a {@link Show} for the current day in history.
 *
 * Utilizes a cache so as to not flood DMBAlmanac with requests, and to provide a faster user experience.
 */
final class ShowSupplier {

    /**
     * Cache a reference to the most recently retrieve Show.
     */
    private static final AtomicReference<Show> TODAYS_SHOW = new AtomicReference<>();

    private final Almanac almanac;

    ShowSupplier(Almanac almanac) {
        this.almanac = almanac;
    }

    Show get() throws IOException {
        final Show show = TODAYS_SHOW.get();
        if (show != null) {
            return show;
        } else {
            TODAYS_SHOW.set(getTodaysShow());
            return TODAYS_SHOW.get();
        }
    }

    private Show getTodaysShow() throws IOException {
        final Response<ResponseBody> res = this.almanac.homepage().execute();
        if (!res.isSuccessful()) {
            throw new IOException("Error retrieving today's show");
        }

        final Element showEl = Jsoup.parse(res.body().string())
                .select("div")
                .stream()
                .filter(e -> e.text().contains("This Day in DMB History"))
                .findFirst()
                .orElseThrow(IOException::new)
                .nextElementSibling()
                .children()
                .first()
                .children()
                .first();

        final String venue = showEl.nextElementSibling().nextElementSibling().text();
        final String show = showEl.attr("href");
        final String showUrl = show.substring(2, show.length());

        final Response<ResponseBody> showRes = this.almanac.show(showUrl).execute();
        if (!showRes.isSuccessful()) {
            throw new IOException("Could not retrieve setlist for show");
        }

        final List<String> setlist = Jsoup.parse(showRes.body().string())
                .select("a.lightorange")
                .stream()
                .map(Element::text)
                .collect(toList());

        return new Show(venue, show.substring(2, show.length()), setlist);
    }
}
