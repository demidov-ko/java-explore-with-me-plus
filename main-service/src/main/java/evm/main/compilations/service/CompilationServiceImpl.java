package evm.main.compilations.service;

import evm.main.compilations.dto.CompilationDto;
import evm.main.compilations.dto.NewCompilationDto;
import evm.main.compilations.model.Compilation;
import evm.main.compilations.repository.CompilationRepository;
import evm.main.event.mapper.EventMapper;
import evm.main.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        if (newCompilationDto.events() != null && !newCompilationDto.events().isEmpty()) {
            compilation.setEvents(eventRepository
                    .findAllByIdIn(newCompilationDto.events()));
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        List<Long> ids = savedCompilation
                .getEvents()
                .stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> confirmedRequests = requestRepository.getConfirmedRequestsCounts(ids);

        List<EventDtoShortWithoutViews> eventsWithRequests = savedCompilation.getEvents().stream()
                .map(event -> eventMapper.toDtoShort(
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
        Compilation compilation = findCompilationOrThrow(compilationId);

        List<EventDtoShortWithoutViews> preparedEvents = prepareEvents(compilation);

        return new CompilationDto(
                compilation.getId(),
                compilation.getPinned(),
                compilation.getTitle(),
                preparedEvents
        );
    }




}
