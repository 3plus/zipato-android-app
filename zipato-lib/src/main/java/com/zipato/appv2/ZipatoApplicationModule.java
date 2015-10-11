/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView.RecycledViewPool;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.Builder;
import com.zipato.appv2.R.string;
import com.zipato.appv2.activities.AlarmTriggerActivity;
import com.zipato.appv2.activities.BrowserManagerActivity;
import com.zipato.appv2.activities.CameraActivity;
import com.zipato.appv2.activities.DeviceManagerActivity;
import com.zipato.appv2.activities.DiscoveryActivity;
import com.zipato.appv2.activities.LogInActivity;
import com.zipato.appv2.activities.MjpegStreamActivity;
import com.zipato.appv2.activities.PasswordRecoveryActivity;
import com.zipato.appv2.activities.RegisterActivity;
import com.zipato.appv2.activities.ScreenShotActivity;
import com.zipato.appv2.activities.ShakeSettingActivity;
import com.zipato.appv2.activities.ShowDialogActivity;
import com.zipato.appv2.activities.ShowVCMenu;
import com.zipato.appv2.activities.WizardActivity;
import com.zipato.appv2.broadcasts.ConnectionChangeReceiver;
import com.zipato.appv2.interactor.BrowserManagerInteractor;
import com.zipato.appv2.interactor.LoginIteractor;
import com.zipato.appv2.services.AutoUpdaterService;
import com.zipato.appv2.ui.fragments.adapters.bm.RoomAdapter;
import com.zipato.appv2.ui.fragments.adapters.bm.ScenesAdapter;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapterImp;
import com.zipato.appv2.ui.fragments.adapters.controllers.TypeViewControllerFactory;
import com.zipato.appv2.ui.fragments.bm.MainRoomFragment;
import com.zipato.appv2.ui.fragments.bm.MainSceneFragment;
import com.zipato.appv2.ui.fragments.bm.RoomFragment;
import com.zipato.appv2.ui.fragments.bm.SceneFragment;
import com.zipato.appv2.ui.fragments.bm.TypesRoomFragment;
import com.zipato.appv2.ui.fragments.bm.TypesScenesFragment;
import com.zipato.appv2.ui.fragments.bm.UiTypeFragment;
import com.zipato.appv2.ui.fragments.cameras.ArchiveFileProvider;
import com.zipato.appv2.ui.fragments.cameras.ArchiveFragment;
import com.zipato.appv2.ui.fragments.cameras.FragmentScreenShot;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCBlindRoller;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCCamera;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCDefault;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCEnumButtons;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCITach;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCLevel;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCMediaPlayer;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOnOff;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamRGB;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamRGBW;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCOsRamTemp;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCPhilipsHue;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCRemotec;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCScenes;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCSecurity;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCThermostat;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCWeather;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.VCZipaRGBW;
import com.zipato.appv2.ui.fragments.discovery.BaseDiscoveryFragment;
import com.zipato.appv2.ui.fragments.discovery.JDeviceFragment;
import com.zipato.appv2.ui.fragments.discovery.JIPCamFragment;
import com.zipato.appv2.ui.fragments.discovery.RDeviceFragment;
import com.zipato.appv2.ui.fragments.discovery.SDeviceFragment;
import com.zipato.appv2.ui.fragments.dm.AttributesFragment;
import com.zipato.appv2.ui.fragments.dm.ClusterEndpointFragment;
import com.zipato.appv2.ui.fragments.dm.DMCMConfigFragment;
import com.zipato.appv2.ui.fragments.dm.DMConfigFragment;
import com.zipato.appv2.ui.fragments.dm.DMIconConfigColorFragment;
import com.zipato.appv2.ui.fragments.dm.DeviceFragment;
import com.zipato.appv2.ui.fragments.dm.EndpointFragment;
import com.zipato.appv2.ui.fragments.dm.NetworkFragment;
import com.zipato.appv2.ui.fragments.security.SecurityEventFragment;
import com.zipato.appv2.ui.fragments.security.ZonesFragment;
import com.zipato.appv2.ui.fragments.settings.BoxInfoFragment;
import com.zipato.appv2.ui.fragments.settings.SettingMenuFragment;
import com.zipato.appv2.ui.fragments.settings.SubSettingsFragment;
import com.zipato.appv2.ui.fragments.vcmenu.ConfigFragment;
import com.zipato.appv2.ui.fragments.vcmenu.EventFragment;
import com.zipato.appv2.ui.fragments.vcmenu.IconConfigColorFragment;
import com.zipato.appv2.ui.fragments.vcmenu.ScenesIconColorFragment;
import com.zipato.discovery.DiscoveryManager;
import com.zipato.discovery.ServiceInfoAdapter;
import com.zipato.helper.AssetLoaderHelper;
import com.zipato.helper.AttributesHelper;
import com.zipato.helper.DeviceStateHelper;
import com.zipato.helper.InternetConnectionHelper;
import com.zipato.helper.PreferenceHelper;
import com.zipato.helper.PreferenceHelper.Preference;
import com.zipato.model.RepositoryFactory;
import com.zipato.model.RepositoryFactoryImpl;
import com.zipato.model.alarm.PartitionRepository;
import com.zipato.model.alarm.ZonesRepository;
import com.zipato.model.attribute.AttributeRepository;
import com.zipato.model.attribute.AttributeValueRepository;
import com.zipato.model.brand.BrandRepository;
import com.zipato.model.camera.CameraRepository;
import com.zipato.model.camera.SVFileRest;
import com.zipato.model.device.DeviceRepository;
import com.zipato.model.device.DeviceStateRepository;
import com.zipato.model.endpoint.ClusterEndpointRepository;
import com.zipato.model.endpoint.EndpointRepository;
import com.zipato.model.network.NetworkRepository;
import com.zipato.model.room.RoomRepository;
import com.zipato.model.room.Rooms;
import com.zipato.model.scene.Scene;
import com.zipato.model.scene.SceneRepository;
import com.zipato.model.thermostat.ThermostatRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.model.typereport.TypeReportKey;
import com.zipato.model.typereport.TypeReportRepository;
import com.zipato.model.typereport.UiType;
import com.zipato.translation.LanguageManager;
import com.zipato.util.CookieOkHttpDownloader;
import com.zipato.util.ShakeUtils;
import com.zipato.util.TypeFaceUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

