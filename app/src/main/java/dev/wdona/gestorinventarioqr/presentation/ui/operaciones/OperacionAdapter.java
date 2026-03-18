package dev.wdona.gestorinventarioqr.presentation.ui.operaciones;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.wdona.gestorinventarioqr.R;
import dev.wdona.gestorinventarioqr.data.EstadoOperacion;
import dev.wdona.gestorinventarioqr.data.TipoOperacion;
import dev.wdona.gestorinventarioqr.domain.model.Operacion;

public class OperacionAdapter extends RecyclerView.Adapter<OperacionAdapter.ViewHolder> {

    private List<Operacion> operaciones = new ArrayList<>();
    private OnOperacionClickListener listener;

    public interface OnOperacionClickListener {
        void onReintentarClick(Operacion operacion);
    }

    public OperacionAdapter(OnOperacionClickListener listener) {
        this.listener = listener;
    }

    public void setOperaciones(List<Operacion> operaciones) {
        this.operaciones = operaciones != null ? operaciones : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(operaciones.get(position));
    }

    @Override
    public int getItemCount() {
        return operaciones.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View estadoIndicator;
        private final TextView tvTipoOperacion;
        private final TextView tvEstado;
        private final TextView tvDetalles;
        private final TextView tvTimestamp;
        private final Button btnReintentar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            estadoIndicator = itemView.findViewById(R.id.estadoIndicator);
            tvTipoOperacion = itemView.findViewById(R.id.tvTipoOperacion);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvDetalles = itemView.findViewById(R.id.tvDetalles);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnReintentar = itemView.findViewById(R.id.btnReintentar);
        }

        void bind(Operacion operacion) {
            // Tipo de operación con texto legible
            String tipoTexto = getTipoTexto(operacion.getTipoOperacion());
            tvTipoOperacion.setText(tipoTexto);

            // Detalles en una línea compacta
            String detalles = "Prod. #" + operacion.getProductoId();
            if (operacion.getEstanteriaId() != null) {
                detalles += " · Est. #" + operacion.getEstanteriaId();
            }
            detalles += " · " + operacion.getCantidad() + " uds";
            tvDetalles.setText(detalles);

            // Timestamp relativo
            tvTimestamp.setText(getRelativeTime(operacion.getTimestamp()));

            // Estado con color
            String estado = operacion.getEstado();
            tvEstado.setText(getEstadoTexto(estado));
            int colorEstado = getColorEstado(estado);
            estadoIndicator.setBackgroundColor(colorEstado);

            // Chip de estado con color dinámico
            GradientDrawable chipBg = new GradientDrawable();
            chipBg.setShape(GradientDrawable.RECTANGLE);
            chipBg.setCornerRadius(20f);
            chipBg.setColor(colorEstado);
            tvEstado.setBackground(chipBg);

            // Mostrar botón reintentar solo si pendiente o fallida
            boolean puedeReintentar = estado.equals(EstadoOperacion.PENDIENTE.getValor())
                    || estado.equals(EstadoOperacion.FALLIDA.getValor());
            btnReintentar.setVisibility(puedeReintentar ? View.VISIBLE : View.GONE);

            btnReintentar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReintentarClick(operacion);
                }
            });
        }

        private String getTipoTexto(String tipo) {
            if (TipoOperacion.ADD.getValor().equals(tipo)) return "Añadir stock";
            if (TipoOperacion.REMOVE.getValor().equals(tipo)) return "Retirar stock";
            if (TipoOperacion.ASSIGN.getValor().equals(tipo)) return "Mover producto";
            return tipo;
        }

        private String getEstadoTexto(String estado) {
            if (EstadoOperacion.ENVIADA.getValor().equals(estado)) return "Enviada";
            if (EstadoOperacion.PENDIENTE.getValor().equals(estado)) return "Pendiente";
            if (EstadoOperacion.FALLIDA.getValor().equals(estado)) return "Fallida";
            return estado;
        }

        private int getColorEstado(String estado) {
            if (EstadoOperacion.ENVIADA.getValor().equals(estado)) {
                return ContextCompat.getColor(itemView.getContext(), R.color.status_enviada);
            } else if (EstadoOperacion.FALLIDA.getValor().equals(estado)) {
                return ContextCompat.getColor(itemView.getContext(), R.color.status_fallida);
            }
            return ContextCompat.getColor(itemView.getContext(), R.color.status_pendiente);
        }

        private String getRelativeTime(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (seconds < 60) return "Ahora";
            if (minutes < 60) return "Hace " + minutes + " min";
            if (hours < 24) return "Hace " + hours + "h";
            if (days < 7) return "Hace " + days + "d";

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
