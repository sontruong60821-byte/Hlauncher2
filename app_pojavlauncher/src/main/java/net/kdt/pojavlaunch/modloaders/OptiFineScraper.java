package net.kdt.pojavlaunch.modloaders;

import net.kdt.pojavlaunch.utils.DownloadUtils;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.List;

public class OptiFineScraper implements DownloadUtils.ParseCallback<OptiFineUtils.OptiFineVersions> {
    private final OptiFineUtils.OptiFineVersions mOptiFineVersions;
    private List<OptiFineUtils.OptiFineVersion> mListInProgress;
    private String mMinecraftVersion;
    private String mBaseUrl = "https://optifine.net/";

    public OptiFineScraper() {
        mOptiFineVersions = new OptiFineUtils.OptiFineVersions();
        mOptiFineVersions.minecraftVersions = new ArrayList<>();
        mOptiFineVersions.optifineVersions = new ArrayList<>();
    }

    public OptiFineScraper(String baseUrl) {
        this();
        if (baseUrl != null) {
            this.mBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        }
    }

    @Override
    public OptiFineUtils.OptiFineVersions process(String input) throws DownloadUtils.ParseException {
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        TagNode tagNode = htmlCleaner.clean(input);
        traverseTagNode(tagNode);
        insertVersionContent(null);
        if(mOptiFineVersions.optifineVersions.size() < 1 ||
            mOptiFineVersions.minecraftVersions.size() < 1) throw new DownloadUtils.ParseException(null);
        return mOptiFineVersions;
    }

    public void traverseTagNode(TagNode tagNode) {
        String tagName = tagNode.getName();
        if(isMinecraftVersionTag(tagNode)) {
           insertVersionContent(tagNode);
        } else if((tagName.equals("tr") || tagName.equals("li")) && mMinecraftVersion != null) {
            if (isProbablyDownloadRow(tagNode)) {
                traverseDownloadLine(tagNode);
            } else {

                for(TagNode child : tagNode.getChildTags()) {
                    traverseTagNode(child);
                }
            }
        } else {
            for(TagNode child : tagNode.getChildTags()) {
                traverseTagNode(child);
            }
        }
    }

    private boolean isProbablyDownloadRow(TagNode tagNode) {
        String className = tagNode.getAttributeByName("class");
        if (className != null && className.contains("downloadLine")) return true;

        String rowText = tagNode.getText().toString();
        if ((rowText.contains("HD U") || rowText.contains("pre")) && (rowText.contains("Mirror") || rowText.contains("Download"))) {
            return true;
        }

        for (TagNode child : tagNode.getChildTags()) {
            if (child.getName().equals("td") || child.getName().equals("span") || child.getName().equals("a")) {
                String text = child.getText().toString().toLowerCase();
                if (text.contains("mirror") || text.contains("download")) return true;
                if (getLinkHref(child) != null && (rowText.contains("HD U") || rowText.contains("pre"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMinecraftVersionTag(TagNode tagNode) {
        String name = tagNode.getName();
        String text = tagNode.getText().toString().trim();
        return (name.equals("h2") || name.equals("h3") || name.equals("h1") || name.equals("b")) && text.startsWith("Minecraft ");
    }

    private void traverseDownloadLine(TagNode tagNode) {
        OptiFineUtils.OptiFineVersion optiFineVersion = new OptiFineUtils.OptiFineVersion();
        optiFineVersion.minecraftVersion = mMinecraftVersion;

        findDataInNode(tagNode, optiFineVersion);

        if (optiFineVersion.downloadUrl != null && optiFineVersion.versionName == null) {
             String text = tagNode.getText().toString().trim();

             text = text.replaceAll("(?i)download|mirror|adloadx\\.php.*", "").trim();
             if (!text.isEmpty()) {
                 optiFineVersion.versionName = text;
             }
        }

        if (optiFineVersion.versionName != null && optiFineVersion.downloadUrl != null) {
            mListInProgress.add(optiFineVersion);
        }
    }

    private void findDataInNode(TagNode node, OptiFineUtils.OptiFineVersion version) {
        String className = node.getAttributeByName("class");
        if (className == null) className = "";
        String text = node.getText().toString().trim();

        if (className.contains("colFile") || (text.contains("OptiFine") && !text.toLowerCase().contains("mirror") && !text.toLowerCase().contains("download"))) {
            version.versionName = text;
        }

        if (className.contains("colMirror") || text.toLowerCase().contains("mirror") || className.contains("colDownload") || text.toLowerCase().contains("download")) {
            String href = getLinkHref(node);
            if (href != null) version.downloadUrl = href;
        }

        if (version.versionName == null || version.downloadUrl == null) {
            for (TagNode child : node.getChildTags()) {
                findDataInNode(child, version);
                if (version.versionName != null && version.downloadUrl != null) break;
            }
        }
    }

    private String getLinkHref(TagNode parent) {
        if (parent.getName().equals("a") && parent.hasAttribute("href")) {
            String href = parent.getAttributeByName("href");
            if (href.startsWith("http://") || href.startsWith("https://")) {
                return href.replace("http://", "https://");
            } else if (href.startsWith("/")) {
                int firstSlash = mBaseUrl.indexOf("/", mBaseUrl.indexOf("://") + 3);
                String domain = firstSlash == -1 ? mBaseUrl : mBaseUrl.substring(0, firstSlash);
                return domain + href;
            } else {
                return mBaseUrl + href;
            }
        }
        for(TagNode subNode : parent.getChildTags()) {
            String href = getLinkHref(subNode);
            if (href != null) return href;
        }
        return null;
    }

    private void insertVersionContent(TagNode tagNode) {
        if(mListInProgress != null && mMinecraftVersion != null && !mListInProgress.isEmpty()) {
            mOptiFineVersions.minecraftVersions.add(mMinecraftVersion);
            mOptiFineVersions.optifineVersions.add(mListInProgress);
        }
        if(tagNode != null) {
            mMinecraftVersion = tagNode.getText().toString().trim();
            mListInProgress = new ArrayList<>();
        }
    }
}
