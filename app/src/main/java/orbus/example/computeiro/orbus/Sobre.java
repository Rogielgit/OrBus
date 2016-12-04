package orbus.example.computeiro.orbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


/**
 * Created by computeiro on 04/12/16.
 */

public class Sobre extends AppCompatActivity implements View.OnClickListener{

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sobre);
        findViewById(R.id.voltar).setOnClickListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i==R.id.voltar){
            Intent intent = new Intent(Sobre.this, LoginActivity.class);
            startActivity(intent);

        }

    }
}
