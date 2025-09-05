package com.example.backend.charger.service;

import com.example.backend.charger.dto.ChargerDTO;
import com.example.backend.charger.dto.EvChargerDTO;
import com.example.backend.charger.util.TmapClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChargerService {

    private final TmapClient tmapClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChargerService(TmapClient tmapClient) {
        this.tmapClient = tmapClient;
    }

    public List<ChargerDTO> findNearbyChargers(double lat, double lon) {
        String json = tmapClient.getNearbyChargers(lat, lon);

        // 🔍 전체 Raw Response 로깅
        System.out.println("=== Tmap Raw Response in Service ===");
        System.out.println(json);

        List<ChargerDTO> list = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode pois = root.path("searchPoiInfo").path("pois").path("poi");

            for (JsonNode poi : pois) {
                // EV충전소는 dataKind = "6"
                if (!"6".equals(poi.path("dataKind").asText())) {
                    continue;
                }

                ChargerDTO dto = new ChargerDTO();
                dto.setId(poi.path("id").asText());
                dto.setName(poi.path("name").asText());
                dto.setTel(poi.path("telNo").asText(""));

                // 주소
                String address = poi.path("roadName").asText("") + " "
                        + poi.path("buildingNo1").asText("")
                        + (poi.path("buildingNo2").asText("").isEmpty() ? "" : "-" + poi.path("buildingNo2").asText(""));
                dto.setAddress(address.trim());

                dto.setLat(poi.path("frontLat").asDouble());
                dto.setLon(poi.path("frontLon").asDouble());

                // evChargers
                List<EvChargerDTO> evList = new ArrayList<>();
                JsonNode evChargers = poi.path("evChargers").path("evCharger");
                if (evChargers.isArray()) {
                    for (JsonNode ev : evChargers) {
                        EvChargerDTO evDto = new EvChargerDTO();
                        evDto.setOperatorId(ev.path("operatorId").asText());
                        evDto.setOperatorName(ev.path("operatorName").asText());
                        evDto.setStationId(ev.path("stationId").asText());
                        evDto.setChargerId(ev.path("chargerId").asText());
                        evDto.setType(ev.path("type").asText());
                        evDto.setStatus(ev.path("status").asText());
                        evDto.setPowerType(ev.path("powerType").asText());
                        evDto.setChargingDateTime(ev.path("chargingDateTime").asText());
                        evDto.setUpdateDateTime(ev.path("updateDateTime").asText());
                        evDto.setIsFast(ev.path("isFast").asText());
                        evDto.setIsAvailable(ev.path("isAvailable").asText());
                        evList.add(evDto);
                    }
                }
                dto.setEvChargers(evList);

                list.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 실패", e);
        }

        return list;
    }
}
