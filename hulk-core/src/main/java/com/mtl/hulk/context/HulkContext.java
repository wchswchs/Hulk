package com.mtl.hulk.context;

public final class HulkContext {

    private BusinessActivityContext bac;
    private RuntimeContext rc;

    public HulkContext() {}

    public HulkContext(BusinessActivityContext bac, RuntimeContext rc) {
        this.bac = bac;
        this.rc = rc;
    }

    public void setBac(BusinessActivityContext bac) {
        this.bac = bac;
    }

    public BusinessActivityContext getBac() {
        return bac;
    }

    public void setRc(RuntimeContext rc) {
        this.rc = rc;
    }

    public RuntimeContext getRc() {
        return rc;
    }

}
