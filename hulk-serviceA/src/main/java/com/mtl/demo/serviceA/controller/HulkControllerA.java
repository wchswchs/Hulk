package com.mtl.demo.serviceA.controller;


import com.mtl.demo.serviceA.service.HulkServiceA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HulkControllerA {
    @Autowired
    private HulkServiceA hulkServiceA;


    @RequestMapping("/query")
    public String getHulkServiceA() {
        return hulkServiceA.getHulkServiceA(1111);
    }
}
