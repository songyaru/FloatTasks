package none.floattasks;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {

        super.onResume();

        Intent is = new Intent(this, SystemOverlayMenuService.class);
        startService(is);

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

    }

}
