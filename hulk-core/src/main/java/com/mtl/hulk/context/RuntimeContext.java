package com.mtl.hulk.context;

import com.mtl.hulk.HulkException;
import com.mtl.hulk.model.BusinessActivity;

public class RuntimeContext {

    private BusinessActivity activity = new BusinessActivity();
    private HulkException exception;

    public void setActivity(BusinessActivity activity) {
        this.activity = activity;
    }

    public BusinessActivity getActivity() {
        return activity;
    }

    public void setException(HulkException exception) {
        this.exception = exception;
    }

    public HulkException getException() {
        return exception;
    }

}
