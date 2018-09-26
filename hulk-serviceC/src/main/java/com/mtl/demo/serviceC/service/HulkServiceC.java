package com.mtl.demo.serviceC.service;

import com.mtl.hulk.context.BusinessActivityContext;

public interface HulkServiceC {

    public String getHulkServiceC(int a) throws InterruptedException;
    public boolean confirmMysqlSaveAssetCCard(BusinessActivityContext ctx);
    public boolean cancelMysqlSaveAssetCCard(BusinessActivityContext ctx);

}
