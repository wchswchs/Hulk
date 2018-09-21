package com.mtl.demo.serviceC.controller;

import com.mtl.demo.serviceC.service.HulkServiceC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("all")
@RestController
public class HulkCController {
    @Autowired
    private HulkServiceC hulkServiceC;

    @RequestMapping("/hulkServiceC")
    public String getHulkServiceC(@RequestParam("a") int a) throws InterruptedException {
        return hulkServiceC.getHulkServiceC(a);
    }
}
