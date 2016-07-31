package work.wanghao.autoupdatedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import im.fir.sdk.FIR;
import work.wanghao.autoupdate.UpdateManager;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FIR.init(this);
    setContentView(R.layout.activity_main);
    Button button = (Button) findViewById(R.id.btn_1);
    button.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        UpdateManager.checkUpdate(MainActivity.this);
      }
    });
  }
}
