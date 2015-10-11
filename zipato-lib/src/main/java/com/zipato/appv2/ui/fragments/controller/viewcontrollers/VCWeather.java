/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.B;import com.zipato.appv2.R;
import com.zipato.appv2.R.drawable;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.inject.Inject;

import butterfork.Bind;

/**
 * Created by murielK on 8/26/2015.
 */
@ViewType("view_controller_weather_station")
public class VCWeather extends ViewController {

    private static final String TAG = TagFactoryUtils.getTag(VCWeather.class);

    private static final ArrayMap<String, Integer> cacheResID = new ArrayMap<>();

    private final DateFormat time;
    private final DateFormat date;
    private final DateFormat formatter;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewCity)
    TextView city;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewCountry)
    TextView country;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewTime)
    TextView timeTextView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewDate)
    TextView dateTexTView;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewHumi)
    TextView humidity;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewRain)
    TextView precipitation;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewWind1)
    TextView wind1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewWind2)
    TextView wind2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewUV)
    TextView textUv;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSunRise)
    TextView sunRise1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSunRise2)
    TextView sunRise2;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSunSet1)
    TextView sunSet1;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewSunSet2)
    TextView sunSet2;
    @Bind(B.id.imageViewMeteoMini)
    ImageView imgMeteoMini;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewMetoMini)
    TextView meteoMini;
    @Bind(B.id.imageViewMetoe)
    ImageView meteo;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.textViewTemperature)
    TextView currentTemperature;

    @Inject
    Picasso picasso;

    public VCWeather(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        final Context context = getContext();
        Locale locale = context.getResources().getConfiguration().locale;
        //time = new SimpleDateFormat("HH:mm:ss", locale);
        time = new SimpleDateFormat("MMM dd, HH:mm:ss", locale);
        date = new SimpleDateFormat("MMMM dd", locale);
        //"Wed Nov 26 15:07:08 CET 2014",
        formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    @Override
    public boolean hasLogic() {
        return false;
    }

    @Override
    public void dispatchOnBind(Object object) {
        try {
            final TypeReportItem item = getTypeReportItem();
            final String location = item.getLocation();
            final String[] locations = location.split(",");
            try {
                city.setText(locations[0]);
            } catch (Exception e) {
                city.setText("-");
            }
            try {
                country.setText(locations[1].substring(1));
            } catch (Exception e) {
                country.setText("-");
            }
        } catch (Exception e) {
            city.setText("-");
            country.setText("-");
        }

        setValueTextView(BaseTypesFragment.HUMIDITY, humidity, "%");
        setValueTextView(BaseTypesFragment.PRECIPITATION_PPM, precipitation, "mm");
        setValueTextView(BaseTypesFragment.WIND_SPEED, wind1, "Kmh");
        setValueTextView(BaseTypesFragment.WIND_DIRECTION, wind2, "");
        setSunRiseSunSet(BaseTypesFragment.SUNRISE, sunRise1, sunRise2);
        setSunRiseSunSet(BaseTypesFragment.SUNSET, sunSet1, sunSet2);
        setValueTextView(BaseTypesFragment.TEMPERATURE_WEATHER, currentTemperature, BaseTypesFragment.DEGREE);
        setValueTextView(BaseTypesFragment.WEATHER_DESC, meteoMini, "");
        setValueTextView(BaseTypesFragment.WEATHER_UV, textUv, "");
        setTimeDate(timeTextView, dateTexTView);
        setImageView(meteo, false);
        setImageView(imgMeteoMini, true);
    }

    private UUID getUuidFor(final int attrId) {
        final TypeReportItem item = getTypeReportItem();
        if (item == null)
            return null;
        return (item.getAttrOfID(attrId) == null) ? null : item.getAttrOfID(attrId).getUuid();
    }

    private void setSunRiseSunSet(int attr, TextView textViewH, TextView textViewM) {
        try {
            final UUID uuid = getUuidFor(attr);
            final String value = attrValueUnitResolver(uuid);
            final String[] localTime = value.split(":");
            textViewH.setText(localTime[0] + 'h');
            textViewM.setText(localTime[1] + 'm');
        } catch (Exception e) {
            textViewH.setText("-");
            textViewM.setText("-");
            Log.d(TAG, "", e);
        }
    }

    private void setValueTextView(int attr, TextView textView, String suffix) {
        try {
            final UUID uuid = getUuidFor(attr);
            final String value = getValueForAttr(uuid);
            suffix = (suffix == null) ? "" : suffix;
            textView.setText((value == null) ? "" : (value + suffix));
        } catch (Exception e) {
            textView.setText("-");
            Log.d(TAG, "", e);
        }
    }

    private void setTimeDate(TextView textViewTime, TextView textViewDate) {
        try {
            final UUID uuid = getUuidFor(BaseTypesFragment.OBS_TIME);
            final String lastUpdate = languageManager.translate("last_update");
            textViewTime.setText(lastUpdate);
            String value = attrValueUnitResolver(uuid);
            Date finaleDate = formatter.parse(value);
            textViewDate.setText(time.format(finaleDate));
        } catch (Exception e) {
            textViewTime.setText("-");
            textViewDate.setText("-");
            Log.d(TAG, "", e);
        }

    }

    private void setImageView(ImageView img, boolean isMinni) {
        final Context context = getContext();
        try {
            UUID uuid = getUuidFor(BaseTypesFragment.WEATHER_ICON);
            String value = attrValueUnitResolver(uuid);
            if (isMinni)
                value += "_mini";
            int resID;
            if (cacheResID.containsKey(value))
                resID = cacheResID.get(value);
            else {
                resID = context.getResources().getIdentifier(value, "drawable", context.getPackageName());
                cacheResID.put(value, resID);
            }
            picasso.load(resID).fit().error(drawable.drawable_transparent).into(img);
        } catch (Exception e) {
            //Empty
        }
    }
}
