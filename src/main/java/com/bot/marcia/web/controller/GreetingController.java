package com.bot.marcia.web.controller;

import com.bot.marcia.service.impl.YtsLookupService;
import com.bot.marcia.worker.TorrentFileWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {


    @Autowired
    private YtsLookupService ytsLookupService;

    @Autowired
    private TorrentFileWorker torrentFileWorker;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        model.addAttribute("map",torrentFileWorker.returnTorrentUrlsFromYTS(ytsLookupService.buildARequestWithQuery(name)));
        return "greeting";
    }

}
