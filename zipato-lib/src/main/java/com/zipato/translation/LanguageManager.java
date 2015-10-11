/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.translation;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipato.appv2.R;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.language.Language;

import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by murielK on 17.6.2014..
 */

public class LanguageManager {

    private static final String LANGUAGE_LIST_FILENAME = "languages_v2.json";
    private static final String LANGUAGE_LIST_URL = "http://translate.zipato.com/media/languages_v2.json";
    private static final String LANGUAGE_URL = "http://translate.zipato.com/media/export/android/package_{code}.json";
    private static final String LANGUAGE_PREFIX = "package_";
    private static final String LANGUAGE_BASE = "package";

    private final LanguageControl control;
    private final Context context;
    private Map<String, Language> languages;
    private RestTemplate restTemplate;
    private ResourceBundle translations;
    private Locale locale;
    private Language language;

    public LanguageManager(Context context) {
        this.context = context;

        control = new LanguageControl(new ObjectMapper(), context);

        String localeTemp;

        if (context.getResources().getBoolean(R.bool.language_force_default_en))
            localeTemp = "en";
        else
            localeTemp = ((context.getResources().getConfiguration().locale.getLanguage() != null) && context.getResources().getConfiguration().locale.getLanguage().isEmpty()) ? "en" : context.getResources().getConfiguration().locale.getLanguage();

        final SharedPreferences sharedPreferences = context.getSharedPreferences(PreferenceHelper.PREF_NAME, Context.MODE_PRIVATE);
        final String localeFinal = sharedPreferences.getString(Preference.LANGUAGE.name(), localeTemp);

        setLanguageCode(localeFinal);
    }

    private void initLanguages() {

        if (languages == null) {
            languages = new LinkedHashMap<>();
        } else {
            languages.clear();
        }

        final ObjectMapper objectMapper = new ObjectMapper();


        Language[] list = null;
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(LANGUAGE_LIST_FILENAME);
            Log.d("LanguageManager", "downloaded languages found loading new list Languages");//
            list = objectMapper.readValue(fis, Language[].class);
        } catch (Exception e) {
            Log.d("LanguageManager", "fail loading from downloads files", e);//
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    //Empty
                }
        }

        if ((list == null) || (list.length == 0)) {

            InputStream is = null;

            try {
                is = context.getAssets().open(LANGUAGE_LIST_FILENAME);
                Log.d("LanguageManager", "loading languages from locale assets");
                list = objectMapper.readValue(is, Language[].class);
            } catch (Exception e) {
                Log.d("LanguageManager", "fail loading from locale asset files", e);//
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }

        if ((list == null) || (list.length == 0)) {
            return;
        }

        for (Language lang : list) {
            languages.put(lang.getCode(), lang);
        }
    }

    public Map<String, Language> getLanguages() {
        if (languages == null) {
            initLanguages();
        }
        return languages;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (language == null) {
            language = getLanguages().get("en");
        }

        this.language = language;
        Locale newLocale = new Locale(language.getCode());
        if (!newLocale.equals(locale)) {
            translations = ResourceBundle.getBundle(LANGUAGE_BASE, newLocale, control);
        }
        locale = newLocale;
    }

    public String translate(String key) {
        try {
            return translations.getString(key);
        } catch (Exception e) {
            Log.d("LanguageManager", e.getMessage());
        }
        return key;
    }

    public void translateFields(Object obj) {
        TranslateField.translate(this, obj);
    }

    public void clean() {
//        File langJson = new File(i18nDir, LANGUAGE_LIST_FILENAME);
//        langJson.delete();
//        String[] children = translationDir.list();
//        for (int i = 0; i < children.length; i++) {
//            new File(translationDir, children[i]).delete();
//        }
    }

    public void update() {
        Log.d("LanguageManager", "updating languages");
        Language[] onServer = downloadLanguageList();
        List<Language> downloadList = prepareDownloadList(onServer);
        if (downloadList.isEmpty()) {
            Log.d("LanguageManager", "empty download list returning...");
            return;
        }
        for (Language lingo : downloadList) {
            Log.d("LanguageManager", "downloading: " + lingo.getLanguage());
            try {
                downloadLanguage(lingo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writeLanguageList(downloadList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDownload() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

    }

    private Language[] downloadLanguageList() {
        if (restTemplate == null)
            initDownload();

        return restTemplate.getForObject(LANGUAGE_LIST_URL, Language[].class);
    }

    private List<Language> prepareDownloadList(Language[] onServer) {
        List<Language> list = new ArrayList<Language>(onServer.length);
        for (Language lingo : onServer) {
            Language here = languages.get(lingo.getCode());
            if ((here == null) || (here.getModified().getTime() < lingo.getModified().getTime())) {
                list.add(lingo);
            }
        }
        return list;
    }

    private void downloadLanguage(Language language) throws IOException {
        if (restTemplate == null) {
            initDownload();
        }

        FileOutputStream fos = null;
        try {
            byte[] bytes = restTemplate.getForObject(LANGUAGE_URL, byte[].class, language.getCode());
            fos = context.openFileOutput(LANGUAGE_PREFIX + language.getCode() + ".json", Context.MODE_PRIVATE);
            fos.write(bytes);
        } catch (Exception e) {
            //
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    private void writeLanguageList(List<Language> downloadList) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(LANGUAGE_LIST_FILENAME, Context.MODE_PRIVATE);
            new ObjectMapper().writeValue(fos, downloadList);
        } catch (Exception e) {
            //
        } finally {
            if (fos != null)
                fos.close();
        }
    }

    public void setLanguageCode(String code) {
        setLanguage(getLanguages().get(code));
    }
}
