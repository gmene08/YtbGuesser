package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/round")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class RoundController {
    private final RoundService roundService;

}
