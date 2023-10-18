package com.generator.service;

import co.kr.dains.crowd.estimation.common.domain.svc.SvcInstance;
import co.kr.dains.crowd.estimation.common.repository.svc.SvcInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstanceService {
    private final SvcInstanceRepository svcInstanceRepository;

    public List<SvcInstance> getRandomInstances() {
        List<SvcInstance> selectedInstances = new ArrayList<>();
        List<SvcInstance> instances = svcInstanceRepository.findAll();

        Map<Integer, List<SvcInstance>> groupedInstances = instances.stream().collect(Collectors.groupingBy(it -> it.getSvcCamera().getCameraId()));

        groupedInstances.values().forEach(it -> {
            if (it.size() >= 2) {
                int randomIndex = new Random().nextInt(it.size());
                selectedInstances.add(it.get(randomIndex));
            }
        });

        return selectedInstances;
    }
}

