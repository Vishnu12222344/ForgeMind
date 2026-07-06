package com.forgemind.users.repository;

import com.forgemind.users.entity.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, UUID> {
}