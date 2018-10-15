package com.mtl.hulk;

import com.mtl.hulk.model.BusinessActivityIsolationLevel;
import com.mtl.hulk.mvcc.ReadCommitedExecutorHulk;
import com.mtl.hulk.mvcc.ReadUncommitedExecutorHulk;

public class HulkMvccFactory {

    public static HulkMvccExecutor getExecuter(BusinessActivityIsolationLevel isolationLevel){
        if(isolationLevel == BusinessActivityIsolationLevel.READ_COMMITED) {
            return new ReadCommitedExecutorHulk();
        }
        if (isolationLevel == BusinessActivityIsolationLevel.READ_UNCOMMITTED) {
            return new ReadUncommitedExecutorHulk();
        }
        return null;
    }

}
