package com.automobilista.giro.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.automobilista.giro.dto.UserDTO;
import com.automobilista.giro.dto.request.CreateUserRequestDTO;
import com.automobilista.giro.dto.request.UpdateUserRequestDTO;
import com.automobilista.giro.dto.request.UserSearchRequestDTO;
import com.automobilista.giro.dto.response.MessageResponseDTO;
import com.automobilista.giro.dto.response.PagingResponseDTO;
import com.automobilista.giro.exception.user.UserNotFoundException;
import com.automobilista.giro.exception.user.UsernameAlreadyExistsException;
import com.automobilista.giro.exception.user.WeakPasswordException;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import com.automobilista.giro.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.flyway.enabled=true")
class UserServiceIT {

  private static final String EMAIL_DOMAIN = "@giro.com";

  private static final String ACTIVE_USERNAME_OWNER_FULL_NAME = "Active Username Owner";
  private static final String SORT_FIELD_USERNAME = "username";

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;

  @Test
  void givenNewUserWhenCreatedAndAssignedAdditionalRoleThenContainsDefaultAndAdditionalRoles() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.user." + seed;
    String email = "itest.user." + seed + EMAIL_DOMAIN;

    CreateUserRequestDTO createRequest =
        CreateUserRequestDTO.builder()
            .username(username)
            .fullName("Integration Test User")
            .email(email)
            .orgUnitId(1L)
            .password("itest.user.giro123!")
            .build();

    UserDTO createdUser = userService.createUser(createRequest);

    assertThat(createdUser.getStatus()).isEqualTo(Status.INACTIVE);
    assertThat(createdUser.getRoles()).containsExactly(Role.USER);

    UpdateUserRequestDTO updateRequest =
        UpdateUserRequestDTO.builder()
            .username(username)
            .fullName("Integration Test User")
            .email(email)
            .orgUnitId(1L)
            .status(Status.ACTIVE)
            .roles(List.of(Role.BUSINESS_ADMIN))
            .build();

    UserDTO updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

    assertThat(updatedUser.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(updatedUser.getRoles()).contains(Role.USER, Role.BUSINESS_ADMIN);
    assertThat(userRepository.findRoleCodesByUserId(updatedUser.getId()))
        .contains("USER", "BUSINESS_ADMIN");
  }

