package org.nastya.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceConfigDto(Map<String, HostDto> hosts) {
}