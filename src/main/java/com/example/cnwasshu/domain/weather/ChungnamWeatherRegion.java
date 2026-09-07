package com.example.cnwasshu.domain.weather;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChungnamWeatherRegion {

    CHEONAN("천안시", 63, 110),
    GONGJU("공주시", 63, 102),
    BORYEONG("보령시", 54, 100),
    ASAN("아산시", 60, 110),
    SEOSAN("서산시", 51, 110),
    NONSAN("논산시", 62, 97),
    GYERYONG("계룡시", 65, 99),
    DANGJIN("당진시", 54, 112),
    GEUMSAN("금산군", 69, 95),
    BUYEO("부여군", 59, 99),
    SEOCHEON("서천군", 55, 94),
    CHEONGYANG("청양군", 57, 103),
    HONGSEONG("홍성군", 55, 106),
    YESAN("예산군", 58, 107),
    TAEAN("태안군", 48, 109);

    private final String regionName;
    private final int nx;
    private final int ny;
}