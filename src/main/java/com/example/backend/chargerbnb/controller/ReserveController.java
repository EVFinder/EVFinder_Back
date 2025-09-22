package com.example.backend.chargerbnb.controller;

import com.example.backend.chargerbnb.dto.ReserveDTO;
import com.example.backend.chargerbnb.service.ReserveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<String> addReserve(@PathVariable String uid, @RequestBody ReserveDTO reserve)
            throws ExecutionException, InterruptedException {
        String id = reserveService.addReserve(uid, reserve);
        return ResponseEntity.ok(id);
    }

    // 예약 조회
    @GetMapping("/{uid}")
    public ResponseEntity<List<ReserveDTO>> getReserves(@PathVariable String uid)
            throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(reserveService.getReservesByUser(uid));
    }
}
