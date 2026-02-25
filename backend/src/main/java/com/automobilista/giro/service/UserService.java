package com.automobilista.giro.service;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.CreateUserRequestDTO;
import com.automobilista.giro.dto.request.UpdateUserRequestDTO;
import com.automobilista.giro.dto.request.UserSearchRequestDTO;
import com.automobilista.giro.dto.response.MessageResponseDTO;
import com.automobilista.giro.dto.response.PagingResponseDTO;
import com.automobilista.giro.exception.resource.ResourceNotFoundException;
import com.automobilista.giro.exception.user.UserNotFoundException;
import com.automobilista.giro.exception.user.UsernameAlreadyExistsException;
import com.automobilista.giro.exception.user.WeakPasswordException;
import com.automobilista.giro.mapper.UserPatchMapper;
import com.automobilista.giro.model.AuditAction;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import com.automobilista.giro.model.entity.OrganizacionaJedinica;
import com.automobilista.giro.model.entity.User;
import com.automobilista.giro.repository.OrganizacionaJedinicaRepository;
import com.automobilista.giro.repository.UserRepository;
import com.automobilista.giro.util.PaginationUtils;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[\\W_]).{8,72}$");
  private static final String ENTITY_TYPE = "USER";

  private final UserRepository repository;
  private final OrganizacionaJedinicaRepository organizacionaJedinicaRepository;
  private final UserPatchMapper userPatchMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuditLogService auditLogService;

  public PagingResponseDTO<UserDTO> getUsers(UserSearchRequestDTO searchRequest) {
    Pageable pageable = PaginationUtils.getPageable(searchRequest);
    Page<User> page = repository.searchUsers(searchRequest, pageable);
    List<UserDTO> data = page.getContent().stream().map(this::toDto).toList();

    return new PagingResponseDTO<>(
        data,
        page.getTotalPages(),
        page.getTotalElements(),
        page.getSize(),
        page.getNumber(),
        page.isEmpty());
  }

  public UserDTO getUser(Long id) {
    User user = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    return toDto(user);
  }

  @Transactional
  public UserDTO createUser(CreateUserRequestDTO createUserRequestDTO) {
    if (createUserRequestDTO.getPassword() == null
        || !PASSWORD_PATTERN.matcher(createUserRequestDTO.getPassword()).matches()) {
      throw new WeakPasswordException();
    }
    ensureEmailUnique(createUserRequestDTO.getEmail(), null);

    User user =
        User.builder()
            .username(createUserRequestDTO.getUsername())
            .fullName(createUserRequestDTO.getFullName())
            .email(createUserRequestDTO.getEmail())
            .password(passwordEncoder.encode(createUserRequestDTO.getPassword()))
            .organizacionaJedinica(
                resolveOrganizacionaJedinica(createUserRequestDTO.getOrgUnitId()))
            .build();
    user.setStatus(Status.INACTIVE);
    ensureUsernameUniqueForActiveStatus(createUserRequestDTO.getUsername(), null, user.getStatus());

    try {
      User savedUser = repository.save(user);
      syncUserRoles(savedUser.getId(), createUserRequestDTO.getRoles());
      auditLogService.logChange(AuditAction.CREATE, ENTITY_TYPE, savedUser.getId());
      return toDto(savedUser);
    } catch (DataIntegrityViolationException ex) {
      throw new UsernameAlreadyExistsException(createUserRequestDTO.getUsername());
    }
  }

  @Transactional
  public UserDTO updateUser(Long id, UpdateUserRequestDTO updateUserRequestDTO) {
    User existingUser = repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

    String nextUsername =
        updateUserRequestDTO.getUsername() != null
            ? updateUserRequestDTO.getUsername()
            : existingUser.getUsername();
    String nextEmail =
        updateUserRequestDTO.getEmail() != null
            ? updateUserRequestDTO.getEmail()
            : existingUser.getEmail();
    Status nextStatus =
        updateUserRequestDTO.getStatus() != null
            ? updateUserRequestDTO.getStatus()
            : existingUser.getStatus();
    ensureUsernameUniqueForActiveStatus(nextUsername, id, nextStatus);
    ensureEmailUnique(nextEmail, id);

    userPatchMapper.applyPatch(updateUserRequestDTO, existingUser);

    if (updateUserRequestDTO.getOrgUnitId() != null) {
      existingUser.setOrganizacionaJedinica(
          resolveOrganizacionaJedinica(updateUserRequestDTO.getOrgUnitId()));
    }

    if (updateUserRequestDTO.getPassword() != null
        && !updateUserRequestDTO.getPassword().isBlank()) {
      if (!PASSWORD_PATTERN.matcher(updateUserRequestDTO.getPassword()).matches()) {
        throw new WeakPasswordException();
      }
      existingUser.setPassword(passwordEncoder.encode(updateUserRequestDTO.getPassword()));
    }

    try {
      User savedUser = repository.save(existingUser);
      if (updateUserRequestDTO.getRoles() != null) {
        syncUserRoles(savedUser.getId(), updateUserRequestDTO.getRoles());
      }
      auditLogService.logChange(AuditAction.UPDATE, ENTITY_TYPE, savedUser.getId());
      return toDto(savedUser);
    } catch (DataIntegrityViolationException ex) {
      throw new UsernameAlreadyExistsException(updateUserRequestDTO.getUsername());
    }
  }

  @Transactional
  public MessageResponseDTO deleteUser(Long id) {
    User user =
        repository
            .findById(id)
            .filter(existing -> existing.getStatus() != Status.DELETED)
            .orElseThrow(() -> new UserNotFoundException(id));

    user.setStatus(Status.DELETED);
    repository.save(user);
    auditLogService.logChange(AuditAction.DELETE, ENTITY_TYPE, user.getId());
    return new MessageResponseDTO("Successfully deleted user with id: " + id);
  }

  private void ensureEmailUnique(String email, Long currentUserId) {
    Optional<User> existing = repository.findByEmailAndStatusNot(email, Status.DELETED);
    if (existing.isPresent()
        && (currentUserId == null || !existing.get().getId().equals(currentUserId))) {
      throw new UsernameAlreadyExistsException(email);
    }
  }

  private void ensureUsernameUniqueForActiveStatus(
      String username, Long currentUserId, Status status) {
    if (status != Status.ACTIVE) {
      return;
    }

    Optional<User> existing = repository.findByUsernameIgnoreCaseAndStatus(username, Status.ACTIVE);
    if (existing.isPresent()
        && (currentUserId == null || !existing.get().getId().equals(currentUserId))) {
      throw new UsernameAlreadyExistsException(username);
    }
  }

  private UserDTO toDto(User user) {
    List<Role> roles =
        repository.findRoleCodesByUserId(user.getId()).stream().map(Role::valueOf).toList();

    return UserDTO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .fullName(user.getFullName())
        .email(user.getEmail())
        .orgUnitId(user.getOrganizacionaJedinica().getId())
        .orgUnitOj(user.getOrganizacionaJedinica().getOj())
        .status(user.getStatus())
        .roles(roles)
        .build();
  }

  private void syncUserRoles(Long userId, List<Role> additionalRoles) {
    Set<Role> targetRoles = EnumSet.of(Role.USER);
    if (additionalRoles != null) {
      targetRoles.addAll(additionalRoles);
    }

    repository.deleteUserRolesByUserId(userId);
    targetRoles.forEach(role -> repository.addUserRole(userId, role.name()));
  }

  private OrganizacionaJedinica resolveOrganizacionaJedinica(Long orgUnitId) {
    return organizacionaJedinicaRepository
        .findById(orgUnitId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organizaciona jedinica nije pronadjena za ID: " + orgUnitId));
  }
}
