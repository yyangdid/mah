package mah.plugin.support.orgnote.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zgq on 10/1/16.
 */
public class NodeEntity {
    private String title;
    private List<String> contentLines;
    private String content;
    private List<String> tags;
    private Date lastReviewTime;
    private int familiarDegree;
    private List<NodeEntity> childrens;
    private int quit;
    private int rememberCount;
    private int quitCount;
    private int forgetCount;

    public int getRememberCount() {
        return rememberCount;
    }

    public void setRememberCount(int rememberCount) {
        this.rememberCount = rememberCount;
    }

    public int getQuitCount() {
        return quitCount;
    }

    public void setQuitCount(int quitCount) {
        this.quitCount = quitCount;
    }

    public int getForgetCount() {
        return forgetCount;
    }

    public void setForgetCount(int forgetCount) {
        this.forgetCount = forgetCount;
    }

    public int getQuit() {
        return quit;
    }

    public void setQuit(int quit) {
        this.quit = quit;
    }

    public List<String> getContentLines() {
        if (contentLines == null) {
            contentLines = new ArrayList<>();
        }
        return contentLines;
    }

    public void setContentLines(List<String> contentLines) {
        this.contentLines = contentLines;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(Date lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public int getFamiliarDegree() {
        return familiarDegree;
    }

    public void setFamiliarDegree(int familiarDegree) {
        this.familiarDegree = familiarDegree;
    }

    public List<NodeEntity> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<NodeEntity> childrens) {
        this.childrens = childrens;
    }

    @Override
    public String toString() {
        return "NodeEntity{" +
                "title='" + title + '\'' +
                ", contentLines=" + contentLines +
                ", content='" + content + '\'' +
                ", tags=" + tags +
                ", lastReviewTime=" + lastReviewTime +
                ", familiarDegree=" + familiarDegree +
                ", childrens=" + childrens +
                ", quit=" + quit +
                ", rememberCount=" + rememberCount +
                ", quitCount=" + quitCount +
                ", forgetCount=" + forgetCount +
                '}';
    }

    public String joinContentLines() {
        StringBuilder sb = new StringBuilder();
        if (contentLines == null) {
            return "";
        }
        contentLines.forEach(line->sb.append(line+"\n"));
        return sb.toString();
    }
}
