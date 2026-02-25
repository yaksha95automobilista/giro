package com.automobilista.giro.repository;

import com.automobilista.giro.dto.request.UserSearchRequestDTO;
import com.automobilista.giro.model.Role;
import com.automobilista.giro.model.Status;
import com.automobilista.giro.model.entity.QOrganizacionaJedinica;
import com.automobilista.giro.model.entity.QUser;
import com.automobilista.giro.model.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      new HashSet<>(
          List.of("id", "username", "fullName", "email", "orgUnitId", "orgUnitOj", "status"));

  private final RoleRepository roleRepository;
  @PersistenceContext private EntityManager entityManager;

  @Override
  public Page<User> searchUsers(UserSearchRequestDTO searchRequest, Pageable pageable) {
    JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
    QUser user = QUser.user;
    QOrganizacionaJedinica organizacionaJedinica = QOrganizacionaJedinica.organizacionaJedinica;
    BooleanBuilder predicate = buildPredicate(searchRequest, user, organizacionaJedinica);

    List<User> content =
        queryFactory
            .selectFrom(user)
            .leftJoin(user.organizacionaJedinica, organizacionaJedinica)
            .fetchJoin()
            .where(predicate)
            .orderBy(
                buildOrderSpecifier(
                    searchRequest.getSortField(),
                    searchRequest.getDirection(),
                    organizacionaJedinica))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total =
        queryFactory
            .select(user.count())
            .from(user)
            .leftJoin(user.organizacionaJedinica, organizacionaJedinica)
            .where(predicate)
            .fetchOne();
    long totalElements = total != null ? total : 0L;

    return new PageImpl<>(content, pageable, totalElements);
  }

  private BooleanBuilder buildPredicate(
      UserSearchRequestDTO searchRequest,
      QUser user,
      QOrganizacionaJedinica organizacionaJedinica) {
    BooleanBuilder predicate = new BooleanBuilder();

    if (searchRequest.getStatus() != null) {
      predicate.and(user.status.eq(searchRequest.getStatus()));
    } else {
      predicate.and(user.status.ne(Status.DELETED));
    }

    if (searchRequest.getId() != null) {
      predicate.and(user.id.eq(searchRequest.getId()));
    }
    if (hasText(searchRequest.getUsername())) {
      predicate.and(user.username.containsIgnoreCase(searchRequest.getUsername().trim()));
    }
    if (hasText(searchRequest.getFullName())) {
      predicate.and(user.fullName.containsIgnoreCase(searchRequest.getFullName().trim()));
    }
    if (hasText(searchRequest.getEmail())) {
      predicate.and(user.email.containsIgnoreCase(searchRequest.getEmail().trim()));
    }
    if (searchRequest.getOrgUnitId() != null) {
      predicate.and(organizacionaJedinica.id.eq(searchRequest.getOrgUnitId()));
    }
    if (hasText(searchRequest.getOrgUnitOj())) {
      predicate.and(
          organizacionaJedinica.oj.containsIgnoreCase(searchRequest.getOrgUnitOj().trim()));
    }
    if (searchRequest.getRoles() != null && !searchRequest.getRoles().isEmpty()) {
      Set<Long> userIds = findUserIdsByRoles(searchRequest.getRoles());
      if (userIds.isEmpty()) {
        predicate.and(user.id.isNull());
      } else {
        predicate.and(user.id.in(userIds));
      }
    }
    return predicate;
  }

  private OrderSpecifier<?> buildOrderSpecifier(
      String sortField, Sort.Direction direction, QOrganizacionaJedinica organizacionaJedinica) {
    String resolvedSortField =
        hasText(sortField) && ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "id";
    QUser user = QUser.user;
    boolean asc = direction == Sort.Direction.ASC;

    return switch (resolvedSortField) {
      case "username" -> asc ? user.username.asc() : user.username.desc();
      case "fullName" -> asc ? user.fullName.asc() : user.fullName.desc();
      case "email" -> asc ? user.email.asc() : user.email.desc();
      case "orgUnitId" -> asc ? organizacionaJedinica.id.asc() : organizacionaJedinica.id.desc();
      case "orgUnitOj" -> asc ? organizacionaJedinica.oj.asc() : organizacionaJedinica.oj.desc();
      case "status" -> asc ? user.status.asc() : user.status.desc();
      default -> asc ? user.id.asc() : user.id.desc();
    };
  }

  private boolean hasText(String value) {
    return value != null && !value.trim().isEmpty();
  }

  private Set<Long> findUserIdsByRoles(List<Role> roles) {
    List<String> roleCodes =
        roles.stream().filter(Objects::nonNull).map(Role::name).distinct().toList();
    if (roleCodes.isEmpty()) {
      return Set.of();
    }

    return new HashSet<>(roleRepository.findDistinctUserIdsByRoleCodes(roleCodes));
  }
}
