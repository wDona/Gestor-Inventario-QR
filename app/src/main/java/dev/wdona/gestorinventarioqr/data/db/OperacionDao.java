package dev.wdona.gestorinventarioqr.data.db;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public interface OperacionDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void addOperacionPendiente(Operacion operacionPendiente);
        @Query("SELECT * FROM Operacion_pendiente WHERE id = :id")
        Operacion getOperacionPendienteById(Long id);
        @Query("UPDATE Operacion_pendiente" +
                " SET estado = :nuevoEstado WHERE id = :id")
        void actualizarEstadoById(Long id, String nuevoEstado);
        @Query("SELECT * FROM Operacion_pendiente WHERE estado = 'PENDIENTE'")
        List<Operacion> getOperacionesPendientesSinEnviar();

        @Query("SELECT id FROM Operacion_pendiente ORDER BY id DESC LIMIT 1")
        Long getUltimoIdOperacionPendiente();
}
