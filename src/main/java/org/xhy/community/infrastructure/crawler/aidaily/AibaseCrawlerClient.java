package org.xhy.community.infrastructure.crawler.aidaily;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AIBase 爬虫实现
 */
import org.springframework.stereotype.Component;

@Component
public class AibaseCrawlerClient implements CrawlerClient {

    private static final Logger log = LoggerFactory.getLogger(AibaseCrawlerClient.class);

    private static final String BASE = "https://www.aibase.com/zh/daily/";
    private static final String UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<CrawledItem> crawlIncremental(long startId, int maxConsecutive404, int maxRetries, long intervalMillis) {
        List<CrawledItem> results = new ArrayList<>();
        int consecutive404 = 0;
        long currentId = startId;

        while (consecutive404 < maxConsecutive404) {
            String url = BASE + currentId;
            try {
                CrawledItem item = fetchSingle(url, currentId, maxRetries);
                if (item != null) {
                    results.add(item);
                    consecutive404 = 0;
                    log.info("[AIBASE] fetched id={} title={}", currentId, item.getTitle());
                } else {
                    // 404 or parse failure
                    consecutive404++;
                    log.info("[AIBASE] not found or parse failed id={} consecutive404={}", currentId, consecutive404);
                }
            } catch (Exception e) {
                log.warn("[AIBASE] error fetching id=" + currentId + ": " + e.getMessage());
            }

            currentId++;
            try { Thread.sleep(Math.max(0, intervalMillis)); } catch (InterruptedException ignored) {}
        }

        return results;
    }

    @Override
    public List<CrawledItem> crawlIncremental(long startId, int maxConsecutive404, int maxRetries, long intervalMillis, int maxCount) {
        if (maxCount <= 0) {
            return crawlIncremental(startId, maxConsecutive404, maxRetries, intervalMillis);
        }
        List<CrawledItem> results = new ArrayList<>();
        int consecutive404 = 0;
        long currentId = startId;

        while (consecutive404 < maxConsecutive404 && results.size() < maxCount) {
            String url = BASE + currentId;
            try {
                CrawledItem item = fetchSingle(url, currentId, maxRetries);
                if (item != null) {
                    results.add(item);
                    consecutive404 = 0;
                    log.info("[AIBASE] fetched id={} title={}", currentId, item.getTitle());
                } else {
                    consecutive404++;
                    log.info("[AIBASE] not found or parse failed id={} consecutive404={}", currentId, consecutive404);
                }
            } catch (Exception e) {
                log.warn("[AIBASE] error fetching id=" + currentId + ": " + e.getMessage());
            }

            currentId++;
            try { Thread.sleep(Math.max(0, intervalMillis)); } catch (InterruptedException ignored) {}
        }

        return results;
    }

    private CrawledItem fetchSingle(String url, long id, int maxRetries) throws IOException {
        IOException last = null;
        for (int i = 0; i < maxRetries; i++) {
            try {
                Document doc = Jsoup.connect(url).userAgent(UA).timeout(30_000).get();
                return parseDocument(doc, url, id);
            } catch (IOException e) {
                last = e;
                if (is404(e)) {
                    return null; // 404 不重试
                }
                try { Thread.sleep(1000L * (i + 1)); } catch (InterruptedException ignored) {}
            }
        }
        if (last != null) throw last;
        return null;
    }

    private boolean is404(IOException e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.contains("Status=404") || msg.contains("HTTP error fetching URL. Status=404");
    }

    private CrawledItem parseDocument(Document doc, String url, long id) {
        String title = extractTitle(doc);
        String html = extractContentHtml(doc);
        if (title == null || title.isBlank() || html == null || html.isBlank()) {
            return null;
        }
        String sanitized = sanitizeHtml(html);
        LocalDateTime publishedAt = extractPublishedAt(doc);
        if (publishedAt == null) publishedAt = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String summary = buildSummaryFromHtml(sanitized, 200);

        CrawledItem item = new CrawledItem();
        item.setSourceName("AIBASE");
        item.setSourceItemId(id);
        item.setTitle(title);
        item.setContentHtml(sanitized);
        item.setSummary(summary);
        item.setUrl(url);
        item.setPublishedAt(publishedAt);
        item.setMetadata(Collections.emptyMap());
        return item;
    }

