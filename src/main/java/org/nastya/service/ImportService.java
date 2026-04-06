package org.nastya.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nastya.dto.DeviceConfigDto;
import org.nastya.entity.Host;
import org.nastya.repository.HostsRepository;
import org.springframework.stereotype.Service;


import java.io.File;
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

            log.info("Starting import of {} hosts from JSON", deviceConfigDto.hosts().size());

            List<Host> hosts = deviceConfigDto.hosts().values().stream()
                    .map(hostMapper::mapDtoToEntity)
                    .toList();

            hostRepository.saveAll(hosts);

            log.info("JSON import completed successfully, saved {} hosts", hosts.size());
        } catch (Exception e) {
            log.error("Unexpected error during JSON import: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during JSON import", e);
        }
    }
}