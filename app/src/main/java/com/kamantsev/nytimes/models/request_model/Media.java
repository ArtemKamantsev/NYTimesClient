
package com.kamantsev.nytimes.models.request_model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = AbstractResult.class,
                                    parentColumns = "id",
                                    childColumns = "resultId",
                                    onDelete = CASCADE),
        tableName = "media")
public class Media {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long resultId;

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("subtype")
    @Expose
    private String subtype;
    @SerializedName("caption")
    @Expose
    private String caption;
    @SerializedName("copyright")
    @Expose
    private String copyright;
    @SerializedName("approved_for_syndication")
    @Expose
    private Integer approvedForSyndication;
    @Ignore
    @SerializedName("media-metadata")
    @Expose
    private List<MediaMetadata> mediaMetadata = null;


    public void bindTo(long resultId){
        this.resultId = resultId;
    }

    public Long getId(){
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getCaption() {
        return caption;
    }

    public String getCopyright() {
        return copyright;
    }

    public Integer getApprovedForSyndication() {
        return approvedForSyndication;
    }

    public List<MediaMetadata> getMediaMetadata() {
        return mediaMetadata;
    }

    public void setMediaMetadata(List<MediaMetadata> mediaMetadata){
        this.mediaMetadata = mediaMetadata;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public void setApprovedForSyndication(Integer approvedForSyndication) {
        this.approvedForSyndication = approvedForSyndication;
    }
}
