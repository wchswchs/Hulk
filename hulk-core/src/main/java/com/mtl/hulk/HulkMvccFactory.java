package com.mtl.hulk;

import com.mtl.hulk.model.BusinessActivityIsolationLevel;
import com.mtl.hulk.mvcc.ReadCommitedExecutor;

public class HulkMvccFactory {

    public static HulkMvccExecutor getExecuter(BusinessActivityIsolationLevel isolationLevel){
        if(isolationLevel == BusinessActivityIsolationLevel.READ_COMMITED) {
            return new ReadCommitedExecutor();
        }
        return null;
    }

}
