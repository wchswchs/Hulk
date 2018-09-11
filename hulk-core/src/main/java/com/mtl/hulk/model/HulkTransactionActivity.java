package com.mtl.hulk.model;

import com.mtl.hulk.context.HulkContext;

public class HulkTransactionActivity {

    private BusinessActivity businessActivity;
    private HulkContext hulkContext;

    public void setHulkContext(HulkContext hulkContext) {
        this.hulkContext = hulkContext;
    }

    public void setBusinessActivity(BusinessActivity businessActivity) {
        this.businessActivity = businessActivity;
    }

    public HulkContext getHulkContext() {
        return hulkContext;
    }

    public BusinessActivity getBusinessActivity() {
        return businessActivity;
    }
}
