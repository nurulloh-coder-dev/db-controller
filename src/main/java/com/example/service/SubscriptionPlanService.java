package com.example.service;

import org.springframework.stereotype.Service;
import com.example.mapper.SubscriptionPlanMapper;
import com.example.model.dto.sub_plans.SubscriptionPlanCreateDto;
import com.example.model.dto.sub_plans.SubscriptionPlanDto;
import com.example.model.dto.sub_plans.SubscriptionPlanUpdateDto;
import com.example.model.entity.SubscriptionPlan;
import com.example.repository.SubscriptionPlanRepository;
import com.example.validator.SubscriptionPlanValidator;
import java.util.List;
@Service
public class SubscriptionPlanService extends AbstractService<
        SubscriptionPlanRepository,
        SubscriptionPlanMapper,
        SubscriptionPlanValidator> implements CRUDService<SubscriptionPlanDto, SubscriptionPlanCreateDto, SubscriptionPlanUpdateDto,String> {


    public SubscriptionPlanService(SubscriptionPlanRepository repository, SubscriptionPlanMapper mapper, SubscriptionPlanValidator validator) {
        super(repository, mapper, validator);
    }


    @Override
    public SubscriptionPlanDto create(SubscriptionPlanCreateDto dto) {
        validator.validateOnCreate(dto);
        SubscriptionPlan subscriptionPlan = mapper.ToEntity(dto);
        return mapper.toDto(repository.save(subscriptionPlan));
    }

    @Override
    public SubscriptionPlanDto update(String id, SubscriptionPlanUpdateDto dto) {
        validator.validateOnUpdate(dto);
        SubscriptionPlan subscriptionPlan = validator.validateId(id);
        mapper.mapUpdate(subscriptionPlan,dto);
        return mapper.toDto(repository.save(subscriptionPlan));
    }

    @Override
    public SubscriptionPlanDto get(String id) {
        return mapper.toDto(validator.validateId(id));
    }

    @Override
    public List<SubscriptionPlanDto> getAll() {
        return mapper.toDtoList(repository.findAllActivePlans());
    }

    @Override
    public void delete(String id) {

    }
}