/**
 * Created by dbudor on 17/06/2014.
 */
@Module(injects = {LogInActivity.class, BrowserManagerActivity.class,
        DeviceManagerActivity.class, RegisterActivity.class, PasswordRecoveryActivity.class, DiscoveryManager.class, LanguageManager.class, //
        PreferenceHelper.class, AutoUpdaterService.class, AttributesFragment.class,//
        NetworkFragment.class, DeviceFragment.class, EndpointFragment.class, ClusterEndpointFragment.class,
        ConfigFragment.class, EventFragment.class, //
        SettingMenuFragment.class, UiTypeFragment.class,
        DiscoveryActivity.class, SDeviceFragment.class, BaseDiscoveryFragment.class, JDeviceFragment.class,
        RDeviceFragment.class, ConnectionChangeReceiver.class,
        CameraActivity.class, CameraActivity.class, JIPCamFragment.class, DMConfigFragment.class,
        ShakeSettingActivity.class, IconConfigColorFragment.class, DMCMConfigFragment.class, DMIconConfigColorFragment.class, ArchiveFragment.class,
        FragmentScreenShot.class, ScreenShotActivity.class, MjpegStreamActivity.class, BoxInfoFragment.class, SubSettingsFragment.class,
        VCPhilipsHue.class, VCZipaRGBW.class, ServiceInfoAdapter.class
        , VCOsRamRGBW.class, WizardActivity.class, TypesRoomFragment.class, VCBlindRoller.class,
        VCThermostat.class, RoomFragment.class, VCScenes.class, RoomAdapter.class, ShowDialogActivity.class, ScenesIconColorFragment.class,
        VCSecurity.class, VCDefault.class, VCCamera.class, VCRemotec.class, VCITach.class, VCEnumButtons.class, VCMediaPlayer.class, VCWeather.class,
        VCLevel.class, VCOnOff.class, TypeViewControllerFactory.class, GenericAdapterImp.class, TypesScenesFragment.class
        , AlarmTriggerActivity.class, ShowVCMenu.class, SecurityEventFragment.class, ZonesFragment.class, VCOsRamTemp.class, VCOsRamRGB.class,
        LoginIteractor.class, BrowserManagerInteractor.class, MainRoomFragment.class, MainSceneFragment.class, ScenesAdapter.class, SceneFragment.class},
        // includes = { DmFragmentsModule.class, SceneFragmentModule.class, SettingsFragmentsModule.class},
        library = false, complete = true)

