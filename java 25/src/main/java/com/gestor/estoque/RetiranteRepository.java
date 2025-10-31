package com.gestor.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RetiranteRepository extends JpaRepository<Retirante, Long> {
    // SELECT * FROM RETIRANTE WHERE NOME_RETIRANTE = ?
    Optional<Retirante> findByNOME_RETIRANTE(String nome);
}