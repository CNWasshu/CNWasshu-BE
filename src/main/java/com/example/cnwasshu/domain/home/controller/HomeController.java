package com.example.cnwasshu.domain.home.controller;

import com.example.cnwasshu.domain.home.dto.HomeItemResponse;
import com.example.cnwasshu.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<List<HomeItemResponse>> getHomeItems() {
        return ResponseEntity.ok(homeService.getHomeItems());
    }
}