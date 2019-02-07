package com.kamantsev.nytimes.models.request_model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;



@Entity(tableName="result")
public class AbstractResult {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("adx_keywords")
    @Expose
    private String adxKeywords;
    @SerializedName("column")
    @Expose
    private String column;
    @SerializedName("section")
    @Expose
    private String section;
    @SerializedName("byline")
    @Expose
    private String byline;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("abstract")
    @Expose
    private String description;
    @SerializedName("published_date")
    @Expose
    private String publishedDate;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("asset_id")
    @Expose
    private Long assetId;
    /*@SerializedName("des_facet")
    @Expose
    private List<String> desFacet = null;
    @SerializedName("org_facet")
    @Expose
    private List<String> orgFacet = null;
    @SerializedName("per_facet")
    @Expose
    private List<String> perFacet = null;
    @SerializedName("geo_facet")
    @Expose
    private String geoFacet;*/
    //TODO change this solution in future (problem was: in json can be string[] or "")
    @SerializedName("des_facet")
    @Expose
    private Object desFacet;
    @SerializedName("org_facet")
    @Expose
    private Object orgFacet;
    @SerializedName("per_facet")
    @Expose
    private Object perFacet;
    @SerializedName("geo_facet")
    @Expose
    private Object geoFacet;
    @Ignore
    @SerializedName("media")
    @Expose
    private List<Media> media = null;
    @SerializedName("uri")
    @Expose
    private String uri;

    private String path;

    public String getPath() {
        return path == null? url: path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAdxKeywords(String adxKeywords) {
        this.adxKeywords = adxKeywords;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setByline(String byline) {
        this.byline = byline;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public void setMedia(List<Media> media){
        this.media = media;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public String getAdxKeywords() {
        return adxKeywords;
    }

    public String getColumn() {
        return column;
    }

    public String getSection() {
        return section;
    }

    public String getByline() {
        return byline;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstract() {
        return description;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public String getSource() {
        return source;
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public List<Media> getMedia() {
        return media;
    }

    public String getUri() {
        return uri;
    }

    public Object getDesFacet() {
        return desFacet;
    }

    public void setDesFacet(Object desFacet) {
        this.desFacet = desFacet;
    }

    public Object getOrgFacet() {
        return orgFacet;
    }

    public void setOrgFacet(Object orgFacet) {
        this.orgFacet = orgFacet;
    }

    public Object getPerFacet() {
        return perFacet;
    }

    public void setPerFacet(Object perFacet) {
        this.perFacet = perFacet;
    }

    public Object getGeoFacet() {
        return geoFacet;
    }

    public void setGeoFacet(Object geoFacet) {
        this.geoFacet = geoFacet;
    }
}
