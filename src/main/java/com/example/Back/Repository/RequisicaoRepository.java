package com.example.Back.Repository;

import com.example.Back.Entity.Requisicao;
// ✅ 1. IMPORTS ADICIONADOS
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, Long> {

    // ✅ 2. O MÉTODO DEVE FICAR AQUI DENTRO
    Page<Requisicao> findByStatus(String status, Pageable pageable);

} // ✅ 3. A INTERFACE FECHA AQUI