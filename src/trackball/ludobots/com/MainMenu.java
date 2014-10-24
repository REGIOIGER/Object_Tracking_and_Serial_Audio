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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileOutputStream;  
import java.io.IOException;  

import android.widget.EditText;  
import android.widget.Toast; 

//import android.serialport.sample.R;  
import android_serialport_api.SerialPort; 

public class MainMenu extends Activity {
	
	
	 EditText mReception;  
     FileOutputStream mOutputStream;  
     FileInputStream mInputStream;  
     SerialPort sp;  

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      /*  
        try {  
            sp=new SerialPort(new File("/dev/ACM0"),9600, 0);  
            } catch (SecurityException e) {  
            	Toast.makeText(getApplicationContext(), "FALLO!",  
                        Toast.LENGTH_SHORT).show();    
                e.printStackTrace();  
            } catch (IOException e) {  
            	Toast.makeText(getApplicationContext(), "CRASH!",  
                        Toast.LENGTH_SHORT).show();  
                e.printStackTrace();  
            }     
        
        
        mOutputStream=(FileOutputStream) sp.getOutputStream();  
        mInputStream=(FileInputStream) sp.getInputStream();  
        
         Toast.makeText(getApplicationContext(), "/dev/ACM0 abierto",  
                  Toast.LENGTH_SHORT).show();  
         
         */

        final Button buttonSetup = (Button)findViewById(R.id.ButtonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this, SerialPortPreferences.class));
			}
		});

        final Button buttonConsole = (Button)findViewById(R.id.ButtonConsole);
        buttonConsole.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this, ConsoleActivity.class));
			}
		});
        
        final Button buttonTrack = (Button)findViewById(R.id.ButtonTrack);
        buttonTrack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(MainMenu.this, Tutorial2Activity.class));
			}
		});

        final Button buttonQuit = (Button)findViewById(R.id.ButtonQuit);
        buttonQuit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MainMenu.this.finish();
			}
		});
    }
}
