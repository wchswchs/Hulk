package com.mtl.demo.serviceA.service.impl;


import com.mtl.hulk.annotation.MTLDTActivityID;
import com.mtl.hulk.annotation.MTLDTransaction;
import com.mtl.hulk.context.BusinessActivityContext;
import com.mtl.demo.serviceA.feign.HulkClientB;
import com.mtl.demo.serviceA.feign.HulkClientC;
import com.mtl.demo.serviceA.service.HulkServiceA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("all")
@Component
public class HulkServiceAImpl implements HulkServiceA {
    @Autowired
    private HulkClientB hulkClientB;
    @Autowired
    private HulkClientC hulkClientC;

    private final static Logger logger = LoggerFactory.getLogger(HulkServiceAImpl.class);

    @MTLDTransaction(confirmMethod = "confirmMysqlSaveAssetCard", cancelMethod = "cancelMysqlSaveAssetCard")
    @MTLDTActivityID(businessDomain = "mtl", businessActivity = "test", entityId = "a")
    @Override
    public String getHulkServiceA(int a) {
        String hulkServiceB = this.hulkClientB.getHulkServiceB(a, 2222);
        String hulkServiceC = this.hulkClientC.getHulkServiceC(a);
        if (hulkServiceB != null && hulkServiceC != null) {
            return "hulkServiceA........................" + a;
        }
        return null;
    }

    public boolean confirmMysqlSaveAssetCard(BusinessActivityContext context) {
        logger.info("confirm A params: {}", context.getParams().get("getHulkServiceA"));
        return true;
    }

    public boolean cancelMysqlSaveAssetCard(BusinessActivityContext context) {
        logger.info("cancel A params: {}", context.getParams().get("getHulkServiceA"));
        return true;
    }

}
