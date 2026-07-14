package evm.main.compilations.controller;

import evm.main.compilations.dto.CompilationDto;
import evm.main.compilations.dto.NewCompilationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Compilations, admin, создание подборки событий: {}", newCompilationDto);
        return compilationService.addCompilation(newCompilationDto);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto updateCompilation(@PathVariable Long compilationId,
                                            @RequestBody @Valid UpdateCompilationRequest updateCompilation) {
        log.info("Compilations, admin, редактирование/изменение подборки событий id = {}", updateCompilation);
        return compilationService.updateCompilation(compilationId, updateCompilation);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compilationId) {
        log.info("Compilations, admin, удаление подборки событий id = {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }

}
