package com.automobilista.giro.service;

import com.automobilista.giro.exception.resource.ResourceNotFoundException;
import com.automobilista.giro.model.entity.OrganizacionaJedinica;
import com.automobilista.giro.repository.OrganizacionaJedinicaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizacionaJedinicaService {

  private final OrganizacionaJedinicaRepository repository;

  public List<OrganizacionaJedinica> getAll() {
    return repository.findAll();
  }

  public OrganizacionaJedinica getById(Long id) {
    return repository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Organizaciona jedinica nije pronađena: " + id));
  }

  public OrganizacionaJedinica getByOj(String oj) {
    return repository
        .findByOjIgnoreCase(oj)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organizaciona jedinica nije pronađena za OJ: " + oj));
  }

  @Transactional
  public OrganizacionaJedinica create(OrganizacionaJedinica organizacionaJedinica) {
    if (repository.existsByOjIgnoreCase(organizacionaJedinica.getOj())) {
      throw new IllegalArgumentException(
          "Organizaciona jedinica sa OJ već postoji: " + organizacionaJedinica.getOj());
    }
    return repository.save(organizacionaJedinica);
  }
}
