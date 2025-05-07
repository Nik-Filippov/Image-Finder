package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Crawler {
    private final Set<String> visitedPages = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> imageLinks = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final String domain;

    public Crawler(String rootUrl) throws Exception {
        this.domain = new URL(rootUrl).getHost();
        crawlPage(rootUrl);
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
    }

    public Set<String> getImageLinks() {
        return imageLinks;
    }

    private void crawlPage(String url) {
        if (visitedPages.contains(url)) return;

        visitedPages.add(url);
        executor.submit(() -> {
            try {
                Document doc = Jsoup.connect(url).get();

                // Find image tags
                Elements images = doc.select("img[src]");
                for (Element img : images) {
                    String src = img.absUrl("src");
                    if (!src.isEmpty()) imageLinks.add(src);
                }

                // Find internal links
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absHref = link.absUrl("href");
                    if (!absHref.isEmpty() && new URL(absHref).getHost().equals(domain)) {
                        crawlPage(absHref);
                    }
                }
            } catch (Exception ignored) {
                // Optional: log
            }
        });
    }
}
