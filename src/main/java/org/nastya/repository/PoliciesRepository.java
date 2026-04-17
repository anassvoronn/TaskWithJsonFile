package org.nastya.repository;

import org.nastya.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "policies")
public interface PoliciesRepository extends JpaRepository<Policy, String> {
}