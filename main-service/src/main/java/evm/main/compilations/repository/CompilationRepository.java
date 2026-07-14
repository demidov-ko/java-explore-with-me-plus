package evm.main.compilations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import evm.main.compilations.model.Compilation;

public class CompilationRepository extends JpaRepository<Compilation, Long>  {
}