public class ZipatoApplicationModule {

    private static final int NUM_THREADS = 5;

    private final Context context;

    public ZipatoApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutor() {
        return Executors.newFixedThreadPool(NUM_THREADS);
    }

    @Provides
    @Singleton
    public ApiV2RestTemplate provideRestTemplate(PreferenceHelper preferenceHelper) {
        ApiV2RestTemplate rt = new ApiV2RestTemplate();
        SharedPreferences sharedPreferences = context.getSharedPreferences(PreferenceHelper.PREF_NAME, Context.MODE_PRIVATE);
        String baseUrl = context.getResources().getString(string.base_url);
        String serverUrl = sharedPreferences.getString(Preference.SERVER_URL.toString(), baseUrl);
        rt.setGcmToken(preferenceHelper.getStringPref(Preference.PROPERTY_REG_ID, ""));
        rt.setGcmRegistered(preferenceHelper.getBooleanPref(Preference.GCM_REGISTERED));
        rt.setRemoteUrl(serverUrl);
        rt.setUsername(preferenceHelper.getStringPref(Preference.USERNAME, ""));
        rt.setPassword(preferenceHelper.getStringPref(Preference.PASSWORD, ""));
        rt.setSerial(preferenceHelper.getStringPref(Preference.BOX_SERIAL, null));
        rt.setLocal(preferenceHelper.getStringPref(Preference.LANGUAGE, context.getResources().getConfiguration().locale.getLanguage()));
        return rt;
    }

    @Provides
    @Singleton
    public EventBus provideEventBus() {
        return new EventBus();
    }

    @Provides
    @Singleton
    public RepositoryFactory provideRepositoryFactory(ApiV2RestTemplate restTemplate, EventBus eventBus) {
        RepositoryFactoryImpl factory = new RepositoryFactoryImpl();
        factory.setRestTemplate(restTemplate);
        factory.setEventBus(eventBus);
        factory.setContext(context);
        return factory;
    }

    @Provides
    @Singleton
    public NetworkRepository provideNetworkRepository(RepositoryFactory factory) {
        return factory.getRepository(NetworkRepository.class);
    }

    @Provides
    @Singleton
    public DeviceRepository provideDeviceRepository(RepositoryFactory factory) {
        return factory.getRepository(DeviceRepository.class);
    }

    @Provides
    @Singleton
    public DeviceStateRepository provideDeviceStateRepository(RepositoryFactory factory) {
        return factory.getRepository(DeviceStateRepository.class);
    }

    @Provides
    @Singleton
    public EndpointRepository provideEndpointRepository(RepositoryFactory factory) {
        return factory.getRepository(EndpointRepository.class);
    }

    @Provides
    @Singleton
    public ClusterEndpointRepository provideClusterEndpointRepository(RepositoryFactory factory) {
        return factory.getRepository(ClusterEndpointRepository.class);
    }

    @Provides
    @Singleton
    public AttributeRepository provideAttributeRepository(RepositoryFactory factory) {
        return factory.getRepository(AttributeRepository.class);
    }

    @Provides
    @Singleton
    public AttributeValueRepository provideAttributeValueRepository(RepositoryFactory factory) {
        return factory.getRepository(AttributeValueRepository.class);
    }

    @Provides
    @Singleton
    public TypeReportRepository provideTypeReportRepository(RepositoryFactory factory) {
        return factory.getRepository(TypeReportRepository.class);
    }

    @Provides
    @Singleton
    public SceneRepository provideSceneRepository(RepositoryFactory factory) {
        return factory.getRepository(SceneRepository.class);
    }

    @Provides
    @Singleton
    public ThermostatRepository provideThermostatRepository(RepositoryFactory factory) {
        return factory.getRepository(ThermostatRepository.class);
    }

