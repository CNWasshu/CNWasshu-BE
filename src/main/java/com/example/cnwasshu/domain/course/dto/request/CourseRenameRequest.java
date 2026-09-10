package com.example.cnwasshu.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CourseRenameRequest(@NotBlank String courseName) {}