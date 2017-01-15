package luoja.prompt;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telecom.Call;
import android.widget.TextView;

import com.survivingwithandroid.weather.lib.WeatherClient;
import com.survivingwithandroid.weather.lib.WeatherConfig;
import com.survivingwithandroid.weather.lib.exception.WeatherLibException;
import com.survivingwithandroid.weather.lib.exception.WeatherProviderInstantiationException;
import com.survivingwithandroid.weather.lib.model.CurrentWeather;
import com.survivingwithandroid.weather.lib.model.Weather;
import com.survivingwithandroid.weather.lib.provider.openweathermap.OpenweathermapProviderType;
import com.survivingwithandroid.weather.lib.request.WeatherRequest;

import java.util.Date;

/**
 * Created by Jane on 26/02/16.
 */
public class Greeter {

    private Launcher context;

    private int outId;

    public Greeter(Launcher inContext, TextView outView){
        outId = outView.getId();
        context = inContext;
    }

    public void greet(){
        clear();
        hello();
        showMissedCalls();
        showNewSms();
        printWeather();
    }

    private void clear(){
        ((TextView) context.findViewById(outId)).setText("");
    }

    private void hello(){
        String name = preferences().getString(Val.KEY_NAME, context.getString(R.string.default_username));
        print(context.getString(R.string.phrase_hello, name));
    }

    private void printWeather(){
        WeatherConfig config = new WeatherConfig();
        WeatherConfig.UNIT_SYSTEM units;
        if (preferences().getInt(Val.KEY_MEASUREMENT_SYSTEM, 0) > 0) units = WeatherConfig.UNIT_SYSTEM.I;
        else units = WeatherConfig.UNIT_SYSTEM.M;
        config.unitSystem = units;
        config.numDays = 1;
        config.maxResult = 1;
        config.ApiKey = context.getString(R.string.OPW_API_KEY);
        try {
            WeatherClient client = new WeatherClient.ClientBuilder().attach(context)
                    .httpClient(com.survivingwithandroid.weather.lib.client.volley.WeatherClientDefault.class)
                    .provider(new OpenweathermapProviderType())
                    .config(config)
                    .build();
            Location location = ((LocationManager)context.getSystemService(Context.LOCATION_SERVICE))
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) return;
            WeatherRequest request = new WeatherRequest(location.getLongitude(), location.getLatitude());
            client.getCurrentCondition(request, new WeatherClient.WeatherEventListener() {
                @Override
                public void onWeatherRetrieved(CurrentWeather currentWeather) {
                    print(context.getString(R.string.phrase_weather_intro));
                    Weather weather = currentWeather.weather;
                    Weather.Temperature temperature = weather.temperature;
                    String out = context.getString(R.string.phrase_temperature, temperature.getTemp(), "\u00B0");
                    print(out);
                    Weather.Wind wind = weather.wind;
                    out = context.getString(R.string.phrase_wind_speed, wind.getSpeed(), getSpeedUnits());
                    print(out);
                    Weather.Rain rain = weather.rain[0];
                    out = context.getString(R.string.phrase_rain, rain.getChance(), "\uFE6A");
                    print(out);
                }
                //Just do nothing for these, it's not an essential function;
                @Override public void onWeatherError(WeatherLibException wle) {return;}
                @Override public void onConnectionError(Throwable t) {return;}
            });
        } catch (WeatherProviderInstantiationException wpie){
        } catch (SecurityException se) {}
    }

    public String getSpeedUnits() {
        if (preferences().getInt(Val.KEY_MEASUREMENT_SYSTEM, 0) > 0)
            return context.getString(R.string.imp_speed);
        else return context.getString(R.string.si_speed);
    }

    private static final int LIMIT = 20;

    private void showMissedCalls(){
        try {
            String[] projection = {CallLog.Calls.TYPE, CallLog.Calls.NEW};
            String order = CallLog.Calls.DATE + " DESC";
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://call_log/calls"),
                    projection, null, null, order);
            int i = 0;
            if (cursor.moveToFirst()) {
                int count = 0;
                while (!cursor.isAfterLast() && count < LIMIT) {
                    String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    String isNew = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NEW));
                    if (Integer.parseInt(type) == CallLog.Calls.MISSED_TYPE && Integer.parseInt(isNew) > 0)
                        i++;
                    cursor.moveToNext();
                    count++;
                }
                cursor.close();
            }
            if (i > 0) print(context.getString(R.string.phrase_missed_calls, i));
        } catch (SecurityException se) {}
    }

    private void showNewSms(){
        try {
            int newSms = 0;
            String order = Telephony.Sms.DATE + " DESC";
            Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox")
                    , null, null, null, order);
            if (cursor.moveToFirst()) {
                int count = 0;
                while (!cursor.isAfterLast() && count < LIMIT) {
                    String isNew = cursor.getString(cursor.getColumnIndex(Telephony.Sms.SEEN));
                    if (Integer.parseInt(isNew) == 0) ++newSms;
                    cursor.moveToNext();
                    count++;
                }
            }
            cursor.close();
            if (newSms > 0) print(context.getString(R.string.phrase_new_sms, newSms));
        } catch (SecurityException se) {}
    }

    private SharedPreferences preferences(){
        return context.getSharedPreferences(Val.PREFERENCES_NAME, 0);
    }

    private void print(String out){
        context.print(out);
    }

    protected boolean premium(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(Val.PREFERENCES_NAME, 0);
        if (!sharedPreferences.getBoolean(Val.KEY_PREMIUM, false)) {
            long now = new Date().getTime();
            long endTrial = sharedPreferences.getLong(Val.KEY_TRIAL_END, now);
            if (now > endTrial) return false;
        }
        return true;
    }
}
