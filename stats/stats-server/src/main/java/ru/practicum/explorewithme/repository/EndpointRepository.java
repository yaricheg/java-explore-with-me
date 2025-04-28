package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EndpointRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT e.app AS app, e.uri AS uri, COUNT(e) AS hits " +
            "FROM EndpointHit AS e " +
            "where e.timestamp between :start and :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsInterface> getAllViewStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.app AS app, e.uri AS uri, COUNT(e) AS hits " +
            "FROM EndpointHit AS e " +
            "where (e.timestamp between :start and :end) and uri in :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsInterface> getViewStatsByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);

    @Query("SELECT e.app AS app, e.uri AS uri, COUNT(DISTINCT e.ip) AS hits " +
            "FROM EndpointHit AS e " +
            "where e.timestamp between :start and :end " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsInterface> getViewStatsUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e.app AS app, e.uri AS uri, COUNT(DISTINCT e.ip) AS hits " +
            "FROM EndpointHit AS e " +
            "where (e.timestamp between :start and :end) and uri in :uris " +
            "GROUP BY e.app, e.uri " +
            "ORDER BY hits DESC")
    List<ViewStatsInterface> getViewStatsByUrisUnique(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("uris") List<String> uris);
}
