package com.expensemanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank
    private String name;
    private String icon = "tag";
    private String color = "#6366F1";
}
