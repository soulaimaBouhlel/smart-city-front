package com.smartcity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAdminDTO extends AdminDTO {
    // UserAdmin specific fields can be added here
}