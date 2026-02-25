package com.automobilista.giro.repository;

import com.automobilista.giro.model.entity.OrganizacionaJedinica;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizacionaJedinicaRepository
    extends JpaRepository<OrganizacionaJedinica, Long> {

  Optional<OrganizacionaJedinica> findByOjIgnoreCase(String oj);

  boolean existsByOjIgnoreCase(String oj);
}
