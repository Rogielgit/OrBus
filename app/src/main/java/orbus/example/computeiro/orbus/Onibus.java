package orbus.example.computeiro.orbus;

import com.google.android.gms.maps.model.LatLng;

public class Onibus {
	private LatLng posicao;
	private float velocidade;

	public Onibus(LatLng posicao, float velocidade) {
		this.posicao = posicao;
		this.velocidade = velocidade;
	}

	public LatLng getPosicao() {
		return posicao;
	}

	public void setPosicao(LatLng posicao) {
		this.posicao = posicao;
	}

	public float getVelocidade() {
		return velocidade;
	}

	public void setVelocidade(float velocidade) {
		this.velocidade = velocidade;
	}
}
