package hkhc.iperfwrapper;

import android.content.Context;

import java.io.File;

/**
 * Created by herman on 6/3/2017.
 */

public class AndroidDefaults implements DefaultConfig {

    private Context context;

    public AndroidDefaults(Context context) {
        this.context = context;
    }

    public void defaults(Iperf3 perf3) {

        File cacheDir = context.getCacheDir();
        File tempFile = new File(cacheDir, "iperf3tempXXXXXX");

        perf3.tempFileTemplate(tempFile.getAbsolutePath());

    }

}
