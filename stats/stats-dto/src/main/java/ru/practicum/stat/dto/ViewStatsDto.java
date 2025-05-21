package ru.practicum.stat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ViewStatsDto {
    private String app;
    private String uri;
    private Integer hits;
}
