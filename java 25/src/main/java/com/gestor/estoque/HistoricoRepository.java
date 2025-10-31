package com.gestor.estoque;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoRepository extends JpaRepository<HistoricoMovimentacao, Long> {
    // Não precisamos de métodos customizados por agora, o save() já vem incluído.
}