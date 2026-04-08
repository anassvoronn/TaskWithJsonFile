package org.nastya.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nastya.dto.DeviceConfigDto;
import org.nastya.entity.Host;
import org.nastya.repository.HostsRepository;
import org.springframework.stereotype.Service;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private final ObjectMapper objectMapper;
    private final HostsRepository hostRepository;
    private final HostMapper hostMapper;

    public void importJson(String path) {
        try {
            DeviceConfigDto deviceConfigDto =
                    objectMapper.readValue(new File(path), DeviceConfigDto.class);

            int hostsCount = deviceConfigDto.hosts() != null ? deviceConfigDto.hosts().size() : 0;
            int groupsCount = deviceConfigDto.hostsGroup() != null ? deviceConfigDto.hostsGroup().size() : 0;

            log.info("Starting import: {} hosts, {} host groups", hostsCount, groupsCount);

            List<Host> allHosts = new ArrayList<>();

            if (deviceConfigDto.hosts() != null) {
                List<Host> hosts = deviceConfigDto.hosts().values().stream()
                        .map(hostMapper::mapDtoToEntity)
                        .toList();

                allHosts.addAll(hosts);
            }

            if (deviceConfigDto.hostsGroup() != null) {
                List<Host> groups = deviceConfigDto.hostsGroup().values().stream()
                        .map(hostMapper::mapDtoToEntity)
                        .toList();

                allHosts.addAll(groups);
            }

            hostRepository.saveAll(allHosts);

            log.info("JSON import completed successfully, saved {} entities", allHosts.size());
        } catch (Exception e) {
            log.error("Unexpected error during JSON import: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during JSON import", e);
        }
    }
}