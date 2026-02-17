package dev.wdona.gestorinventarioqr.data.repository;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaRepository {

        Estanteria getEstanteriaById(Long id);
}
