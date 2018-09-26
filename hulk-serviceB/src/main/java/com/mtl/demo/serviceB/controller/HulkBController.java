package com.mtl.demo.serviceB.controller;

import com.mtl.demo.serviceB.service.HulkServiceB;
import com.mtl.hulk.context.BusinessActivityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("all")
@RestController
public class HulkBController {
    @Autowired
    private HulkServiceB hulkServiceB;

    @RequestMapping("/hulkServiceB")
    public String getHulkServiceB(@RequestParam("a") int a, @RequestParam("b") int b) throws InterruptedException {
        return hulkServiceB.getHulkServiceB(a, b);
    }

    @RequestMapping(value = "/hulkServiceBConfirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean getHulkServiceBConfirm(@RequestBody BusinessActivityContext ctx) throws InterruptedException {
        return hulkServiceB.confirmMysqlSaveAssetBCard(ctx);
    }

    @RequestMapping(value = "/hulkServiceBCancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean getHulkServiceBCancel(@RequestBody BusinessActivityContext ctx) throws InterruptedException {
        return hulkServiceB.cancelMysqlSaveAssetBCard(ctx);
    }

}
