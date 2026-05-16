package com.afarms.user.utils;

import com.afarms.user.model.entity.Farm;
import com.afarms.user.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FarmBuilderUtils {

    private final FarmRepository farmRepository;

    public record FarmData(Farm farm, UUID farmId, String farmName) {}

    public FarmData createNewFarm(String farmName) {
        Farm farm = Farm.builder()
                .name(farmName)
                .masterId(null)
                .build();
        Farm savedFarm = farmRepository.save(farm);
        return new FarmData(savedFarm, savedFarm.getId(), savedFarm.getName());
    }

    public void linkMasterToFarm(Farm farm, UUID masterId) {
        farm.setMasterId(masterId);
        farmRepository.save(farm);
    }
}