    @Provides
    @Singleton
    public BrandRepository provideBrandRepository(RepositoryFactory factory) {
        return factory.getRepository(BrandRepository.class);
    }

    @Provides
    @Singleton
    public PartitionRepository providePartitionRepository(RepositoryFactory factory) {
        return factory.getRepository(PartitionRepository.class);
    }

    @Provides
    @Singleton
    public ZonesRepository provideZoneRepository(RepositoryFactory factory) {
        return factory.getRepository(ZonesRepository.class);
    }

    @Provides
    @Singleton
    public RoomRepository provideRoomsRepository(RepositoryFactory factory) {
        return factory.getRepository(RoomRepository.class);
    }

    @Provides
    @Singleton
    public CameraRepository provideCamerasRepository(RepositoryFactory factory) {
        return factory.getRepository(CameraRepository.class);
    }


    @Provides
    @Singleton
    public Picasso porividePicasso(ApiV2RestTemplate restTemplate, Cache lruCache) {
        final Downloader downloader = new CookieOkHttpDownloader(context, restTemplate.getCookieStore());
        final ExecutorService executorService = Executors.newFixedThreadPool(3);
        return new Builder(context).downloader(downloader).executor(executorService).memoryCache(lruCache).build();
    }

    @Provides
    @Singleton
    public TypeFaceUtils provideTypeFacceUtils() {
        return new TypeFaceUtils(context);
    }

    @Provides
    @Singleton
    public Cache provideCache() {
        return new LruCache(context);
    }

    @Provides
    @Singleton
    public AssetLoaderHelper provideAssetLoader(Picasso picasso) {
        return new AssetLoaderHelper(context, picasso);
    }

    @Provides
    @Singleton
    public Map<UiType, List<TypeReportItem>> provideTypeMap() {
        return new ConcurrentHashMap<>();
    }

    @Provides
    @Singleton
    public List<UiType> provideUiTypes() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    public List<Rooms> provideRooms() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    @Named("rooms")
    public List<TypeReportItem> provideListTypesForRooms() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    @Named("scenes")
    public List<TypeReportItem> provideListTypesForScenes() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    public List<Scene> provideListScenes() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    public ArchiveFileProvider providerArchiveFileProvider() {
        return new ArchiveFileProvider();
    }

    @Provides
    @Singleton
    public List<SVFileRest> provideFiles() {
        return new ArrayList<>();
    }

    @Provides
    @Singleton
    public DeviceStateHelper provideDeviceStateHelper(AttributeRepository attributeRepository, DeviceRepository deviceRepository,
                                                      EndpointRepository endpointRepository, ClusterEndpointRepository clusterEndpointRepository,
                                                      DeviceStateRepository deviceStateRepository) {
        return new DeviceStateHelper(attributeRepository, deviceRepository, endpointRepository, clusterEndpointRepository, deviceStateRepository);
    }

    @Provides
    @Singleton
    public AttributesHelper provideAttributesHelper(AttributeRepository attributeRepository, AttributeValueRepository attributeValueRepository,
                                                    LanguageManager languageManager) {
        return new AttributesHelper(attributeRepository, attributeValueRepository, languageManager);
    }

    @Provides
    @Singleton
    public Map<TypeReportKey, ArrayMap<String, Object>> proveGlobalTypeReportCache() {
        return new ConcurrentHashMap<>();
    }

    @Provides
    @Singleton
    public InternetConnectionHelper provideInternetConnectionHelper() {
        return new InternetConnectionHelper(context);
    }

    @Provides
    @Singleton
    public ShakeUtils provideShakeUtils() {
        return new ShakeUtils(context);
    }

    @Provides
    @Singleton
    public LanguageManager provideLanguageManager() {
        return new LanguageManager(context);
    }

    @Provides
    @Singleton
    public PreferenceHelper providePreferenceHelper(Lazy<LanguageManager> languageManagerLazy, Lazy<ApiV2RestTemplate> restTemplateLazy) {
        return new PreferenceHelper(context, languageManagerLazy, restTemplateLazy);
    }

    @Named("typeReportPool")
    @Provides
    @Singleton
    public RecycledViewPool provideRecycledViewPool() {
        return new RecycledViewPool();
    }

}