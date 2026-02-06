package com.example.mapper;

import org.springframework.stereotype.Component;
import com.example.model.dto.sub_plans.SubscriptionPlanCreateDto;
import com.example.model.dto.sub_plans.SubscriptionPlanDto;
import com.example.model.dto.sub_plans.SubscriptionPlanUpdateDto;
import com.example.model.entity.SubscriptionPlan;

import java.util.List;

@Component
public class SubscriptionPlanMapper implements BaseMapper {
    public SubscriptionPlanDto toDto(SubscriptionPlan subscriptionPlan) {
        SubscriptionPlanDto subscriptionPlanDto = new SubscriptionPlanDto();
        subscriptionPlanDto.setId(subscriptionPlan.getId());
        subscriptionPlanDto.setName(subscriptionPlan.getName());
        subscriptionPlanDto.setDescription(subscriptionPlan.getDescription());
        subscriptionPlanDto.setPrice(subscriptionPlan.getPrice());
        subscriptionPlanDto.setMaxUsers(subscriptionPlan.getMaxUsers());
        subscriptionPlanDto.setMaxDatabases(subscriptionPlan.getMaxDatabases());
        subscriptionPlanDto.setMaxMembersPerDatabase(subscriptionPlan.getMaxMembersPerDatabase());
        return subscriptionPlanDto;
    }

    public List<SubscriptionPlanDto> toDtoList(List<SubscriptionPlan> allActivePlans) {
        return allActivePlans
                .stream()
                .map(this::toDto)
                .toList();
    }

    public SubscriptionPlan ToEntity(SubscriptionPlanCreateDto dto) {
        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();
        subscriptionPlan.setName(dto.getName());
        subscriptionPlan.setDescription(dto.getDescription());
        subscriptionPlan.setPrice(dto.getPrice());
        subscriptionPlan.setMaxUsers(dto.getMaxUsers());
        subscriptionPlan.setMaxDatabases(dto.getMaxDatabases());
        subscriptionPlan.setMaxMembersPerDatabase(dto.getMaxMemberPerDatabase());
        return subscriptionPlan;
    }

    public void mapUpdate(SubscriptionPlan subscriptionPlan, SubscriptionPlanUpdateDto dto) {
        if (dto.getName() != null) {
            subscriptionPlan.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            subscriptionPlan.setDescription(dto.getDescription());
        }
        if (dto.getPrice() != null) {
            subscriptionPlan.setPrice(dto.getPrice());
        }
        if (dto.getMaxUsers() != null) {
            subscriptionPlan.setMaxUsers(dto.getMaxUsers());
        }
        if (dto.getMaxDatabases() != null) {
            subscriptionPlan.setMaxDatabases(dto.getMaxDatabases());
        }
        if (dto.getMaxMembersPerDatabase() != null) {
            subscriptionPlan.setMaxMembersPerDatabase(dto.getMaxMembersPerDatabase());
        }
    }
}
