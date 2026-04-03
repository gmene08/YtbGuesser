package com.gabmene.videoguesser.controller;

import com.gabmene.videoguesser.entity.Category;
import com.gabmene.videoguesser.enums.MatchCategory; // Ajuste o import do seu Enum se precisar
import com.gabmene.videoguesser.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final VideoService videoService;

    @GetMapping("/test-youtube")
    public String testYoutubeFetch() {
        try {

            Category testCategory = new Category();
            testCategory.setId(1);
            testCategory.setName(MatchCategory.GAMING);


            //videoService.fetchAndSaveVideosByCategory(testCategory);

            return "Sucesso! Vai lá olhar a tabela 'video' no seu banco de dados!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Deu erro: " + e.getMessage();
        }
    }
}