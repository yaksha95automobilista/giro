package com.automobilista.giro.repository;

import com.automobilista.giro.dto.request.UserSearchRequestDTO;
import com.automobilista.giro.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

  Page<User> searchUsers(UserSearchRequestDTO searchRequest, Pageable pageable);
}
