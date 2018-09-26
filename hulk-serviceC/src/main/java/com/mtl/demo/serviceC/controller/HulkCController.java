package com.mtl.demo.serviceC.controller;

import com.mtl.demo.serviceC.service.HulkServiceC;
import com.mtl.hulk.context.BusinessActivityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("all")
@RestController
public class HulkCController {

    @Autowired
    private HulkServiceC hulkServiceC;

    @RequestMapping("/hulkServiceC")
    public String getHulkServiceC(@RequestParam("a") int a) throws InterruptedException {
        return hulkServiceC.getHulkServiceC(a);
    }

    @RequestMapping(value = "/hulkServiceCConfirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean getHulkServiceBConfirm(@RequestBody BusinessActivityContext ctx) throws InterruptedException {
        return hulkServiceC.confirmMysqlSaveAssetCCard(ctx);
    }

    @RequestMapping(value = "/hulkServiceCCancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean getHulkServiceBCancel(@RequestBody BusinessActivityContext ctx) throws InterruptedException {
        return hulkServiceC.cancelMysqlSaveAssetCCard(ctx);
    }

}
