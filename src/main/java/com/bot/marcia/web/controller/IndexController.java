package com.bot.marcia.web.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Thariq
 * @created 10-08-2021
 **/
@RestController
public class IndexController {


    @GetMapping("/")
    public String index(){
        return "marcia-v0.1.0-build-20210810";
    }
}
