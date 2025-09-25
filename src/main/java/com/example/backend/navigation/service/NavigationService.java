package com.example.backend.navigation.service;

import com.example.backend.navigation.util.TmapClientForNavi;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NavigationService {

    private final TmapClientForNavi tmapClient;

    public String getRoute(double startLat, double startLon, double endLat, double endLon) {
        return tmapClient.getNavigation(startLat, startLon, endLat, endLon);
    }
}
