package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Query;

import dev.wdona.gestorinventarioqr.domain.model.Estanteria;

public interface EstanteriaDao {
    @Query("SELECT * FROM Estanteria WHERE id = :id")
    Estanteria getEstanteriaById(int id);
}
