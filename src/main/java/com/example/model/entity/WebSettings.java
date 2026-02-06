package com.example.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import com.example.model.entity.base.IdEntity;
import com.example.model.entity.enums.WebLang;
import com.example.model.entity.enums.WebTheme;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WebSettings extends IdEntity {
    @Enumerated(EnumType.STRING)
    private WebTheme theme;
    @Enumerated(EnumType.STRING)
    private WebLang language;
    private Boolean enableEmailing;
}
