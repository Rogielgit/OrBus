package orbus.example.computeiro.orbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import static android.os.Build.VERSION_CODES.M;
import static orbus.example.computeiro.orbus.R.id.map;

public class RouteActivity extends AppCompatActivity
	implements OnMapReadyCallback, LocationListener,
	FileSaveFragment.Callbacks,	FileSelectFragment.Callbacks{
	private GoogleMap mMap;
	private LocationManager locationManager;
	private LatLng prevLatLng = new LatLng(0,0);
	private boolean started = false;
	private boolean StartAllowed = false;
	private Button bInicio;
	private Button bFim;
	private Button bPonto;
	private Button bSalvar;
	private Button bCarregar;
	private int ponto = 1;
	private ArrayList<LatLng> pontos;
	private ArrayList<Marker> marcas;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);

		pontos = new ArrayList<LatLng>();
		marcas = new ArrayList<Marker>();


		bInicio = (Button) findViewById(R.id.bIniciar);
		bFim = (Button) findViewById(R.id.bFinalizar);
		bPonto = (Button) findViewById(R.id.bPonto);
		bSalvar = (Button) findViewById(R.id.bSalvar);
		bCarregar = (Button) findViewById(R.id.bCarregar);

		bInicio.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				started = true;
				pontos.clear();
				marcas.clear();
				mMap.clear();

				pontos.add(prevLatLng);

				Marker marker = mMap.addMarker(new MarkerOptions()
					.position(prevLatLng)
					.title("Início")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				marker.setTag("inicio");

				marcas.add(marker);

				ponto = 1;

				bInicio.setEnabled(false);
				bPonto.setEnabled(true);
				bFim.setEnabled(true);
				bSalvar.setEnabled(false);
				bCarregar.setEnabled(false);
			}
		});

		bFim.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				started = false;

				Marker marker = mMap.addMarker(new MarkerOptions()
					.position(prevLatLng)
					.title("Fim")
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				marker.setTag("fim");

				marcas.add(marker);

				String ext = ".orbm";
				String fragTag = "Salvar...";

				FileSaveFragment fsf = FileSaveFragment.newInstance(ext,
					R.string.salvarOk,
					R.string.salvarCancelar,
					R.string.salvarTitulo,
					R.string.salvarEditar,
					R.drawable.salvar);

				fsf.show(getFragmentManager(), fragTag);

				bInicio.setEnabled(true);
				bPonto.setEnabled(false);
				bFim.setEnabled(false);
				bSalvar.setEnabled(true);
				bCarregar.setEnabled(true);
			}
		});

		bPonto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Marker marker = mMap.addMarker(new MarkerOptions()
					.position(prevLatLng)
					.title("Ponto "+ponto)
					.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
				marker.setTag("ponto");

				marcas.add(marker);
				ponto++;
			}
		});

		bSalvar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				String ext = ".orbm";
				String fragTag = "Salvar...";

				FileSaveFragment fsf = FileSaveFragment.newInstance(ext,
					R.string.salvarOk,
					R.string.salvarCancelar,
					R.string.salvarTitulo,
					R.string.salvarEditar,
					R.drawable.salvar);

				fsf.show(getFragmentManager(), fragTag);
			}
		});

		bCarregar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				String fragTag = "Abrir...";

				FileSelectFragment fsf = FileSelectFragment.newInstance(FileSelectFragment.Mode.FileSelector,
					R.string.abrirOk,
					R.string.abrirCancelar,
					R.string.abrirTitulo,
					R.drawable.abrir,R.drawable.pasta,R.drawable.arquivo);

				ArrayList<String> allowedExtensions = new ArrayList<String>();
				allowedExtensions.add(".orbm");
				fsf.setFilter(FileSelectFragment.FiletypeFilter(allowedExtensions));

				fsf.show(getFragmentManager(), fragTag);
			}
		});

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
			.findFragmentById(map);
		mapFragment.getMapAsync(this);

		Criteria criteria = new Criteria();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String bestProvider = locationManager.getBestProvider(criteria, true);

		try {
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER,2000, 1, this);
		} catch (SecurityException e) {
			Log.e("RouteActivity", "Permission Error");
		}
	}

	@Override
	public boolean onCanSave(String absolutePath, String fileName) {
		boolean canSave = true;

		// Catch the really stupid case.
		if (absolutePath == null || absolutePath.length() ==0 ||
			fileName == null || fileName.length() == 0) {
			canSave = false;
			showToast("Digite o nome do arquivo", Toast.LENGTH_SHORT);
		}

		// Do we have a filename if the extension is thrown away?
		if (canSave) {
			String copyName = FileSaveFragment.NameNoExtension(fileName);
			if (copyName == null || copyName.length() == 0 ) {
				canSave = false;
				showToast("Digite o nome do arquivo", Toast.LENGTH_SHORT);
			}
		}

		// Allow only alpha-numeric names. Simplify dealing with reserved path
		// characters.
		if (canSave) {
			if (!FileSaveFragment.IsAlphaNumeric(fileName)) {
				canSave = false;
				showToast("Use somente números ou letras no nome do arquivo", Toast.LENGTH_SHORT);
			}
		}

		return canSave;
	}

	@Override
	public void onConfirmSave(String absolutePath, String filename) {
		if (absolutePath==null || filename==null)
			return;

		salvar(absolutePath,filename);
	}

	public boolean salvar(String path, String filename) {
		if (pontos.size()<2) {
			showToast("Rota sem pontos.", Toast.LENGTH_SHORT);
			mMap.clear();
			return false;
		}

		File outputfile = null;
		if (filename.endsWith(".orbm"))
			outputfile = new File(path+"/"+filename);
		else
			outputfile = new File(path+"/"+filename+".orbm");

		FileOutputStream fos;

		String auxNewLine = new String("\n");
		try {
			if ( !outputfile.exists() ){
				outputfile.createNewFile();
			}

			fos = new FileOutputStream(outputfile, false);
			OutputStreamWriter writer = new OutputStreamWriter(fos,"latin1");

			writer.write("<p>"+auxNewLine);
			for (int i=0;i<pontos.size();i++) {
				LatLng ponto = pontos.get(i);
				writer.write(ponto.latitude+";"+ponto.longitude+auxNewLine);
			}
			writer.write("</p>"+auxNewLine);
			writer.write("<m>"+auxNewLine);
			for (int i=0;i<marcas.size();i++) {
				Marker marca = marcas.get(i);
				String tipo = (String)marca.getTag();
				String titulo = marca.getTitle();
				String position = marca.getPosition().latitude+";"+marca.getPosition().longitude;
				writer.write(tipo+";"+titulo+";"+position+auxNewLine);
			}
			writer.write("</m>"+auxNewLine);

			writer.close();
			fos.close();

			showToast("Arquivo Salvo", Toast.LENGTH_SHORT);
		}
		catch (IOException e) {
			e.printStackTrace();
			showToast("Erro de gravação.", Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	public void abrir(String path, String filename)	{
		pontos.clear();
		marcas.clear();
		File file = new File(path+"/"+filename);
		Scanner inputFile = null;
		String line = "";

		try {
			inputFile = new Scanner(file,"latin1");

			line = inputFile.nextLine();
			if (!line.startsWith("<p>")) {
				showToast("Arquivo inválido", Toast.LENGTH_SHORT);
				return;
			}
			line = inputFile.nextLine();
			while (!line.equals("</p>")) {
				String p = line.trim();
				StringTokenizer st = new StringTokenizer(p,";",false);

				Float lat = new Float(st.nextToken());
				Float longi = new Float(st.nextToken());

				LatLng ponto = new LatLng(lat,longi);
				pontos.add(ponto);
				line = inputFile.nextLine();
			}
			line = inputFile.nextLine();
			if (!line.startsWith("<m>")) {
				showToast("Arquivo inválido", Toast.LENGTH_SHORT);
				return;
			}
			line = inputFile.nextLine();
			while (!line.equals("</m>")) {
				String p = line.trim();
				StringTokenizer st = new StringTokenizer(p,";",false);

				String tipo = st.nextToken();
				String titulo = st.nextToken();

				Float lat = new Float(st.nextToken());
				Float longi = new Float(st.nextToken());
				LatLng position = new LatLng(lat,longi);

				float color = 0f;

				if (tipo.startsWith("inicio"))
					color = BitmapDescriptorFactory.HUE_GREEN;
				else if (tipo.startsWith("fim"))
					color = BitmapDescriptorFactory.HUE_RED;
				else
					color = BitmapDescriptorFactory.HUE_BLUE;

				Marker marca = mMap.addMarker(new MarkerOptions()
					.position(position)
					.title(titulo)
					.icon(BitmapDescriptorFactory.defaultMarker(color)));
				marca.setTag(tipo);

				marcas.add(marca);
				line = inputFile.nextLine();
			}
			inputFile.close();

			exibeRota();
			bSalvar.setEnabled(true);
		}
		catch(Exception e) {
			if (inputFile != null)
				inputFile.close();
			showToast("Arquivo inválido", Toast.LENGTH_SHORT);
			return;
		};
	}

	private void exibeRota() {
		for (int i=1;i<pontos.size();i++) {
			LatLng ponto1 = pontos.get(i-1);
			LatLng ponto2 = pontos.get(i);

			mMap.addPolyline((new PolylineOptions())
				.add(ponto1, ponto2).width(6).color(Color.argb(255,255,165,0))
				.visible(true));
		}
	}

	private void showToast(String text, int duration) {
		Toast toast = Toast.makeText(this, text, duration);
		toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);

		TextView textView = new TextView(this);
		textView.setBackgroundColor(Color.DKGRAY);
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(30);
		Typeface typeface = Typeface.create("serif", Typeface.BOLD);
		textView.setTypeface(typeface);
		textView.setPadding(10, 10, 10, 10);
		textView.setText(text);

		toast.setView(textView);
		toast.show();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		LatLng saoCarlos = new LatLng(-22.007373, -47.894752);
		prevLatLng = saoCarlos;
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(saoCarlos,15));
	}


	@Override
	public void onLocationChanged(Location location) {
		if (!StartAllowed) {
			StartAllowed = true;
			bInicio.setEnabled(true);
			LatLng currLatLng = new LatLng(location.getLatitude(), location.getLongitude());
			prevLatLng = new LatLng(currLatLng.latitude, currLatLng.longitude);
		}

		LatLng currLatLng = new LatLng(location.getLatitude(), location.getLongitude());

		if(started) {
			mMap.addPolyline((new PolylineOptions())
				.add(prevLatLng, currLatLng).width(6).color(Color.argb(255,255,165,0))
				.visible(true));
			prevLatLng = new LatLng(currLatLng.latitude, currLatLng.longitude);

			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currLatLng, 15);
			mMap.animateCamera(update);

			//mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng,15));

			pontos.add(currLatLng);
		}
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {}

	@Override
	public void onProviderEnabled(String s) {
		Toast.makeText(getBaseContext(), "Gps is turned on!! ",
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String s) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
		Toast.makeText(getBaseContext(), "Gps is turned off!! ",
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConfirmSelect(String absolutePath, String filename) {
		if (absolutePath==null || filename==null)
			return;

		mMap.clear();
		abrir(absolutePath,filename);
	}

	@Override
	public boolean isValid(String absolutePath, String fileName) {
		return true;
	}
}
