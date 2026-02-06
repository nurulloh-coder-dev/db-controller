package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.model.entity.WebSettings;

public interface WebSettingsRepository extends JpaRepository<WebSettings, String> {


}
