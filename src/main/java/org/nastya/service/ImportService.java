package org.nastya.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nastya.dto.HostDto;
import org.nastya.dto.PolicyDto;
import org.nastya.dto.ServiceDto;
import org.nastya.entity.Host;
import org.nastya.entity.Policy;
import org.nastya.repository.HostsRepository;
import org.nastya.repository.PoliciesRepository;
import org.nastya.repository.ServicesRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportService {

    private static final int BATCH_SIZE = 100_000;

    private final ObjectMapper objectMapper;
    private final HostsRepository hostRepository;
    private final HostMapper hostMapper;
    private final PolicyMapper policyMapper;
    private final PoliciesRepository policiesRepository;
    private final ServiceMapper serviceMapper;
    private final ServicesRepository servicesRepository;
    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public void importJson(String path) {
        try (JsonParser parser = objectMapper
                .getFactory()
                .createParser(new File(path))) {

            log.info("Starting streaming import: {}", path);

            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Invalid JSON format");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {

                String field = parser.currentName();
                parser.nextToken();

                switch (field) {
                    case "hosts" -> importHosts(parser);
                    case "hosts_groups" -> importHosts(parser);
                    case "policies" -> importPolicies(parser);
                    case "services" -> importServices(parser);
                    case "services_groups" -> importServices(parser);
                    default -> parser.skipChildren();
                }
            }

            log.info("Import finished successfully");

        } catch (Exception e) {
            log.error("Import failed", e);
            throw new RuntimeException("JSON import failed", e);
        }
    }

    private void importHosts(JsonParser parser) throws IOException {

        log.info("Starting hosts import");

        if (parser.currentToken() != JsonToken.START_OBJECT) {
            log.warn("Invalid JSON: expected START_OBJECT, but got {}", parser.currentToken());
            parser.skipChildren();
            return;
        }

        List<Host> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int batchNumber = 0;

        log.info("Entering hosts loop");

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            parser.nextToken();

            HostDto dto = objectMapper.readValue(parser, HostDto.class);

            Host host = hostMapper.mapDtoToEntity(dto);
            batch.add(host);
            count++;

            if (batch.size() >= BATCH_SIZE) {
                log.info("Host added to batch. Current batch size: {}, total processed: {}", batch.size(), count);
                batchNumber++;
                log.info("Flushing batch #{} with size {}", batchNumber, batch.size());
                flushHosts(batch);
                log.info("Batch #{} flushed successfully", batchNumber);
            }
        }

        log.info("Final flush started. Remaining batch size: {}", batch.size());
        flushHosts(batch);

        log.info("Hosts import finished. Total imported: {}", count);
    }

    private void flushHosts(List<Host> batch) {
        if (batch.isEmpty()) {
            log.info("Flush skipped: batch is empty");
            return;
        }

        log.info("Saving {} hosts to database", batch.size());

        hostRepository.saveAll(batch);
        hostRepository.flush();

        log.info("Database flush completed for {} records", batch.size());

        batch.clear();
        entityManager.clear();

        log.info("Batch cleared after flush");
    }

    private void importPolicies(JsonParser parser) throws IOException {

        if (parser.currentToken() != JsonToken.START_OBJECT) {
            parser.skipChildren();
            return;
        }

        List<Policy> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int batchNumber = 0;

        while (parser.nextToken() != JsonToken.END_OBJECT) {

            parser.nextToken();

            PolicyDto dto = objectMapper.readValue(parser, PolicyDto.class);
            Policy entity = policyMapper.mapDtoToEntity(dto);

            batch.add(entity);
            count++;

            if (batch.size() >= BATCH_SIZE) {
                batchNumber++;
                flushPolicies(batch, batchNumber, count);
            }
        }

        if (!batch.isEmpty()) {
            batchNumber++;
            flushPolicies(batch, batchNumber, count);
        }

        log.info("Policies imported: {}", count);
    }

    public void flushPolicies(List<Policy> batch, int batchNumber, int totalCount) {

        policiesRepository.saveAll(batch);
        policiesRepository.flush();

        entityManager.clear();
        batch.clear();

        log.info("Policies batch #{} flushed, total={}", batchNumber, totalCount);
    }

    private void importServices(JsonParser parser) throws IOException {

        if (parser.currentToken() != JsonToken.START_OBJECT) {
            parser.skipChildren();
            return;
        }

        List<org.nastya.entity.Service> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;
        int batchNumber = 0;

        while (parser.nextToken() != JsonToken.END_OBJECT) {

            parser.nextToken();

            ServiceDto dto = objectMapper.readValue(parser, ServiceDto.class);
            org.nastya.entity.Service entity = serviceMapper.mapDtoToEntity(dto);

            batch.add(entity);
            count++;

            if (batch.size() >= BATCH_SIZE) {
                batchNumber++;
                flushServices(batch, batchNumber, count);
            }
        }

        if (!batch.isEmpty()) {
            batchNumber++;
            flushServices(batch, batchNumber, count);
        }

        log.info("Services imported: {}", count);
    }

    public void flushServices(List<org.nastya.entity.Service> batch,
                              int batchNumber,
                              int totalCount) {

        servicesRepository.saveAll(batch);
        servicesRepository.flush();

        entityManager.clear();
        batch.clear();

        log.info("Services batch #{} flushed, total={}", batchNumber, totalCount);
    }
}