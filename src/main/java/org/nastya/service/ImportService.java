package org.nastya.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nastya.dto.DeviceConfigDto;
import org.nastya.entity.Host;
import org.nastya.entity.Policy;
import org.nastya.repository.HostsRepository;
import org.nastya.repository.PoliciesRepository;
import org.nastya.repository.ServicesRepository;
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
    private final ServiceMapper serviceMapper;
    private final ServicesRepository servicesRepository;

    @Transactional
    public void importJson(String path) {
        try {
            DeviceConfigDto deviceConfigDto =
                    objectMapper.readValue(new File(path), DeviceConfigDto.class);

            int hostsCount = deviceConfigDto.hosts() != null ? deviceConfigDto.hosts().size() : 0;
            int hostsGroupsCount = deviceConfigDto.hostsGroup() != null ? deviceConfigDto.hostsGroup().size() : 0;
            int policiesCount = deviceConfigDto.policies() != null ? deviceConfigDto.policies().size() : 0;
            int servicesCount = deviceConfigDto.services() != null ? deviceConfigDto.services().size() : 0;
            int servicesGroupsCount = deviceConfigDto.servicesGroups() != null ? deviceConfigDto.servicesGroups().size() : 0;

            log.info("Starting import: {} hosts, {} host groups", hostsCount, hostsGroupsCount);

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

            List<org.nastya.entity.Service> allServices = new ArrayList<>();

            log.info("Starting import: {} services, {} service groups", servicesCount, servicesGroupsCount);

            if (deviceConfigDto.services() != null) {
                List<org.nastya.entity.Service> services = deviceConfigDto.services().values().stream()
                        .map(serviceMapper::mapDtoToEntity)
                        .toList();

                allServices.addAll(services);
            }

            if (deviceConfigDto.servicesGroups() != null) {
                List<org.nastya.entity.Service> services = deviceConfigDto.servicesGroups().values().stream()
                        .map(serviceMapper::mapDtoToEntity)
                        .toList();

                allServices.addAll(services);
            }

            servicesRepository.saveAll(allServices);
            log.info("Saved {} services/groups", allServices.size());

            log.info("JSON import completed successfully, saved {} entities", allHosts.size());
        } catch (Exception e) {
            log.error("Unexpected error during JSON import: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during JSON import", e);
        }
    }
}