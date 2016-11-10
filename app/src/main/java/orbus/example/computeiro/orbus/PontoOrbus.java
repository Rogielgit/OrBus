package orbus.example.computeiro.orbus;

import com.google.android.gms.maps.model.LatLng;

public class PontoOrbus {
	private LatLng ponto;
	private double distanciaAteInicio;

	public PontoOrbus(LatLng ponto, double distanciaAteInicio) {
		this.ponto = ponto;
		this.distanciaAteInicio = distanciaAteInicio;
	}

	public LatLng getPonto() {
		return ponto;
	}

	public void setPonto(LatLng ponto) {
		this.ponto = ponto;
	}

	public double getDistanciaAteInicio() {
		return distanciaAteInicio;
	}

	public void setDistanciaAteInicio(double distanciaAteInicio) {
		this.distanciaAteInicio = distanciaAteInicio;
	}
}