package com.smartcity.dto;

import com.smartcity.model.Skill;
import lombok.Data;
import java.util.Set;

@Data
public class SkillsUpdateRequest {
    private Set<Skill> skills;
}
