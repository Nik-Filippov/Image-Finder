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
    private final Set<String> logoLinks = Collections.synchronizedSet(new HashSet<>());
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

    public Set<String> getLogoLinks() {
        return logoLinks;
    }

    private void crawlPage(String url) {
        if (!visitedPages.add(url)) return;

        executor.submit(() -> {
            try {
                Document doc = Jsoup.connect(url).userAgent("Mozilla").get();

                extractImages(doc);
                extractFaviconsAndMetaLogos(doc);

                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String absHref = link.absUrl("href");
                    if (!absHref.isEmpty() && new URL(absHref).getHost().equals(domain)) {
                        crawlPage(absHref);
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    private void extractImages(Document doc) {
        Elements images = doc.select("img[src]");
        for (Element img : images) {
            String src = img.absUrl("src");
            if (src.isEmpty()) continue;

            imageLinks.add(src);

            String alt = img.attr("alt").toLowerCase();
            String title = img.attr("title").toLowerCase();
            String className = img.className().toLowerCase();
            String id = img.id().toLowerCase();

            boolean looksLikeLogo = false;

            if (alt.contains("logo") || title.contains("logo") || className.contains("logo") || id.contains("logo")) {
                looksLikeLogo = true;
            }

            // Check parent tags like header/nav/footer
            Element parent = img.parent();
            while (parent != null && !looksLikeLogo) {
                String tag = parent.tagName().toLowerCase();
                String parentClass = parent.className().toLowerCase();
                if (tag.contains("header") || parentClass.contains("logo") || parentClass.contains("nav")) {
                    looksLikeLogo = true;
                    break;
                }
                parent = parent.parent();
            }

            if (looksLikeLogo || src.contains("logo") || src.contains("icon") || src.contains("favicon")) {
                logoLinks.add(src);
            }
        }
    }

    private void extractFaviconsAndMetaLogos(Document doc) {
        Elements iconLinks = doc.select("link[rel~=(?i)^(icon|shortcut icon|apple-touch-icon)]");
        for (Element link : iconLinks) {
            String href = link.absUrl("href");
            if (!href.isEmpty()) {
                logoLinks.add(href);
            }
        }

        Elements metaLogos = doc.select("meta[property=og:image], meta[name=twitter:image]");
        for (Element meta : metaLogos) {
            String content = meta.attr("abs:content");
            if (!content.isEmpty()) {
                logoLinks.add(content);
            }
        }
    }
}