package io.ionic.starter;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import java.io.IOException;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MetaScanPlugin.class);
        super.onCreate(savedInstanceState);
        try {
            Runtime.getRuntime().exec("logcat -d");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
