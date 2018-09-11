package com.mtl.demo.serviceB.controller;

import com.mtl.demo.serviceB.service.HulkServiceB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("all")
@RestController
public class HulkBController {
    @Autowired
    private HulkServiceB hulkServiceB;

    @RequestMapping("/hulkServiceB")
    public String getHulkServiceB(@RequestParam("a") int a, @RequestParam("b") int b) {
        return hulkServiceB.getHulkServiceB(a, b);
    }
}
