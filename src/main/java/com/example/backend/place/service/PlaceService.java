package com.example.backend.place.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.backend.place.dto.AddressDTO;
import com.example.backend.place.dto.PlaceDTO;
import com.example.backend.place.util.PlaceClient;

@Service
public class PlaceService {

    private final PlaceClient placeClient;

    public PlaceService(PlaceClient placeClient) {
        this.placeClient = placeClient;
    }

    public List<PlaceDTO> getPlaceListByKeyword(String query) throws Exception {
        return placeClient.searchPlaces(query);
    }

    public AddressDTO getAddressByCoordinates(String x, String y) throws Exception {
        return placeClient.getAddressFromCoordinates(x, y);
    }
}
