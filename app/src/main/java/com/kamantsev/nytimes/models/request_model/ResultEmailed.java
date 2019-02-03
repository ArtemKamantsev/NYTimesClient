
package com.kamantsev.nytimes.models.request_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultEmailed extends AbstractResult {

    @SerializedName("subsection")
    @Expose
    private String subsection;
    @SerializedName("email_count")
    @Expose
    private Integer emailCount;
    @SerializedName("count_type")
    @Expose
    private String countType;

    @SerializedName("eta_id")
    @Expose
    private Integer etaId;

    @SerializedName("nytdsection")
    @Expose
    private String nytdsection;

    @SerializedName("updated")
    @Expose
    private String updated;


    public String getSubsection() {
        return subsection;
    }

    public Integer getEmailCount() {
        return emailCount;
    }

    public String getCountType() {
        return countType;
    }

    public Integer getEtaId() {
        return etaId;
    }

    public String getNytdsection() {
        return nytdsection;
    }

    public String getUpdated() {
        return updated;
    }
}
