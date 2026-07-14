package evm.main.compilations.service;

import evm.main.compilations.dto.CompilationDto;
import evm.main.compilations.dto.NewCompilationDto;
import evm.main.compilations.dto.UpdateCompilationRequest;
import evm.main.compilations.mapper.CompilationMapper;
import evm.main.compilations.model.Compilation;
import evm.main.compilations.repository.CompilationRepository;
import evm.main.event.dto.EventShortDto;
import evm.main.event.mapper.EventMapper;
import evm.main.event.model.Event;
import evm.main.event.repository.EventRepository;
import evm.main.exceptions.NotFoundException;
import evm.main.requests.repository.RequestRepositoryJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final RequestRepositoryJpa requestRepository;

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            compilation.setEvents(eventRepository
                    .findAllByIdIn(newCompilationDto
                            .getEvents().toList()
                    );
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        List<Long> ids = savedCompilation
                .getEvents()
                .stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.countConfirmedByEventIds(ids);

        List<EventShortDto> eventsWithRequests = savedCompilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(
                        event, confirmedRequests
                                .getOrDefault(event.getId(), 0L)))
                .toList();

        return new CompilationDto(savedCompilation.getId(),
                savedCompilation.getPinned(),
                savedCompilation.getTitle(),
                eventsWithRequests);
    }

    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = findCompilationOrRaiseException(compilationId);

        List<EventShortDto> preparedEvents = prepareEvents(compilation);

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));

        List<Compilation> compilations = compilationRepository
                .findAllByPinned(pinned, pageable)
                .getContent();

        List<Long> eventsIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.countConfirmedByEventIds(eventsIds);

        return compilations.stream()
                .map(compilation -> new CompilationDto(
                        compilation.getId(),
                        compilation.getPinned(),
                        compilation.getTitle(),
                        compilation.getEvents().stream()
                                .map(event -> eventMapper
                                        .toShortDto(
                                                event, confirmedRequests
                                                        .getOrDefault(event.getId(), 0L)))
                                .toList()))
                .toList();
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation) {
        Compilation compilation = findCompilationOrRaiseException(compId);

        if (updateCompilation.getEvents() != null) {
            Set<Event> events = updateCompilation.getEvents().stream()
                    .map(id -> {
                        Event event = new Event();
                        event.setId(id);
                        return event;
                    })
                    .collect(Collectors.toSet());
            compilation.setEvents(events);
        }

        if (updateCompilation.getPinned() != null) {
            compilation.setPinned(updateCompilation.getPinned());
        }

        String title = updateCompilation.getTitle();
        if (title != null && !title.isBlank()) {
            compilation.setTitle(title);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);

        List<EventShortDto> preparedEvents = prepareEvents(updatedCompilation);

        return new CompilationDto(
                updatedCompilation.getId(),
                updatedCompilation.getPinned(),
                updatedCompilation.getTitle(),
                preparedEvents
        );
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        findCompilationOrRaiseException(compilationId);
        compilationRepository.deleteById(compilationId);
    }

    private Compilation findCompilationOrRaiseException(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id = " + compilationId.toString() + " не найдена!"));
    }

    private List<EventShortDto> prepareEvents(Compilation compilation) {
        if (compilation.getEvents() == null || compilation.getEvents().isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> confirmedRequests = requestRepository.countConfirmedByEventIds(ids);

        return compilation.getEvents().stream()
                .map(event -> eventMapper.toShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L)))
                .toList();
    }


}
