package com.mtl.demo.serviceA.feign;

import com.mtl.hulk.annotation.MTLDTBroker;
import com.mtl.hulk.context.BusinessActivityContext;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("hulkServiceB")
public interface HulkClientB {

    @MTLDTBroker
    @RequestMapping("/hulkServiceB")
    String getHulkServiceB(@RequestParam("a") int a, @RequestParam("b") int b);

    @RequestMapping(value = "/hulkServiceBConfirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean confirmMysqlSaveAssetBCard(@RequestBody BusinessActivityContext ctx);

    @RequestMapping(value = "/hulkServiceBCancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean cancelMysqlSaveAssetBCard(@RequestBody BusinessActivityContext ctx);

}