    private String extractTitle(Document doc) {
        String[] selectors = new String[]{
            "h1.font-extrabold",
            "h1.md:text-4xl",
            "h1",
            "title",
            ".article-title",
            ".post-title"
        };
        for (String sel : selectors) {
            Element el = doc.selectFirst(sel);
            if (el != null) {
                String t = el.text();
                if (t != null && !t.isBlank()) return t.trim();
            }
        }
        return null;
    }

    private String extractContentHtml(Document doc) {
        // try main content containers
        Elements containers = new Elements();
        String[] containerSel = new String[]{
            ".post-content",
            "article .leading-8",
            "main, article, .content, .article-content"
        };
        for (String sel : containerSel) {
            containers = doc.select(sel);
            if (!containers.isEmpty()) break;
        }
        if (containers.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (Element c : containers) {
            // Remove noisy nodes
            c.select("script, style, nav, header, footer, .advertisement, .ads, .nav, .menu, .sidebar, iframe, object, embed, form").remove();
            Elements parts = c.select("p, div.text, .article-text, h1, h2, h3, h4, h5, h6, ul, ol, li, blockquote, pre, code");
            if (parts.isEmpty()) {
                sb.append(c.html());
            } else {
                for (Element p : parts) {
                    String text = p.text();
                    if (text != null && text.trim().length() > 10) {
                        sb.append('<').append(p.tagName()).append('>').append(p.html()).append("</").append(p.tagName()).append('>').append("\n\n");
                    }
                }
            }
        }
        return sb.toString().trim();
    }

    private String sanitizeHtml(String html) {
        Document d = Jsoup.parseBodyFragment(html);
        d.select("script, style, iframe, object, embed, form, input, button").remove();
        for (Element e : d.select("*")) {
            // remove event handler attrs
            List<String> toRemove = new ArrayList<>();
            e.attributes().forEach(attr -> {
                String key = attr.getKey().toLowerCase();
                String val = attr.getValue().toLowerCase();
                if (key.startsWith("on") || val.startsWith("javascript:") || val.startsWith("vbscript:") || val.startsWith("data:")) {
                    toRemove.add(attr.getKey());
                }
            });
            toRemove.forEach(e::removeAttr);
        }
        return d.body().html().trim();
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        // priority: <time datetime="...">
        Element timeEl = doc.selectFirst("time[datetime]");
        if (timeEl != null) {
            String dt = timeEl.attr("datetime");
            try {
                return LocalDateTime.parse(dt, DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {}
        }
        // fallback by common text patterns
        String text = doc.text();
        List<Pattern> patterns = List.of(
            Pattern.compile("(\\w+\\s+\\d{1,2},\\s+\\d{4})"), // Jan 2, 2006
            Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})"),
            Pattern.compile("(\\d{4}/\\d{1,2}/\\d{1,2})"),
            Pattern.compile("(\\d{4}年\\d{1,2}月\\d{1,2}日)")
        );
        for (Pattern p : patterns) {
            Matcher m = p.matcher(text);
            if (m.find()) {
                String s = m.group(1);
                try { return parseDateFlexible(s); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private LocalDateTime parseDateFlexible(String s) {
        String[] fmts = new String[]{
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "MMM d, yyyy",
            "MMMM d, yyyy",
            "yyyy年M月d日"
        };
        for (String f : fmts) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern(f, Locale.ENGLISH);
                return LocalDateTime.parse(s, fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String buildSummaryFromHtml(String html, int limit) {
        String text = Jsoup.parse(html).text();
        if (text.length() <= limit) return text;
        return text.substring(0, limit) + "...";
    }
}
