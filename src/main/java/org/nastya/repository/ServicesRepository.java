package org.nastya.repository;

import org.nastya.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "services")
public interface ServicesRepository extends JpaRepository<Service, String> {
}