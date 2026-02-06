package com.example.validator;

import com.example.exception.ObjectNotFound;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.example.model.dto.sub_plans.SubscriptionPlanCreateDto;
import com.example.model.dto.sub_plans.SubscriptionPlanUpdateDto;
import com.example.model.entity.SubscriptionPlan;
import com.example.repository.SubscriptionPlanRepository;

@Component
@AllArgsConstructor
public class SubscriptionPlanValidator implements BaseValidator {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlan validateId(String id) {
        return subscriptionPlanRepository.findActiveById((id))
                .orElseThrow(() -> new ObjectNotFound("Subscription Plan Not Found"));
    }

    public void validateOnCreate(SubscriptionPlanCreateDto dto) {
        if (dto.getName()==null||dto.getName().isBlank()){
            throw new RuntimeException("Subscription Plan Name cannot be empty");
        }
        if (dto.getDescription()==null||dto.getDescription().isBlank()){
            throw new RuntimeException("Subscription Plan description cannot be empty");
        }

        if (dto.getPrice()==null){
            throw new RuntimeException("Subscription Plan price cannot be empty");
        }
    }

    public void validateOnUpdate(SubscriptionPlanUpdateDto dto) {
        if (dto.getName()==null||dto.getName().isBlank()){
            throw new RuntimeException("Subscription Plan Name cannot be empty");
        }
        if (dto.getDescription()==null||dto.getDescription().isBlank()){
            throw new RuntimeException("Subscription Plan description cannot be empty");
        }

        if (dto.getPrice()==null){
            throw new RuntimeException("Subscription Plan price cannot be empty");
        }
    }
}
