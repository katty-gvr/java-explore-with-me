package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitDto;
import ru.practicum.dto.StatDto;
import ru.practicum.server.mapper.HitMapper;
import ru.practicum.server.mapper.StatMapper;
import ru.practicum.server.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    @Transactional
    public void addHit(HitDto hitDto) {
        statRepository.save(HitMapper.toHit(hitDto));

    }

    @Override
    public List<StatDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            return unique ? statRepository.findAllUniqueStats(start, end).stream().map(StatMapper::toStatDto).collect(Collectors.toList())
                    : statRepository.findAllStats(start, end).stream().map(StatMapper::toStatDto).collect(Collectors.toList());
        } else {
            return unique ? statRepository.findUniqueStat(start, end, uris).stream().map(StatMapper::toStatDto).collect(Collectors.toList())
                    : statRepository.findStat(start, end, uris).stream().map(StatMapper::toStatDto).collect(Collectors.toList());
        }
    }

/*    @Override
    public List<StatDto> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (uris == null || uris.isEmpty()) {
            return unique ? statRepository.findAllUniqueStats(start, end)
                    : statRepository.findAllStats(start, end);
        } else {
            return unique ? statRepository.findUniqueStat(start, end, uris)
                    : statRepository.findStat(start, end, uris);
        }
    }*/
}
