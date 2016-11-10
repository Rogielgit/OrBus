package orbus.example.computeiro.orbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import static android.R.attr.path;
import static orbus.example.computeiro.orbus.R.drawable.abrir;
import static orbus.example.computeiro.orbus.R.id.bCarregar;
import static orbus.example.computeiro.orbus.R.id.bPesquisar;
import static orbus.example.computeiro.orbus.R.id.bPonto;
import static orbus.example.computeiro.orbus.R.id.bSalvar;
import static orbus.example.computeiro.orbus.R.id.bSalvarFirebase;
import static orbus.example.computeiro.orbus.R.id.map;
import static orbus.example.computeiro.orbus.R.id.routeSpinner;

public class PesquisarActivity extends AppCompatActivity
		implements OnMapReadyCallback, LocationListener {

	private LatLng posicaoAtual = null;
	private Marker you = null;
	private GoogleMap mMap;
	private LocationManager locationManager;
	private DatabaseReference mDatabase;
	private HashMap<String,Route> routesMap = new HashMap<String,Route>();
	private ArrayList<PontoOrbus> pontos;
	private ArrayList<MarcaOrbus> marcas;
	private ArrayList<Onibus> listaOnibus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pesquisar);

		pontos = new ArrayList<PontoOrbus>();
		marcas = new ArrayList<MarcaOrbus>();
		listaOnibus = new ArrayList<Onibus>();

		mDatabase = FirebaseDatabase.getInstance().getReference();
		mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				routesMap.clear();
				Spinner routeSpinner = (Spinner)findViewById(R.id.routeSpinner);
				List<String> spinnerArray =  new ArrayList<String>();
				spinnerArray.add("Pesquisar...");

				for (DataSnapshot routes : snapshot.child("routes").getChildren()) {
					Route route = routes.getValue(Route.class);
					if (route!=null) {
						String name = route.getName();
						String value = route.getValue();

						spinnerArray.add(name);
						routesMap.put(name,route);

						String string = "Name: "+name+"\nValue: "+value+"\n\n";
						Log.v("Route",string);
					}
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					PesquisarActivity.this, android.R.layout.simple_spinner_item, spinnerArray);

				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				routeSpinner.setAdapter(adapter);
				findViewById(R.id.loadingPanel).setVisibility(View.GONE);
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e("RouteActivity","Erro lendo banco de dados.");
				finish();
			}
		});

		Button bPesquisar = (Button) findViewById(R.id.bPesquisar);
		bPesquisar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				((Spinner)findViewById(R.id.routeSpinner)).performClick();
			}
		});



		Spinner routeSpinner = (Spinner)findViewById(R.id.routeSpinner);
		routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
																 int position, long id) {
				String selected = parent.getSelectedItem().toString();
				if (!selected.equals("Pesquisar...")) {
					abrir(routesMap.get(selected));
					showToast("Rota Carregada!", Toast.LENGTH_SHORT);
					((Button)findViewById(R.id.bMenuEmbarcar)).setEnabled(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		// Inicializar mapa
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
			.findFragmentById(map);
		mapFragment.getMapAsync(this);

		// Inicializar localização
		Criteria criteria = new Criteria();

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String bestProvider = locationManager.getBestProvider(criteria, true);

		try {
			locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
			locationManager.requestLocationUpdates(
				LocationManager.GPS_PROVIDER, 2000, 1, this);
		} catch (SecurityException e) {
			Log.e("RouteActivity", "Permission Error");
		}






		// DELETAR // TODO
		Button bRoute = (Button) findViewById(R.id.bRota);
		bRoute.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				// Start NewActivity.class
				Intent myIntent = new Intent(PesquisarActivity.this,
					RouteActivity.class);
				startActivity(myIntent);
			}
		});
	}

	public void abrir(Route route) {
		pontos.clear();
		marcas.clear();

		String[] lines = route.getValue().split("\n");

		int i = 0;

		if (!lines[i].startsWith("<p>")) {
			showToast("Erro carregando rota.", Toast.LENGTH_SHORT);
			return;
		}
		i++;
		String line = lines[i];
		while (!line.equals("</p>")) {
			String p = line.trim();
			StringTokenizer st = new StringTokenizer(p, ";", false);

			Float lat = new Float(st.nextToken());
			Float longi = new Float(st.nextToken());

			LatLng ponto = new LatLng(lat, longi);

			PontoOrbus pontoOrbus = null;
			if (pontos.size()==0) {
				pontoOrbus = new PontoOrbus(ponto,0);
			}
			else {
				double distance = distance(ponto,pontos.get(pontos.size()-1).getPonto());
				pontoOrbus = new PontoOrbus(ponto,distance);
			}

			pontos.add(pontoOrbus);
			i++;
			line = lines[i];
		}
		i++;
		line = lines[i];

		if (!line.startsWith("<m>")) {
			showToast("Erro carregando rota", Toast.LENGTH_SHORT);
			return;
		}

		i++;
		line = lines[i];
		while (!line.equals("</m>")) {
			String p = line.trim();
			StringTokenizer st = new StringTokenizer(p, ";", false);

			String tipo = st.nextToken();
			String titulo = st.nextToken();

			Float lat = new Float(st.nextToken());
			Float longi = new Float(st.nextToken());
			LatLng position = new LatLng(lat, longi);

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

			for (int pIndex=0;pIndex<pontos.size();pIndex++) {
				if (distance(position,pontos.get(i).getPonto())<0.01) {
					MarcaOrbus mo = new MarcaOrbus(marca,pontos.get(i));
					marcas.add(mo);
					break;
				}
			}

			i++;
			line = lines[i];
		}

		exibeRota();
	}

	private Onibus getBusClosestToMark(MarcaOrbus mo) {
		double minDistance = 100000000;
		double distance;
		Onibus closestBus = null;

		for (int i=0; i<listaOnibus.size() ; i++) {
			Onibus onibus = listaOnibus.get(i);
			PontoOrbus po = getClosestPontoOrbus(onibus.getPosicao());
			distance = mo.getPonto().getDistanciaAteInicio() - po.getDistanciaAteInicio();
			if (distance<0)
				break;
			if (distance<minDistance) {
				minDistance = distance;
				closestBus = onibus;
			}
		}

		return closestBus;
	}

	private PontoOrbus getClosestPontoOrbus(LatLng posicao) {
		double minDistance = 100000000;
		double distance;
		PontoOrbus closestPO = null;
		for (int i=0;i<pontos.size();i++) {
			PontoOrbus po = pontos.get(i);
			distance = distance(po.getPonto(),posicao);
			if (distance<minDistance) {
				minDistance = distance;
				closestPO = po;
			}
		}
		return closestPO;
	}

	private float distance (LatLng p1, LatLng p2) {
		double lat_a = p1.latitude;
		double lat_b = p2.latitude;

		double lng_a = p1.longitude;
		double lng_b = p2.longitude;

		double earthRadius = 3958.75;
		double latDiff = Math.toRadians(lat_b-lat_a);
		double lngDiff = Math.toRadians(lng_b-lng_a);
		double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
			Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
				Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double distance = earthRadius * c;

		int meterConversion = 1609;

		return new Float(distance * meterConversion).floatValue();
	}

	private void exibeRota() {
		for (int i = 1; i < pontos.size(); i++) {
			LatLng ponto1 = pontos.get(i - 1).getPonto();
			LatLng ponto2 = pontos.get(i).getPonto();

			mMap.addPolyline((new PolylineOptions())
				.add(ponto1, ponto2).width(6).color(Color.argb(255, 255, 165, 0))
				.visible(true));
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		posicaoAtual = new LatLng(location.getLatitude(), location.getLongitude());

		if (you == null) {
			you = createBubbleMarker(posicaoAtual);
		} else {
			you.setPosition(posicaoAtual);
		}

		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(posicaoAtual, 15);
		mMap.animateCamera(update);
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

	public Marker createBubbleMarker(LatLng position) {
		IconGenerator iconGenerator = new IconGenerator(this);
		iconGenerator.setStyle(IconGenerator.STYLE_WHITE);
		Bitmap iconBitmap = iconGenerator.makeIcon("Você");

		int width = iconBitmap.getWidth() + 10;
		int height = iconBitmap.getHeight() + 10;

		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint color = new Paint();
		color.setColor(Color.BLACK);

		Paint bubble = new Paint();
		bubble.setColor(Color.WHITE);
		bubble.setStyle(Paint.Style.FILL);
		Paint bubbleBorder = new Paint();
		bubbleBorder.setColor(Color.BLACK);
		bubbleBorder.setStyle(Paint.Style.STROKE);
		bubbleBorder.setStrokeWidth(4);

		Path borderPath = new Path();
		borderPath.moveTo(height / 5f, height / 2f);
		borderPath.lineTo(height / 5f + height / 10f, height - 2f);
		borderPath.lineTo(height * 2f / 3f, height / 2f);
		borderPath.close();

		Path fillPath = new Path();
		fillPath.moveTo(height / 5f + 1f, height / 2f);
		fillPath.lineTo(height / 5f + height / 10f + 1f, height - 5f);
		fillPath.lineTo(height * 2f / 3f - 2f, height / 2f);
		fillPath.close();

		canvas.drawRoundRect(2f, 2f, width - 2f, height * 2f / 3f - 2f, height / 5f, height / 5f, bubbleBorder);
		canvas.drawPath(borderPath, bubbleBorder);
		canvas.drawPath(fillPath, bubble);
		canvas.drawRoundRect(4f, 4f, width - 4f, height * 2f / 3f - 4f, height / 5f, height / 5f, bubble);

		float areaWidth = width - 4f;
		float areaHeight = height * 2f / 3f - 2f;

		float[] textWidth = new float[1];
		float[] textHeight = new float[1];
		setTextSizeForWidth(color, width * 2f / 3f, "Você", textWidth, textHeight);

		float textX = (areaWidth - textWidth[0]) / 2f;
		float textY = areaHeight - ((areaHeight - textHeight[0]) / 2f);
		canvas.drawText("Você", textX, textY, color);

		Marker m = mMap.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.fromBitmap(bitmap)).zIndex(1000)
			.position(position).title("Você está aqui!")
			.anchor((height / 5f + height / 10f + 1f) / width, 1f));

		return m;
	}

	private void setTextSizeForWidth(Paint paint, float desiredWidth, String text,
																	 float[] textWidth, float[] textHeight) {
		final float testTextSize = 48f;

		// Get the bounds of the text, using our testTextSize.
		paint.setTextSize(testTextSize);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		// Calculate the desired size as a proportion of our testTextSize.
		float desiredTextSize = testTextSize * desiredWidth / bounds.width();

		// Set the paint for that size.
		paint.setTextSize(desiredTextSize);
		paint.getTextBounds(text, 0, text.length(), bounds);
		textWidth[0] = bounds.width();
		textHeight[0] = bounds.height();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	@Override
	public void onProviderEnabled(String s) {
		Toast.makeText(getBaseContext(), "Gps ativado! ",
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderDisabled(String s) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(intent);
		Toast.makeText(getBaseContext(), "Gps desativado! ",
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		LatLng saoCarlos = new LatLng(-22.007373, -47.894752);
		if (posicaoAtual!=null)
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicaoAtual, 15));
		else
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(saoCarlos, 15));
	}
}
