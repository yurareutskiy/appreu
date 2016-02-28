package styleru.it_lab.reaschedule.Operations;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class NetworkOperations {

    public static final String DEBUG_TAG = "NETWORKOPERATIONS_DEBUG";

    public static boolean isConnectionAvailable(Context c)
    {
        //Проверяет, есть ли вообще сеть и включен ли интернет
        ConnectivityManager connMgr = (ConnectivityManager)
                c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static class RequestTask extends AsyncTask<String, String, String> {

        public interface AsyncResponse {
            void processFinish(Object result, String response);
        }

        AsyncResponse delegate = null;
        String whatIsThat = "";

        public RequestTask(AsyncResponse _delegate, String _whatIsThat) {
            delegate = _delegate;
            whatIsThat = _whatIsThat;
        }

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                Log.i(DEBUG_TAG, "Executing query with URI: " + uri[0]);
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    Log.i(DEBUG_TAG, "Status OK!");
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    Log.i(DEBUG_TAG, "Status isn't OK!");
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                cancel(true);
            } catch (IOException e) {
                cancel(true);
            }

            if (responseString == null)
            {
                Log.i(DEBUG_TAG, "Response is NULL!");
                cancel(true);
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Object objResult = null;
            String errorText = "";

            if (whatIsThat.equals("members"))
            {
                objResult = OtherOperations.parseMembers(result);
            }
            else if (whatIsThat.equals("schedule"))
            {
                objResult = OtherOperations.parseSchedule(result);
            }

            delegate.processFinish(objResult, result);
        }

        @Override
        protected void onCancelled() {
            Log.i(DEBUG_TAG, "Query cancelled.");
            delegate.processFinish(null, "");
        }
    }

}
