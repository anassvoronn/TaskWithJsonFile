package org.nastya.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nastya.dto.DeviceConfigDto;
import org.nastya.entity.Host;
import org.nastya.entity.Policy;
import org.nastya.repository.HostsRepository;
import org.nastya.repository.PoliciesRepository;
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
    private final PolicyMapper policyMapper;
    private final PoliciesRepository policiesRepository;

    public void importJson(String path) {
        try {
            DeviceConfigDto deviceConfigDto =
                    objectMapper.readValue(new File(path), DeviceConfigDto.class);

            int hostsCount = deviceConfigDto.hosts() != null ? deviceConfigDto.hosts().size() : 0;
            int groupsCount = deviceConfigDto.hostsGroup() != null ? deviceConfigDto.hostsGroup().size() : 0;
            int policiesCount = deviceConfigDto.policies() != null ? deviceConfigDto.policies().size() : 0;

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
            log.info("Saved {} hosts/groups", allHosts.size());

            log.info("Starting import: {} policies", policiesCount);

            if (deviceConfigDto.policies() != null) {
                List<Policy> policies = deviceConfigDto.policies().values().stream()
                        .map(policyMapper::mapDtoToEntity)
                        .toList();
                policiesRepository.saveAll(policies);
                log.info("Saved {} policies", policies.size());
            }

            log.info("JSON import completed successfully, saved {} entities", allHosts.size());
        } catch (Exception e) {
            log.error("Unexpected error during JSON import: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during JSON import", e);
        }
    }
}