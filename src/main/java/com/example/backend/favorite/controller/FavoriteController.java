package com.example.backend.favorite.controller;

import com.example.backend.favorite.dto.FavoriteRequest;
import com.example.backend.favorite.service.FavoriteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
     private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // 즐겨찾기 추가
    @PostMapping("/add/{uid}")
    public ResponseEntity<?> addFavorite(
            @PathVariable String uid,
            @RequestBody FavoriteRequest request
    ) throws Exception {
        request.setUid(uid); // path에서 받은 uid를 request에 주입
        favoriteService.addFavorite(request);
        return ResponseEntity.ok("즐겨찾기 추가 완료");
    }

    // 즐겨찾기 삭제
    @DeleteMapping("/delete/{uid}/{stationId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable String uid,
            @PathVariable String stationId
    ) throws Exception {
        favoriteService.removeFavorite(uid, stationId);
        return ResponseEntity.ok("즐겨찾기 삭제 완료");
    }

     // 즐겨찾기 조회
    @GetMapping("/list/{uid}")
    public ResponseEntity<?> getFavorites(@PathVariable String uid) throws Exception {
        return ResponseEntity.ok(favoriteService.getFavorites(uid));
    }

    //즐겨찾기 업데이트
    @PutMapping("/updateStatus/{uid}")
    public ResponseEntity<?> updateFavoriteStatus(@PathVariable String uid) throws Exception {
        return ResponseEntity.ok(favoriteService.updateFavoriteStatus(uid));
    }
}
