package net.kdt.pojavlaunch.modloaders;

import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import java.io.IOException;

public class OFDownloadPageScraper implements TagNodeVisitor {
    public static String run(String urlInput) throws IOException{
        String result = new OFDownloadPageScraper().runInner(urlInput);
        if (result == null) {
            throw new IOException("Failed to find download link on OptiFine ad-page");
        }
        return result;
    }

    private String mDownloadFullUrl;
    private String mBaseUrl;

    private String runInner(String url) throws IOException {
        try {
            return performScrape(url);
        } catch (IOException e) {
            if (url.contains("optifine.net")) {
                String fallbackUrl = url.replace("optifine.net", "optifined.net");
                return performScrape(fallbackUrl);
            }
            throw e;
        }
    }

    private String performScrape(String url) throws IOException {
        this.mBaseUrl = url.substring(0, url.indexOf("/", url.indexOf("://") + 3));
        String htmlContent = DownloadUtils.downloadString(url);
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        htmlCleaner.clean(htmlContent).traverse(this);
        return mDownloadFullUrl;
    }

    @Override
    public boolean visit(TagNode parentNode, HtmlNode htmlNode) {
        if(isDownloadUrl(parentNode, htmlNode)) {
            TagNode tagNode = (TagNode) htmlNode;
            String href = tagNode.getAttributeByName("href");
            if(!href.startsWith("https://") && !href.startsWith("http://")) {
                if (href.startsWith("/")) {
                    href = mBaseUrl + href;
                } else {
                    href = mBaseUrl + "/" + href;
                }
            }
            this.mDownloadFullUrl = href.replace("http://", "https://");
            return false;
        }
        return true;
    }

    public boolean isDownloadUrl(TagNode parentNode, HtmlNode htmlNode) {
        if(!(htmlNode instanceof TagNode)) return false;
        TagNode tagNode = (TagNode) htmlNode;
        if (!tagNode.getName().equals("a")) return false;

        String onclick = tagNode.getAttributeByName("onclick");
        if ("onDownload()".equals(onclick)) return true;

        String href = tagNode.getAttributeByName("href");
        if (href != null && href.contains("downloadx")) return true;

        if (parentNode != null) {
            String parentId = parentNode.getAttributeByName("id");
            if ("Download".equalsIgnoreCase(parentId)) return true;
        }

        return false;
    }
}
