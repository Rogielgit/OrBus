package orbus.example.computeiro.orbus;

import com.google.android.gms.maps.model.Marker;

public class MarcaOrbus {
	private Marker marker;
	private PontoOrbus ponto;

	public MarcaOrbus(Marker marker, PontoOrbus ponto) {
		this.marker = marker;
		this.ponto = ponto;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public PontoOrbus getPonto() {
		return ponto;
	}

	public void setPonto(PontoOrbus ponto) {
		this.ponto = ponto;
	}
}