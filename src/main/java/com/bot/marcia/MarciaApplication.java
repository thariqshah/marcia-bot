package com.bot.marcia;

import com.bot.marcia.dto.YtsJsonSchema;
import com.bot.marcia.service.MovieLookupService;
import com.bot.marcia.worker.TorrentFileWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MarciaApplication implements CommandLineRunner {

	@Autowired
	private MovieLookupService movieLookupService;

	@Autowired
	TorrentFileWorker torrentFileWorker;

	public static void main(String[] args) {
		SpringApplication.run(MarciaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		YtsJsonSchema data = movieLookupService.buildARequestWithQuery("Once Upon a time in hollywood");
		log.info("",data);
		log.info("{}",torrentFileWorker.returnTorrentUrlsFromYTS(data));
	}
}
