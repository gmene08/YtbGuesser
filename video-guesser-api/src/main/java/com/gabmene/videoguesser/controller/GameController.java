package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.enums.MatchCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class GameController {


    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAvailableCategories(){
        return ResponseEntity.ok(MatchCategory.valuesAsStrings());
    }
}
