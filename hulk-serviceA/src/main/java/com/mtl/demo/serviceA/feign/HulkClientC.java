package com.mtl.demo.serviceA.feign;

import com.mtl.hulk.annotation.MTLDTBroker;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("hulkServiceC")
public interface HulkClientC {

    @MTLDTBroker
    @RequestMapping("/hulkServiceC")
    String getHulkServiceC(@RequestParam("a") int a);

}
