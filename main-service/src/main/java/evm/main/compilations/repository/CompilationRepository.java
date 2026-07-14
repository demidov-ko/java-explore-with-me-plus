package evm.main.compilations.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import evm.main.compilations.model.Compilation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompilationRepository extends JpaRepository<Compilation, Long>  {

    @Query("""
            SELECT DISTINCT c FROM Compilation c
            WHERE :pinned IS NULL OR c.pinned = :pinned
            """)
    Page<Compilation> findAllByPinned(
            @Param("pinned") @Nullable Boolean pinned,
            Pageable pageable);

}
