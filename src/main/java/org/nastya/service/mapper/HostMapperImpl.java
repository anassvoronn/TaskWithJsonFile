package org.nastya.service.mapper;

import org.nastya.dto.AdditionalPropertyDto;
import org.nastya.dto.HostDto;
import org.nastya.entity.AdditionalProperty;
import org.nastya.entity.Host;
import org.nastya.service.HostMapper;
import org.springframework.stereotype.Component;

@Component
public class HostMapperImpl implements HostMapper {

    public Host mapDtoToEntity(HostDto dto) {
        Host host = new Host();
        host.setName(dto.name());
        host.setComment(dto.comment());
        host.setComments(dto.comments());
        host.setMembers(dto.members());
        host.setNegate(dto.isNegate());
        host.setFqdn(dto.fqdn());
        host.setType(dto.type());
        host.setIps(dto.ips());

        AdditionalPropertyDto dtoProp = dto.additionalProperties();
        if (dtoProp != null) {
            AdditionalProperty additionalProperty = new AdditionalProperty(
                    dtoProp.originalName(),
                    dtoProp.broadcast(),
                    Boolean.TRUE.equals(dtoProp.natAutoRule()),
                    Boolean.TRUE.equals(dtoProp.globalLevel()),
                    dtoProp.ckpType(),
                    dtoProp.natTarget(),
                    dtoProp.natIpv4(),
                    dtoProp.natMethod(),
                    dtoProp.location()
            );

            host.getAdditionalProperties().add(additionalProperty);
        }

        return host;
    }
}