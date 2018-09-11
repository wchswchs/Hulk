package com.mtl.hulk.model;

public class BusinessActivityId {

    private String businessDomain;
    private String businessActivity;
    private String entityId;
    private String sequence;

    public BusinessActivityId(String businessDomain, String businessActivity, String entityId, String sequence) {
        this.businessDomain = businessDomain;
        this.businessActivity = businessActivity;
        this.entityId = entityId;
        this.sequence = sequence;
    }

    public BusinessActivityId() {
    }

    public void setBusinessActivity(String businessActivity) {
        this.businessActivity = businessActivity;
    }

    public void setBusinessDomain(String businessDomain) {
        this.businessDomain = businessDomain;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getBusinessActivity() {
        return businessActivity;
    }

    public String getBusinessDomain() {
        return businessDomain;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public String formatString() {
        return businessDomain + "_" + businessActivity + "_" + entityId + "_" + sequence;
    }

}
