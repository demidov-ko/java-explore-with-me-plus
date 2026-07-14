package evm.main.compilations.service;

import evm.main.compilations.dto.CompilationDto;
import evm.main.compilations.dto.NewCompilationDto;

import java.util.List;

public interface CompilationService {

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    CompilationDto getCompilationById(Long compilationId);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto updateCompilation(Long compId, NewCompilationDto updateCompilation);

    void deleteCompilation(Long compilationId);
}
