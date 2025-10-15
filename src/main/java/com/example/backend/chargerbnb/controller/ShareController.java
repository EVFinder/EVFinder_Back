package com.example.backend.chargerbnb.controller;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.example.backend.chargerbnb.dto.ShareDTO;
import com.example.backend.chargerbnb.service.ShareService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
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

    // 사용자 기준 예약 가능한 모든 충전기 조회(기본 반경 5km)
    @GetMapping("/all")
    public ResponseEntity<List<ShareDTO>> getAllShares(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double radiusKm // 기본 반경 5km
    ) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(shareService.getAllAvailableShares(lat, lon, radiusKm));
    }
    
    // 공유하는 충전기 상태 변경
    @PatchMapping("/{uid}/{shareId}/status")
    public ResponseEntity<String> updateShareStatus(
            @PathVariable String uid,
            @PathVariable String shareId,
            @RequestParam("status") String status) {
        try {
            shareService.updateShareStatus(uid, shareId, status);
            return ResponseEntity.ok("공유 충전기 상태가 업데이트되었습니다: " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 오래된 비활성화 날짜 제거
    @DeleteMapping("/{uid}/{shareId}/disabledDates/cleanup")
    public ResponseEntity<?> cleanupDisabledDates(
            @PathVariable String uid,
            @PathVariable String shareId
    ) {
        try {
            int removed = shareService.cleanupOldDisabledDates(uid, shareId);
            return ResponseEntity.ok(Map.of(
                    "message", "비활성화 일자 정리 완료",
                    "removedCount", removed
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    //예약 가능 정보
    @GetMapping("/{uid}/{shareId}/availability")
    public ResponseEntity<?> getAvailability(
            @PathVariable String uid,
            @PathVariable String shareId
    ) {
        try {
            Map<String, Object> availability = shareService.getAvailability(uid, shareId);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    //비활성화 날짜 추가
    @PostMapping("/{uid}/{shareId}/disabledDates")
    public ResponseEntity<?> addDisabledDates(
            @PathVariable String uid,
            @PathVariable String shareId,
            @RequestBody List<String> newDates
    ) {
        try {
            shareService.addDisabledDates(uid, shareId, newDates);
            return ResponseEntity.ok(Map.of(
                    "message", "비활성화 날짜가 추가되었습니다.",
                    "addedCount", newDates.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 비활성화 날짜 삭제
    @DeleteMapping("/{uid}/{shareId}/disabledDates")
    public ResponseEntity<?> removeDisabledDates(
            @PathVariable String uid,
            @PathVariable String shareId,
            @RequestBody List<String> datesToRemove
    ) {
        try {
            shareService.removeDisabledDates(uid, shareId, datesToRemove);
            return ResponseEntity.ok(Map.of(
                    "message", "비활성화 날짜가 삭제되었습니다.",
                    "removedCount", datesToRemove.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
