package org.nastya.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceConfigDto(
        Map<String, HostDto> hosts,
        @JsonProperty("hosts_groups")
        Map<String, HostDto> hostsGroup,
        Map<String, PolicyDto> policies) {
}