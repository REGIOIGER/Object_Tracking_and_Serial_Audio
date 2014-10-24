/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package trackball.ludobots.com;

import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.Button;

public class ConsoleActivity extends SerialPortActivity {

	EditText mReception;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        
        //**********************************
  /*      for(int i=0; i<5;i++){
            
            try {  
                //mOutputStream.write(new String("send").getBytes());  
                mOutputStream.write('h'); 
                mOutputStream.write('\n');
               // Toast.makeText(getApplicationContext(), "Foquito prendido...",  
               //         Toast.LENGTH_SHORT).show();
                try {
        			Thread.sleep(1000);
        		 } catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		 }
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            
            try {  
                //mOutputStream.write(new String("send").getBytes());  
                mOutputStream.write('l'); 
                mOutputStream.write('\n');
                //Toast.makeText(getApplicationContext(), "Foquito apagado...",  
                //        Toast.LENGTH_SHORT).show(); 
                try {
        			Thread.sleep(1000);
        		 } catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		 }
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
                
            }
        
        
        //**********************************
     */   
        
        
 		setContentView(R.layout.console);

		mReception = (EditText) findViewById(R.id.EditTextReception);
		EditText Emission = (EditText) findViewById(R.id.EditTextEmission);
		
		 
        
		
		Emission.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				int i;
				CharSequence t = v.getText();
				char[] text = new char[t.length()];
				for (i=0; i<t.length(); i++) {
					text[i] = t.charAt(i);
				}
				try {
					mOutputStream.write(new String(text).getBytes());
					mOutputStream.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
		
		final Button buttonAvance = (Button)findViewById(R.id.ButtonAvance);
        buttonAvance.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//startActivity(new Intent(MainMenu.this, SerialPortPreferences.class));
				try {
				mOutputStream.write('h'); 
                mOutputStream.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

        final Button buttonRetroceso = (Button)findViewById(R.id.ButtonRetroceso);
        buttonRetroceso.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//startActivity(new Intent(MainMenu.this, ConsoleActivity.class));
				try {
				mOutputStream.write('l'); 
                mOutputStream.write('\n');
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		
		
		
	}

	@Override
	protected void onDataReceived(final byte[] buffer, final int size) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (mReception != null) {
					mReception.append(new String(buffer, 0, size));
				}
			}
		});
	}
}
