package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.model.entity.SubscriptionPlan;

import java.util.List;
import java.util.Optional;


public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan,String> {

    @Query(value = "select * from subscription_plas where active = true",nativeQuery = true)
    List<SubscriptionPlan> findAllActivePlans();

    @Query(value = "select * from subscription_plas where active = true and id = :id",nativeQuery = true)
    Optional<SubscriptionPlan> findActiveById(@Param("id") String id);
}
