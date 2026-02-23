package com.automobilista.giro.model.entity;

import com.automobilista.giro.model.Status;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(updatable = false, nullable = false)
  private Long id;

  @Column(nullable = false)
  private String username;

  private String fullName;

  @Column(unique = true, nullable = false)
  private String email;

  private String password;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "org_unit_id", nullable = false)
  private OrganizacionaJedinica organizacionaJedinica;

  @Column(name = "status", nullable = false)
  @Builder.Default
  private Status status = Status.ACTIVE;

  @Transient private List<SimpleGrantedAuthority> grantedAuthorities;

  @Override
  public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
    if (grantedAuthorities != null && !grantedAuthorities.isEmpty()) {
      return grantedAuthorities;
    }
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public @NonNull String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return getStatus() == Status.ACTIVE;
  }

  @Override
  public boolean isAccountNonLocked() {
    return getStatus() == Status.ACTIVE;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return getStatus() == Status.ACTIVE;
  }

  @Override
  public boolean isEnabled() {
    return getStatus() == Status.ACTIVE;
  }
}
