package com.avidas.localize;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SignIn extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
       // ImageView imageView1 = new ImageView(this);
       // imageView1.setBackgroundResource(R.drawable.logo); 
       // LinearLayout layout = new LinearLayout(this);
       // layout.addView(imageView1, imageParams1);
        
        setContentView(R.layout.signin);
        Button signin = (Button) findViewById(R.id.signin);
        signin.setTextSize(30);    
    }
    
    public void onClickSignIn(View view) {
    	Intent intent = new Intent(this, DevAroundActivity.class);
    	//EditText editText = (EditText) findViewById(R.id.edit_messages);
    	//String message = editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
}
