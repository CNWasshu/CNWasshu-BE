package com.example.cnwasshu.domain.timetable.service;

import java.util.List;

public interface SavedActivityQueryPort {

    List<SavedActivityInfo> findSavedActivities(Long userId);
}
