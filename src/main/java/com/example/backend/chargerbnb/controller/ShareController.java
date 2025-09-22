package com.example.backend.chargerbnb.controller;

import com.example.backend.chargerbnb.dto.ShareDTO;
import com.example.backend.chargerbnb.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/share")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    // 공유 충전기 등록
    @PostMapping("/{uid}")
    public ResponseEntity<String> addShare(@PathVariable String uid, @RequestBody ShareDTO share)
            throws ExecutionException, InterruptedException {
        String id = shareService.addShare(uid, share);
        return ResponseEntity.ok(id);
    }

    // 내 공유 충전기 조회
    @GetMapping("/{uid}")
    public ResponseEntity<List<ShareDTO>> getShares(@PathVariable String uid)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(shareService.getSharesByUser(uid));
    }
}
