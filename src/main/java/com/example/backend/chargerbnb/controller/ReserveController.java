package com.example.backend.chargerbnb.controller;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.example.backend.chargerbnb.service.ReserveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/reserve")
public class ReserveController {

    private final ReserveService reserveService;

    public ReserveController(ReserveService reserveService) {
        this.reserveService = reserveService;
    }

    // 예약 등록
    @PostMapping("/{uid}")
    public ResponseEntity<?> addReserve(@PathVariable String uid, @RequestBody ReserveDTO reserve)
            throws ExecutionException, InterruptedException {
        // ownerUid 필수 체크
        if (reserve.getOwnerUid() == null || reserve.getOwnerUid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "ownerUid는 필수입니다."
            ));
        }

        String id = reserveService.addReserve(uid, reserve);

        return ResponseEntity.ok(Map.of(
            "reserveId", id,
            "shareId", reserve.getShareId(),
            "ownerUid", reserve.getOwnerUid(),
            "message", "예약이 등록되었습니다."
        ));
    }

    // 예약 조회
    @GetMapping("/{uid}")
    public ResponseEntity<List<Map<String, Object>>> getReserves(@PathVariable String uid)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reserveService.getReservesByUser(uid));
    }

    // 예약 삭제
    @DeleteMapping("/{uid}/{reserveId}")
    public ResponseEntity<?> cancelReserve(
            @PathVariable String uid,
            @PathVariable String reserveId) {
        try {
            reserveService.cancelReserve(uid, reserveId);
            return ResponseEntity.ok(Map.of(
                "reserveId", reserveId,
                "message", "예약이 취소되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // 개별 충전소에 있는 예약 정보
    @GetMapping("share/{shareId}")
    public ResponseEntity<List<ReserveDTO>> getReservesByShare(@PathVariable String shareId)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reserveService.getReservesByShare(shareId));
    }

    // 예약 수정
    @PutMapping("/{uid}/{reserveId}")
    public ResponseEntity<?> updateReserve(
            @PathVariable String uid,
            @PathVariable String reserveId,
            @RequestBody ReserveDTO reserve) throws Exception {

        if (reserve.getOwnerUid() == null || reserve.getOwnerUid().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "ownerUid는 필수입니다."
            ));
        }

        reserveService.updateReserve(uid, reserveId, reserve);

        return ResponseEntity.ok(Map.of(
            "reserveId", reserveId,
            "shareId", reserve.getShareId(),
            "ownerUid", reserve.getOwnerUid(),
            "message", "예약이 수정되었습니다."
        ));
    }
}