  @Test
  void givenNewUserWhenCreatedThenAssignsDefaultUserRole() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.default.role." + seed;
    String email = "itest.default.role." + seed + EMAIL_DOMAIN;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Integration Default Role User")
                .email(email)
                .orgUnitId(1L)
                .password("itest.default.role.giro123!")
                .build());

    assertThat(createdUser.getRoles()).containsExactly(Role.USER);
    assertThat(userRepository.findRoleCodesByUserId(createdUser.getId())).containsExactly("USER");
  }

  @Test
  void givenNewUserWhenCreatedThenInitialStatusIsInactive() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.default.status." + seed;
    String email = "itest.default.status." + seed + EMAIL_DOMAIN;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Integration Default Status User")
                .email(email)
                .orgUnitId(1L)
                .password("itest.default.status.giro123!")
                .build());

    assertThat(createdUser.getStatus()).isEqualTo(Status.INACTIVE);
  }

  @Test
  void givenExistingUserWhenUpdateWithOnlyIdAndStatusThenUpdatesOnlyStatus() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.partial.update." + seed;
    String email = "itest.partial.update." + seed + EMAIL_DOMAIN;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Integration Partial Update User")
                .email(email)
                .orgUnitId(1L)
                .password("itest.partial.update.giro123!")
                .build());

    UpdateUserRequestDTO updateRequest =
        UpdateUserRequestDTO.builder().status(Status.ACTIVE).build();

    UserDTO updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

    assertThat(updatedUser.getStatus()).isEqualTo(Status.ACTIVE);
    assertThat(updatedUser.getUsername()).isEqualTo(username);
    assertThat(updatedUser.getEmail()).isEqualTo(email);
    assertThat(updatedUser.getFullName()).isEqualTo("Integration Partial Update User");
    assertThat(updatedUser.getOrgUnitId()).isEqualTo(1L);
  }

  @Test
  void givenPasswordWithMinLengthDigitAndSpecialWhenCreateUserThenCreatesUser() {
    String seed = String.valueOf(System.currentTimeMillis());

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username("itest.password.valid." + seed)
                .fullName("Integration Password Valid")
                .email("itest.password.valid." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("Aa1@aaaa")
                .build());

    assertThat(createdUser.getId()).isNotNull();
    assertThat(createdUser.getStatus()).isEqualTo(Status.INACTIVE);
  }

  @Test
  void givenPasswordWithoutSpecialCharacterWhenCreateUserThenThrowsWeakPasswordException() {
    String seed = String.valueOf(System.currentTimeMillis());
    CreateUserRequestDTO weakPasswordRequest =
        CreateUserRequestDTO.builder()
            .username("itest.password.no.special." + seed)
            .fullName("Integration Password No Special")
            .email("itest.password.no.special." + seed + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .password("Password1")
            .build();

    assertThatThrownBy(() -> userService.createUser(weakPasswordRequest))
        .isInstanceOf(WeakPasswordException.class);
  }

  @Test
  void givenPasswordWithoutDigitWhenCreateUserThenThrowsWeakPasswordException() {
    String seed = String.valueOf(System.currentTimeMillis());
    CreateUserRequestDTO weakPasswordRequest =
        CreateUserRequestDTO.builder()
            .username("itest.password.no.digit." + seed)
            .fullName("Integration Password No Digit")
            .email("itest.password.no.digit." + seed + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .password("Password@")
            .build();

    assertThatThrownBy(() -> userService.createUser(weakPasswordRequest))
        .isInstanceOf(WeakPasswordException.class);
  }

  @Test
  void givenExistingUserWhenDeleteUserThenPerformsSoftDeleteAndUserIsNotAccessible() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.delete.user." + seed;
    String email = "itest.delete.user." + seed + EMAIL_DOMAIN;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Integration Delete User")
                .email(email)
                .orgUnitId(1L)
                .password("itest.delete.user.giro123!")
                .build());

    MessageResponseDTO response = userService.deleteUser(createdUser.getId());

    assertThat(response.getMessage()).contains("Successfully deleted user");
    assertThat(userRepository.findById(createdUser.getId())).isPresent();
    assertThat(userRepository.findById(createdUser.getId()).orElseThrow().getStatus())
        .isEqualTo(Status.DELETED);
    Long deletedUserId = createdUser.getId();
    assertThatThrownBy(() -> userService.getUser(deletedUserId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void givenExistingUserIdWhenGetUserThenReturnsUserDetails() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.get." + seed;
    String email = "itest.get." + seed + EMAIL_DOMAIN;

    CreateUserRequestDTO createRequest =
        CreateUserRequestDTO.builder()
            .username(username)
            .fullName("Integration Get User")
            .email(email)
            .orgUnitId(1L)
            .password("itest.get.giro123!")
            .build();

    UserDTO createdUser = userService.createUser(createRequest);
    UserDTO fetchedUser = userService.getUser(createdUser.getId());

    assertThat(fetchedUser.getId()).isEqualTo(createdUser.getId());
    assertThat(fetchedUser.getUsername()).isEqualTo(username);
    assertThat(fetchedUser.getEmail()).isEqualTo(email);
    assertThat(fetchedUser.getStatus()).isEqualTo(Status.INACTIVE);
    assertThat(fetchedUser.getRoles()).containsExactly(Role.USER);
  }

  @Test
  void givenSearchRequestWithPaginationAndSortingWhenGetUsersThenReturnsFilteredAndSortedPage() {
    String seed = String.valueOf(System.currentTimeMillis());
    String prefix = "itest.list." + seed;

    userService.createUser(
        CreateUserRequestDTO.builder()
            .username(prefix + ".b")
            .fullName("Integration List B")
            .email(prefix + ".b" + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .password("itest.list.giro123!")
            .build());

    userService.createUser(
        CreateUserRequestDTO.builder()
            .username(prefix + ".a")
            .fullName("Integration List A")
            .email(prefix + ".a" + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .password("itest.list.giro123!")
            .build());

    UserSearchRequestDTO searchRequest =
        UserSearchRequestDTO.builder()
            .username(prefix)
            .status(Status.INACTIVE)
            .sortField(SORT_FIELD_USERNAME)
            .direction(Sort.Direction.ASC)
            .page(1)
            .size(10)
            .build();

    PagingResponseDTO<UserDTO> page = userService.getUsers(searchRequest);

    List<UserDTO> pageData = new ArrayList<>(page.getData());

    assertThat(pageData).hasSize(2);
    assertThat(page.getTotalElements()).isEqualTo(2);
    assertThat(pageData.get(0).getUsername()).isEqualTo(prefix + ".a");
    assertThat(pageData.get(1).getUsername()).isEqualTo(prefix + ".b");
  }

  @Test
  void givenRoleFilterWhenGetUsersThenReturnsOnlyUsersWithRequestedRole() {
    String seed = String.valueOf(System.currentTimeMillis());
    String prefix = "itest.role.filter." + seed;

    UserDTO hovUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(prefix + ".hov")
                .fullName("Integration Role HOV")
                .email(prefix + ".hov" + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.role.filter.giro123!")
                .build());

    userService.updateUser(
        hovUser.getId(),
        UpdateUserRequestDTO.builder()
            .status(Status.ACTIVE)
            .roles(List.of(Role.HOV_ADMIN))
            .build());

    UserDTO businessUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(prefix + ".biz")
                .fullName("Integration Role Business")
                .email(prefix + ".biz" + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.role.filter.giro123!")
                .build());

    userService.updateUser(
        businessUser.getId(),
        UpdateUserRequestDTO.builder()
            .status(Status.ACTIVE)
            .roles(List.of(Role.BUSINESS_ADMIN))
            .build());

    UserSearchRequestDTO searchRequest =
        UserSearchRequestDTO.builder()
            .username(prefix)
            .roles(List.of(Role.HOV_ADMIN))
            .sortField(SORT_FIELD_USERNAME)
            .direction(Sort.Direction.ASC)
            .page(1)
            .size(10)
            .build();

    PagingResponseDTO<UserDTO> page = userService.getUsers(searchRequest);
    List<UserDTO> pageData = new ArrayList<>(page.getData());

    assertThat(pageData).hasSize(1);
    assertThat(pageData.getFirst().getId()).isEqualTo(hovUser.getId());
    assertThat(pageData.getFirst().getRoles()).contains(Role.HOV_ADMIN);
  }

  @Test
  void givenNonExistingUserIdWhenGetUserThenThrowsUserNotFoundException() {
    assertThatThrownBy(() -> userService.getUser(Long.MAX_VALUE))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void givenDeletedUserIdWhenGetUserThenThrowsUserNotFoundException() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.deleted." + seed;
    String email = "itest.deleted." + seed + EMAIL_DOMAIN;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Integration Deleted User")
                .email(email)
                .orgUnitId(1L)
                .password("itest.deleted.giro123!")
                .build());

    userService.deleteUser(createdUser.getId());

    Long deletedUserId = createdUser.getId();
    assertThatThrownBy(() -> userService.getUser(deletedUserId))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void givenDeletedAndNonDeletedUsersWhenGetUsersWithoutStatusFilterThenExcludesDeletedUsers() {
    String seed = String.valueOf(System.currentTimeMillis());
    String prefix = "itest.exclude.deleted." + seed;

    UserDTO activeLikeUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(prefix + ".one")
                .fullName("Exclude Deleted One")
                .email(prefix + ".one" + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.exclude.giro123!")
                .build());

    UserDTO toDeleteUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(prefix + ".two")
                .fullName("Exclude Deleted Two")
                .email(prefix + ".two" + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.exclude.giro123!")
                .build());

    userService.deleteUser(toDeleteUser.getId());

    UserSearchRequestDTO searchRequest =
        UserSearchRequestDTO.builder()
            .username(prefix)
            .sortField(SORT_FIELD_USERNAME)
            .direction(Sort.Direction.ASC)
            .page(1)
            .size(10)
            .build();

    PagingResponseDTO<UserDTO> page = userService.getUsers(searchRequest);
    List<UserDTO> pageData = new ArrayList<>(page.getData());

    assertThat(pageData).hasSize(1);
    assertThat(pageData.getFirst().getId()).isEqualTo(activeLikeUser.getId());
    assertThat(pageData.getFirst().getUsername()).isEqualTo(prefix + ".one");
  }

  @Test
  void
      givenExistingActiveUsernameWhenCreateUserWithSameUsernameThenThrowsUsernameAlreadyExists() {
    String seed = String.valueOf(System.currentTimeMillis());
    String username = "itest.duplicate." + seed;

    UserDTO createdUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(username)
                .fullName("Duplicate Username Base")
                .email("itest.duplicate.base." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.duplicate.giro123!")
                .build());

    userService.updateUser(
        createdUser.getId(),
        UpdateUserRequestDTO.builder()
            .username(username)
            .fullName("Duplicate Username Base")
            .email("itest.duplicate.base." + seed + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .status(Status.ACTIVE)
            .build());

    CreateUserRequestDTO duplicateCreateRequest =
        CreateUserRequestDTO.builder()
            .username(username)
            .fullName("Duplicate Username Second")
            .email("itest.duplicate.second." + seed + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .password("itest.duplicate.giro123!")
            .build();

    assertThatThrownBy(() -> userService.createUser(duplicateCreateRequest))
        .isInstanceOf(UsernameAlreadyExistsException.class);
  }

  @Test
  void
      givenAnotherActiveUserWithSameUsernameWhenUpdateUserStatusToActiveThenThrowsUsernameAlreadyExists() {
    String seed = String.valueOf(System.currentTimeMillis());
    String sharedUsername = "itest.update.active.conflict." + seed;

    UserDTO activeUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(sharedUsername)
                .fullName(ACTIVE_USERNAME_OWNER_FULL_NAME)
                .email("itest.active.owner." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.update.active.giro123!")
                .build());

    userService.updateUser(
        activeUser.getId(),
        UpdateUserRequestDTO.builder()
            .username(sharedUsername)
            .fullName(ACTIVE_USERNAME_OWNER_FULL_NAME)
            .email("itest.active.owner." + seed + EMAIL_DOMAIN)
            .orgUnitId(1L)
            .status(Status.ACTIVE)
            .build());

    UserDTO inactiveUserWithSameUsername =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(sharedUsername)
                .fullName("Inactive Same Username")
                .email("itest.inactive.same.username." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.update.active.giro123!")
                .build());

    assertThat(inactiveUserWithSameUsername.getStatus()).isEqualTo(Status.INACTIVE);

    UpdateUserRequestDTO activateWithDuplicateUsernameRequest =
        UpdateUserRequestDTO.builder().status(Status.ACTIVE).build();

    Long inactiveUserId = inactiveUserWithSameUsername.getId();
    assertThatThrownBy(
            () -> userService.updateUser(inactiveUserId, activateWithDuplicateUsernameRequest))
        .isInstanceOf(UsernameAlreadyExistsException.class);
  }

  @Test
  void
      givenActiveAndInactiveUsersWithSameUsernameWhenUpdatingInactiveWithoutActivationThenUpdateSucceeds() {
    String seed = String.valueOf(System.currentTimeMillis());
    String sharedUsername = "itest.update.inactive.allowed." + seed;

    UserDTO activeUser =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(sharedUsername)
                .fullName(ACTIVE_USERNAME_OWNER_FULL_NAME)
                .email("itest.inactive.allowed.active." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.update.inactive.giro123!")
                .build());

    userService.updateUser(
        activeUser.getId(), UpdateUserRequestDTO.builder().status(Status.ACTIVE).build());

    UserDTO inactiveUserWithSameUsername =
        userService.createUser(
            CreateUserRequestDTO.builder()
                .username(sharedUsername)
                .fullName("Inactive Same Username")
                .email("itest.inactive.allowed.inactive." + seed + EMAIL_DOMAIN)
                .orgUnitId(1L)
                .password("itest.update.inactive.giro123!")
                .build());

    UserDTO updatedInactiveUser =
        userService.updateUser(
            inactiveUserWithSameUsername.getId(),
            UpdateUserRequestDTO.builder()
                .fullName("Inactive Same Username Updated")
                .status(Status.INACTIVE)
                .build());

    assertThat(updatedInactiveUser.getStatus()).isEqualTo(Status.INACTIVE);
    assertThat(updatedInactiveUser.getUsername()).isEqualTo(sharedUsername);
    assertThat(updatedInactiveUser.getFullName()).isEqualTo("Inactive Same Username Updated");
  }
}
