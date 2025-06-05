package ru.practicum.main.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.mapper.CompilationMapper;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.NotFoundException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Transactional(rollbackFor = {NotFoundException.class})
    @Override
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Set<Event> events = Collections.emptySet();
        if (dto.getEvents() != null && !dto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findByIdIn(dto.getEvents().stream().toList()));
        }
        Compilation compilation = Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null ? dto.getPinned() : false)
                .events(events)
                .build();
        Compilation saved = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(saved);
    }

    @Transactional(rollbackFor = {NotFoundException.class})
    @Override
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional(rollbackFor = {NotFoundException.class})
    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest dto) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        if (dto.getTitle() != null) {
            compilation.setTitle(dto.getTitle());
        }
        if (dto.getPinned() != null) {
            compilation.setPinned(dto.getPinned());
        }
        if (dto.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findByIdIn(dto.getEvents().stream().toList()));
            compilation.setEvents(events);
        }
        Compilation updated = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updated);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        if (pinned != null) {
            return compilationRepository.findByPinned(pinned, pageRequest)
                    .stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        } else {
            return compilationRepository.findAll(pageRequest)
                    .stream()
                    .map(compilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation not found"));
        return compilationMapper.toCompilationDto(compilation);
    }
}
