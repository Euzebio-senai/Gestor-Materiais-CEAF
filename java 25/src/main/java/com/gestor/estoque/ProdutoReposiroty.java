package com.gestor.estoque;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    // O Spring entende isto e cria a query:
    // SELECT * FROM PRODUTO WHERE ID_PRODUTO = ?
    Optional<Produto> findByID_PRODUTO(Long id);
}