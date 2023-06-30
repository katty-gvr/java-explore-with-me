package ru.practicum.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.server.model.Hit;
import ru.practicum.server.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<Hit, Long> {
    @Query(" SELECT new ru.practicum.server.model.Stat(h.app, h.uri, COUNT(h.ip) AS hits) " +
            "FROM Hit h " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "AND h.uri IN(?3) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<Stat> findStat(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(" SELECT new ru.practicum.server.model.Stat(h.app, h.uri, COUNT(DISTINCT h.ip) AS hits) " +
            "FROM Hit h " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "AND h.uri IN(?3) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<Stat> findUniqueStat(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query(" SELECT new ru.practicum.server.model.Stat(h.app, h.uri, COUNT(h.ip) AS hits) " +
            "FROM Hit h " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.ip) DESC ")
    List<Stat> findAllStats(LocalDateTime start, LocalDateTime end);

    @Query(" SELECT new ru.practicum.server.model.Stat(h.app, h.uri, COUNT(DISTINCT h.ip) AS hits) " +
            "FROM Hit h " +
            "WHERE h.timestamp between ?1 AND ?2 " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC ")
    List<Stat> findAllUniqueStats(LocalDateTime start, LocalDateTime end);
}
