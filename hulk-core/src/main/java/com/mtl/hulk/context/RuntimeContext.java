package com.mtl.hulk.context;

import com.mtl.hulk.model.BusinessActivity;

public class RuntimeContext {

    private BusinessActivity activity = new BusinessActivity();

    public void setActivity(BusinessActivity activity) {
        this.activity = activity;
    }

    public BusinessActivity getActivity() {
        return activity;
    }

}
