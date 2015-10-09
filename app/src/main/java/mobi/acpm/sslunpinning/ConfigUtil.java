package mobi.acpm.sslunpinning;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

/**
 * Created by acpm on 07/10/15.
 */
public class ConfigUtil {

    public static void writeToFile(String data) {

        try {

            File conf = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/XSSLUnpinning/app.conf");

            if(conf.exists() == false) {
                File path = new File(String.valueOf(conf.getParentFile()));
                if (path.mkdirs()) {
                    conf.createNewFile();
                }
            }
            FileOutputStream fOut = new FileOutputStream(conf,false);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(data);
            myOutWriter.close();
            fOut.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile() {

        String packageName = "";
        try {
            File conf = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/XSSLUnpinning/app.conf");
            if (conf.exists() == false) {
                conf.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(conf));
            String line;
            while ((line = br.readLine()) != null) {
                packageName = line;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packageName;
    }
}
