package com.synchrony.ParallelProcessingApplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synchrony.ParallelProcessingApplication.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long>{

}
