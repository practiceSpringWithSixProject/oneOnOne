package com.fromapril.member.repository;

import com.fromapril.member.domain.member.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile,Long> {
}
