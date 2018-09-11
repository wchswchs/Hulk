package com.mtl.demo.serviceA.feign;

import com.mtl.hulk.annotation.MTLDTBroker;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("hulkServiceB")
public interface HulkClientB {

    @MTLDTBroker
    @RequestMapping("/hulkServiceB")
    String getHulkServiceB(@RequestParam("a") int a, @RequestParam("b") int b);

}
