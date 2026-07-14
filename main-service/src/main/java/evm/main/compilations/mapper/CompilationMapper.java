package evm.main.compilations.mapper;

import evm.main.category.dto.CategoryDto;
import evm.main.compilations.dto.CompilationDto;
import evm.main.compilations.dto.NewCompilationDto;
import evm.main.compilations.model.Compilation;
import evm.main.event.dto.EventShortDto;
import evm.main.event.model.Event;
import evm.main.users.dto.UserShortDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {

    public Compilation toEntity(CompilationDto dto, Set<Event> eventsFromDb) {
        if (dto == null) {
            return null;
        }

        Compilation entity = new Compilation();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        // eventsFromDb — это уже загруженные сущности Event по ID из DTO
        if (eventsFromDb != null) {
            entity.getEvents().clear();
            entity.getEvents().addAll(eventsFromDb);
        } else {
            // Если нет переданных событий, оставляем как есть (пустое множество)
            entity.getEvents().clear();
        }

        return entity;
    }

    public Compilation toEntity(NewCompilationDto dto) {
        if (dto == null) {
            return null;
        }

        Compilation entity = new Compilation();
        entity.setTitle(dto.getTitle());
        entity.setPinned(dto.getPinned() != null ? dto.getPinned() : false);

        //как быть с событиями, как их мапить?
        return entity;
    }

    public CompilationDto toDto(Compilation entity) {
        if (entity == null) {
            return null;
        }

        List<EventShortDto> eventDtos = Objects.nonNull(entity.getEvents())
                ? entity.getEvents().stream()
                .filter(Objects::nonNull)
                .map(this::toEventShortDto)
                .collect(Collectors.toList())
                : List.of();

        return CompilationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .pinned(entity.getPinned())
                .events(eventDtos)
                .build();
    }

    private EventShortDto toEventShortDto(Event event) {
        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        //CategoryDto category???
        //confirmedRequests???
        dto.setEventDate(event.getEventDate());
        //UserShortDto initiator;???
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle();
        //views;??
        return dto;
    }
}
