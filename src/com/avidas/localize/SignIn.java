package com.avidas.localize;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SignIn extends Activity {
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        
        Button signIn = (Button) findViewById(R.id.sign_in);
        signIn.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		Context ctx = SignIn.this;
        		Intent intent = new Intent(ctx,MapActivity.class);
        		ctx.startActivity(intent);
        	}
        });
    }
}
