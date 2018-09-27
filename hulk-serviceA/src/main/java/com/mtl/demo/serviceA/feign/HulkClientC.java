package com.mtl.demo.serviceA.feign;

import com.mtl.hulk.annotation.MTLDTBroker;
import com.mtl.hulk.context.BusinessActivityContext;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("hulkServiceC")
public interface HulkClientC {

    @MTLDTBroker
    @RequestMapping("/hulkServiceC")
    String getHulkServiceC(@RequestParam("a") int a);

    @RequestMapping(value = "/hulkServiceCConfirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean confirmMysqlSaveAssetCCard(@RequestBody BusinessActivityContext ctx);

    @RequestMapping(value = "/hulkServiceCCancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean cancelMysqlSaveAssetCCard(@RequestBody BusinessActivityContext ctx);

}
