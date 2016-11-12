package orbus.example.computeiro.orbus;

import com.google.android.gms.maps.model.LatLng;

public class OnibusPosicaoTempo {
	private LatLng posicao;
	private Long tempo;

	public OnibusPosicaoTempo(LatLng posicao, Long tempo) {
		this.posicao = posicao;
		this.tempo = tempo;
	}

	public LatLng getPosicao() {
		return posicao;
	}

	public void setPosicao(LatLng posicao) {
		this.posicao = posicao;
	}

	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}
}
