package com.mtl.demo.serviceB.service;

import com.mtl.hulk.context.BusinessActivityContext;

public interface HulkServiceB {

    public String getHulkServiceB(int a, int b);
    public boolean confirmMysqlSaveAssetBCard(BusinessActivityContext ctx) throws InterruptedException;
    public boolean cancelMysqlSaveAssetBCard(BusinessActivityContext ctx);

}
