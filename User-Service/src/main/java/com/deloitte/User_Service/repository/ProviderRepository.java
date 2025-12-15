package com.deloitte.User_Service.repository;

import com.deloitte.User_Service.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {

    @Query("SELECT pr FROM Provider pr WHERE pr.user_id.id = :userId")
    Optional<Provider> findByUser_id(@Param("userId") Long userId);
}

