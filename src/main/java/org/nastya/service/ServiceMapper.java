package org.nastya.service;

import org.nastya.dto.ServiceDto;
import org.nastya.entity.Service;

public interface ServiceMapper {
    Service mapDtoToEntity(ServiceDto dto);
}