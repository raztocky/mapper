package app.organicmaps;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static app.organicmaps.leftbutton.LeftButtonsHolder.BUTTON_ADD_PLACE_CODE;
import static app.organicmaps.leftbutton.LeftButtonsHolder.BUTTON_HELP_CODE;
import static app.organicmaps.leftbutton.LeftButtonsHolder.BUTTON_SETTINGS_CODE;
import static app.organicmaps.sdk.location.LocationState.FOLLOW;
import static app.organicmaps.sdk.location.LocationState.FOLLOW_AND_ROTATE;
import static app.organicmaps.sdk.location.LocationState.LOCATION_TAG;
import static app.organicmaps.sdk.util.Utils.dimen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import android.view.Gravity;
import android.view.MenuItem;
import androidx.appcompat.widget.PopupMenu;
import app.organicmaps.sdk.bookmarks.data.BookmarkInfo;
import app.organicmaps.sdk.bookmarks.data.FeatureId;
import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import app.organicmaps.api.Const;
import app.organicmaps.backup.PeriodicBackupRunner;
import app.organicmaps.base.BaseMwmFragmentActivity;
import app.organicmaps.base.OnBackPressListener;
import app.organicmaps.bookmarks.BookmarkCategoriesActivity;
import app.organicmaps.downloader.DownloaderActivity;
import app.organicmaps.downloader.DownloaderFragment;
import app.organicmaps.downloader.OnmapDownloader;
import app.organicmaps.editor.EditorActivity;
import app.organicmaps.editor.EditorHostFragment;
import app.organicmaps.editor.FeatureCategoryActivity;
import app.organicmaps.help.HelpActivity;
import app.organicmaps.intent.Factory;
import app.organicmaps.intent.IntentProcessor;
import app.organicmaps.leftbutton.LeftButton;
import app.organicmaps.leftbutton.LeftButtonsHolder;
import app.organicmaps.maplayer.MapButtonsController;
import app.organicmaps.maplayer.MapButtonsViewModel;
import app.organicmaps.maplayer.ToggleMapLayerFragment;
import app.organicmaps.routing.DirectionsPreviewBottomSheet;
import app.organicmaps.routing.ManageRouteBottomSheet;
import app.organicmaps.routing.NavigationController;
import app.organicmaps.routing.NavigationService;
import app.organicmaps.routing.RoutePreviewController;
import app.organicmaps.routing.RoutePreviewPanelController;
import app.organicmaps.routing.RoutingBottomMenuListener;
import app.organicmaps.routing.RoutingErrorDialogFragment;
import app.organicmaps.routing.RoutingPlanFragment;
import app.organicmaps.routing.RoutingPlanInplaceController;
import app.organicmaps.sdk.ChoosePositionMode;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.Map;
import app.organicmaps.sdk.MapController;
import app.organicmaps.sdk.MapRenderingListener;
import app.organicmaps.sdk.PlacePageActivationListener;
import app.organicmaps.sdk.Router;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.sdk.display.DisplayChangedListener;
import app.organicmaps.sdk.display.DisplayManager;
import app.organicmaps.sdk.display.DisplayType;
import app.organicmaps.sdk.downloader.MapManager;
import app.organicmaps.sdk.downloader.UpdateInfo;
import app.organicmaps.sdk.editor.Editor;
import app.organicmaps.sdk.location.LocationHelper;
import app.organicmaps.sdk.location.LocationListener;
import app.organicmaps.sdk.location.LocationState;
import app.organicmaps.sdk.location.SensorListener;
import app.organicmaps.sdk.maplayer.isolines.IsolinesState;
import app.organicmaps.sdk.routing.RouteMarkType;
import app.organicmaps.sdk.routing.RoutingController;
import app.organicmaps.sdk.routing.RoutingInfo;
import app.organicmaps.sdk.routing.RoutingOptions;
import app.organicmaps.sdk.search.SearchEngine;
import app.organicmaps.sdk.settings.RoadType;
import app.organicmaps.sdk.settings.UnitLocale;
import app.organicmaps.sdk.sound.TtsPlayer;
import app.organicmaps.sdk.util.Config;
import app.organicmaps.sdk.util.LocationUtils;
import app.organicmaps.sdk.util.log.Logger;
import app.organicmaps.sdk.widget.placepage.PlacePageData;
import app.organicmaps.search.SearchActivity;
import app.organicmaps.search.SearchFragment;
import app.organicmaps.settings.RoutingOptionsActivity;
import app.organicmaps.settings.SettingsActivity;
import app.organicmaps.util.SharingUtils;
import app.organicmaps.util.ThemeSwitcher;
import app.organicmaps.util.ThemeUtils;
import app.organicmaps.util.UiUtils;
import app.organicmaps.util.Utils;
import app.organicmaps.util.bottomsheet.MenuBottomSheetFragment;
import app.organicmaps.util.bottomsheet.MenuBottomSheetItem;
import app.organicmaps.widget.menu.MainMenu;
import app.organicmaps.widget.placepage.PlacePageController;
import app.organicmaps.widget.placepage.PlacePageViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Objects;

public class MwmActivity extends BaseMwmFragmentActivity
    implements PlacePageActivationListener, MapRenderingListener, RoutingController.Container, LocationListener,
               SensorListener, LocationState.ModeChangeListener, RoutingPlanInplaceController.RoutingPlanListener,
               RoutingBottomMenuListener, BookmarkManager.BookmarksLoadingListener,
               MenuBottomSheetFragment.MenuBottomSheetInterfaceWithHeader,
               PlacePageController.PlacePageRouteSettingsListener, MapButtonsController.MapButtonClickListener,
               DisplayChangedListener
{
  private static final String TAG = MwmActivity.class.getSimpleName();

  // The "Home" chip routes to the bookmark with this name.
  private static final String HOME_BOOKMARK_NAME = "Home";
  // The Home chip ETA is requested once per app open, as soon as a location fix and the bookmarks are available.
  private boolean mHomeEtaPending = false;
  private boolean mColdOpen = false;

  public static final String EXTRA_COUNTRY_ID = "country_id";
  public static final String EXTRA_CATEGORY_ID = "category_id";
  public static final String EXTRA_BOOKMARK_ID = "bookmark_id";
  public static final String EXTRA_TRACK_ID = "track_id";
  public static final String EXTRA_UPDATE_THEME = "update_theme";
  private static final String EXTRA_CONSUMED = "mwm.extra.intent.processed";
  private boolean mPreciseLocationDialogShown = false;

  private static final String[] DOCKED_FRAGMENTS = {SearchFragment.class.getName(), DownloaderFragment.class.getName(),
                                                    RoutingPlanFragment.class.getName(), EditorHostFragment.class.getName()};

  public final ActivityResultLauncher<Intent> startRoutingOptionsForResult =
      registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
        if (activityResult.getResultCode() == Activity.RESULT_OK)
          rebuildLastRoute();
      });

  private static final String MAIN_MENU_ID = "MAIN_MENU_BOTTOM_SHEET";
  private static final String LAYERS_MENU_ID = "LAYERS_MENU_BOTTOM_SHEET";


  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private MapController mMapController;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private RoutePreviewController mRoutePreviewController;

  private View mPointChooser;
  private MaterialToolbar mPointChooserToolbar;

  private RoutingPlanInplaceController mRoutingPlanInplaceController;
  @Nullable
  private RoutePreviewPanelController mRoutePreviewPanelController;

  private NavigationController mNavigationController;

  private MainMenu mMainMenu;

  private PanelAnimator mPanelAnimator;
  @Nullable
  private OnmapDownloader mOnmapDownloader;
  private boolean mIsTabletLayout;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull

  private boolean mRestoreRoutingPlanFragmentNeeded;
  @Nullable
  private Bundle mSavedForTabletState;

  private int mNavBarHeight;

  private LeftButtonsHolder buttonsHolder;

  private PlacePageViewModel mPlacePageViewModel;
  private MapButtonsViewModel mMapButtonsViewModel;
  private MapButtonsController.LayoutMode mPreviousMapLayoutMode;

  @Nullable
  private WindowInsetsCompat mCurrentWindowInsets;

  @Nullable
  private Dialog mLocationErrorDialog;

  @Nullable
  private Dialog mAlertDialog;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<String[]> mLocationPermissionRequest;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<String> mPostNotificationPermissionRequest;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<IntentSenderRequest> mLocationResolutionRequest;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<SharingUtils.SharingIntent> mShareLauncher;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private ActivityResultLauncher<Intent> mSettingsLauncher;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private DisplayManager mDisplayManager;

  private PeriodicBackupRunner backupRunner;

  private ManageRouteBottomSheet mManageRouteBottomSheet;

  private DirectionsPreviewBottomSheet mDirectionsPreviewBottomSheet;

  private static int mLastUiMode = Configuration.UI_MODE_TYPE_UNDEFINED;

  public interface LeftAnimationTrackListener
  {
    void onTrackStarted(boolean collapsed);

    void onTrackFinished(boolean collapsed);

    void onTrackLeftAnimation(float offset);
  }

  public static Intent createShowMapIntent(@NonNull Context context, @Nullable String countryId)
  {
    return new Intent(context, DownloadResourcesLegacyActivity.class).putExtra(EXTRA_COUNTRY_ID, countryId);
  }

  @Override
  public void onRenderingCreated()
  {
    checkMeasurementSystem();
  }

  @Override
  @Keep
  public void onRenderingInitializationFinished()
  {
    if (mColdOpen)
    {
      mColdOpen = false;
      mHomeEtaPending = true;
    }
    ThemeSwitcher.INSTANCE.restart(true);

    Framework.nativeRestoreDownloadQueue();

    if (RoutingController.get().isPlanning())
      onPlanningStarted();
    else if (RoutingController.get().isNavigating())
      onNavigationStarted();
    else if (RoutingController.get().hasSavedRoute())
      RoutingController.get().restoreRoute();

    processIntent();
    requestHomeEta();
  }

  /**
   * Process intents AFTER rendering is initialized.
   */
  private void processIntent()
  {
    if (!Map.isEngineCreated())
      throw new AssertionError("Must be called with initialized Drape");

    final Intent intent = getIntent();
    if (intent == null || intent.getBooleanExtra(EXTRA_CONSUMED, false))
      return;
    intent.putExtra(EXTRA_CONSUMED, true);

    final long categoryId = intent.getLongExtra(EXTRA_CATEGORY_ID, -1);
    final long bookmarkId = intent.getLongExtra(EXTRA_BOOKMARK_ID, -1);
    final long trackId = intent.getLongExtra(EXTRA_TRACK_ID, -1);
    if (bookmarkId != -1)
    {
      Objects.requireNonNull(BookmarkManager.INSTANCE.getBookmarkInfo(bookmarkId));
      BookmarkManager.INSTANCE.showBookmarkOnMap(bookmarkId);
      return;
    }
    else if (trackId != -1)
    {
      Objects.requireNonNull(BookmarkManager.INSTANCE.getTrack(trackId));
      Framework.nativeShowTrackRect(trackId);
      return;
    }
    else if (categoryId != -1)
    {
      BookmarkManager.INSTANCE.showBookmarkCategoryOnMap(categoryId);
      return;
    }

    final String countryId = intent.getStringExtra(EXTRA_COUNTRY_ID);
    if (countryId != null)
    {
      Framework.nativeShowCountry(countryId, false);
      return;
    }

    final IntentProcessor[] mIntentProcessors = {
        new Factory.UrlProcessor(),
        new Factory.KmzKmlProcessor(),
    };
    for (IntentProcessor ip : mIntentProcessors)
    {
      if (ip.process(intent, this))
        break;
    }
  }

  private static void checkMeasurementSystem()
  {
    UnitLocale.initializeCurrentUnits();
  }

  @Override
  protected int getFragmentContentResId()
  {
    return (mIsTabletLayout ? R.id.fragment_container : super.getFragmentContentResId());
  }

  @Nullable
  Fragment getFragment(Class<? extends Fragment> clazz)
  {
    if (!mIsTabletLayout)
      throw new IllegalStateException("Must be called for tablets only!");

    return getSupportFragmentManager().findFragmentByTag(clazz.getName());
  }

  void replaceFragmentInternal(Class<? extends Fragment> fragmentClass, Bundle args)
  {
    super.replaceFragment(fragmentClass, args, null);
  }

  @Override
  public void replaceFragment(@NonNull Class<? extends Fragment> fragmentClass, @Nullable Bundle args,
                              @Nullable Runnable completionListener)
  {
    if (mPanelAnimator.isVisible() && getFragment(fragmentClass) != null)
    {
      if (completionListener != null)
        completionListener.run();
      return;
    }

    mPanelAnimator.show(fragmentClass, args, completionListener);
  }

  private void showBookmarks()
  {
    BookmarkCategoriesActivity.start(this);
  }

  private void onAddPlace()
  {
    showPositionChooserForEditor(false, false);
  }

  private void showHelp()
  {
    Intent intent = new Intent(this, HelpActivity.class);
    startActivity(intent);
  }

  private void showSearch(String query)
  {
    closeSearchToolbar(false, true);
    if (mIsTabletLayout)
    {
      final Bundle args = new Bundle();
      args.putString(SearchActivity.EXTRA_QUERY, query);
      replaceFragment(SearchFragment.class, args, null);
    }
    else
    {
      SearchActivity.start(this, query);
    }
  }

  public void showEditor()
  {
    // TODO(yunikkk) think about refactoring. It probably should be called in editor.
    Editor.nativeStartEdit();
    if (mIsTabletLayout)
      replaceFragment(EditorHostFragment.class, null, null);
    else
      EditorActivity.start(this);
  }

  private void shareMyLocation()
  {
    final Location loc = MwmApplication.from(this).getLocationHelper().getSavedLocation();
    if (loc != null)
    {
      SharingUtils.shareLocation(this, loc);
      return;
    }

    dismissLocationErrorDialog();
    mLocationErrorDialog = new MaterialAlertDialogBuilder(MwmActivity.this)
                               .setMessage(R.string.unknown_current_position)
                               .setCancelable(true)
                               .setPositiveButton(R.string.ok, null)
                               .setOnDismissListener(dialog -> mLocationErrorDialog = null)
                               .show();
  }

  private void showDownloader(boolean openDownloaded)
  {
    final Bundle args = new Bundle();
    args.putBoolean(DownloaderActivity.EXTRA_OPEN_DOWNLOADED, openDownloaded);
    if (mIsTabletLayout)
    {
      closeSearchToolbar(false, true);
      replaceFragment(DownloaderFragment.class, args, null);
    }
    else
    {
      startActivity(new Intent(this, DownloaderActivity.class).putExtras(args));
    }
  }

  @Override
  public void onDisplayChangedToCar(@NonNull Runnable onTaskFinishedCallback)
  {
    startActivity(new Intent(this, MapPlaceholderActivity.class));
    mMapController.setOnDestroyListener(onTaskFinishedCallback);
    finish();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);

    final int newType = newConfig.uiMode & Configuration.UI_MODE_TYPE_MASK;
    final int oldType = mLastUiMode & Configuration.UI_MODE_TYPE_MASK;

    mLastUiMode = newConfig.uiMode;

    final boolean carModeChanged =
        newType != oldType && (newType == Configuration.UI_MODE_TYPE_CAR || oldType == Configuration.UI_MODE_TYPE_CAR);

    if (carModeChanged)
      return;

    makeNavigationBarTransparentInLightMode();
    recreate();
  }

  @SuppressLint("InlinedApi")
  @CallSuper
  @Override
  protected void onSafeCreate(@Nullable Bundle savedInstanceState)
  {
    super.onSafeCreate(savedInstanceState);
    mColdOpen = savedInstanceState == null;

    mIsTabletLayout = getResources().getBoolean(R.bool.tabletLayout);

    if (!mIsTabletLayout)
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

    setContentView(R.layout.activity_map);
    makeNavigationBarTransparentInLightMode();

    mPlacePageViewModel = new ViewModelProvider(this).get(PlacePageViewModel.class);
    mMapButtonsViewModel = new ViewModelProvider(this).get(MapButtonsViewModel.class);
    mRoutePreviewController = new RoutePreviewController(this, mMapButtonsViewModel);
    // We don't need to manually handle removing the observers it follows the activity lifecycle
    mMapButtonsViewModel.getBottomButtonsHeight().observe(this, this::onMapBottomButtonsHeightChange);
    mMapButtonsViewModel.getLayoutMode().observe(this, this::initNavigationButtons);

    // Note: You must call registerForActivityResult() before the fragment or activity is created.
    mLocationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                                                           this::onLocationPermissionsResult);
    mLocationResolutionRequest = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
                                                           this::onLocationResolutionResult);
    mPostNotificationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                                                                   this::onPostNotificationPermissionResult);
    mSettingsLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onSettingsResult);

    mShareLauncher = SharingUtils.RegisterLauncher(this);

    mDisplayManager = MwmApplication.from(this).getDisplayManager();
    if (mDisplayManager.isCarDisplayUsed())
    {
      startActivity(new Intent(this, MapPlaceholderActivity.class));
      finish();
      return;
    }
    mDisplayManager.addListener(DisplayType.Device, this);

    final Intent intent = getIntent();
    final boolean isLaunchByDeepLink = intent != null && !intent.hasCategory(Intent.CATEGORY_LAUNCHER);
    initViews(isLaunchByDeepLink);
    updateViewsInsets();

    if (getIntent().getBooleanExtra(EXTRA_UPDATE_THEME, false))
      ThemeSwitcher.INSTANCE.restart(mMapController.isRenderingActive());

    /*
     * onRenderingInitializationFinished() hook is not called when MwmActivity is recreated with the already
     * initialized Drape engine. This can happen when the activity is swiped away from the most recent app lists
     * during navigation and then restarted from the launcher. Call this hook explicitly here to run operations
     * that require initialized Drape, such as restoring navigation and processing incoming intents.
     * https://github.com/organicmaps/organicmaps/issues/6712
     */
    if (Map.isEngineCreated())
      onRenderingInitializationFinished();

    backupRunner = new PeriodicBackupRunner(this);
  }

  private void onSettingsResult(ActivityResult activityResult)
  {
    // Nothing to refresh: the map buttons do not depend on settings any more.
  }

  private void refreshLightStatusBar()
  {
    UiUtils.setLightStatusBar(this, !(ThemeUtils.isNightTheme() || RoutingController.get().isPlanning()
                                      || ChoosePositionMode.get() != ChoosePositionMode.None));
  }

  private void updateViewsInsets()
  {
    ViewCompat.setOnApplyWindowInsetsListener(mPointChooser, (view, windowInsets) -> {
      UiUtils.setViewInsetsPaddingBottom(mPointChooser, windowInsets);
      UiUtils.setViewInsetsPaddingNoBottom(mPointChooserToolbar, windowInsets);
      mNavBarHeight = isFullscreen() ? 0 : windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
      // For the first loading, set compass top margin to status bar size
      // The top inset will be then be updated by the routing controller
      if (mCurrentWindowInsets == null)
      {
        updateCompassOffset(windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                            windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right);
      }
      refreshLightStatusBar();
      updateBottomWidgetsOffset(windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).left);
      mCurrentWindowInsets = windowInsets;
      return windowInsets;
    });
  }

  private int getDownloadMapsCounter()
  {
    UpdateInfo info = MapManager.nativeGetUpdateInfo(null);
    return info == null ? 0 : info.filesCount;
  }

  private void initViews(boolean isLaunchByDeeplink)
  {
    mMapController = new MapController(findViewById(R.id.map), MwmApplication.from(this).getLocationHelper(), this,
                                       this::reportUnsupported, isLaunchByDeeplink);
    getLifecycle().addObserver(mMapController);

    initNavigationButtons();

    if (!mIsTabletLayout)
    {
      mRoutingPlanInplaceController = new RoutingPlanInplaceController(this, startRoutingOptionsForResult, this, this);
      mRoutePreviewPanelController =
          new RoutePreviewPanelController(findViewById(R.id.map_container), mRoutePreviewController::exitPreview);
      removeCurrentFragment(false);
    }

    mNavigationController =
        new NavigationController(this, v -> onSettingsOptionSelected(), this::updateBottomWidgetsOffset);
    // TrafficManager.INSTANCE.attach(mNavigationController);

    initMainMenu();
    initOnmapDownloader();
    initPositionChooser();
  }

  @Nullable
  public String getPreviewedTurnInstruction()
  {
    return mRoutePreviewController.getPreviewedTurnInstruction();
  }

  public void exitRoutePreview()
  {
    mRoutePreviewController.exitPreview();
  }

  private void initPositionChooser()
  {
    mPointChooser = findViewById(R.id.position_chooser);
    if (mPointChooser == null)
      return;

    mPointChooserToolbar = mPointChooser.findViewById(R.id.toolbar_point_chooser);
    UiUtils.showHomeUpButton(mPointChooserToolbar);
    mPointChooserToolbar.setNavigationOnClickListener(v -> closePositionChooser());
    mPointChooser.findViewById(R.id.done).setOnClickListener(v -> {
      switch (ChoosePositionMode.get())
      {
      case Api:
        final Intent apiResult = new Intent();
        final double[] center = Framework.nativeGetScreenRectCenter();
        apiResult.putExtra(Const.EXTRA_POINT_LAT, center[0]);
        apiResult.putExtra(Const.EXTRA_POINT_LON, center[1]);
        apiResult.putExtra(Const.EXTRA_ZOOM_LEVEL, Framework.nativeGetDrawScale());
        setResult(Activity.RESULT_OK, apiResult);
        finish();
        break;
      case Editor:
        if (Framework.nativeIsDownloadedMapAtScreenCenter())
          startActivity(new Intent(MwmActivity.this, FeatureCategoryActivity.class));
        else
        {
          dismissAlertDialog();
          mAlertDialog = new MaterialAlertDialogBuilder(this)
                             .setTitle(R.string.message_invalid_feature_position)
                             .setPositiveButton(R.string.ok, null)
                             .setOnDismissListener(dialog -> mAlertDialog = null)
                             .show();
        }
        break;
      case None: throw new IllegalStateException("Unexpected Framework.nativeGetChoosePositionMode()");
      }
      closePositionChooser();
    });
    UiUtils.hide(mPointChooser);
  }

  private void refreshSearchToolbar()
  {
    final CharSequence query = SearchEngine.INSTANCE.getQuery();
    if (!TextUtils.isEmpty(query))
    {
      // Close all panels and tool bars (including search) but do not stop search backend
      closeFloatingToolbars(false, false);
      // Do not show the active query on the search pill if we are planning or navigating
      if (!RoutingController.get().isNavigating() && !RoutingController.get().isPlanning())
        mMapButtonsViewModel.setActiveSearchQuery(query.toString());
    }
    else
    {
      closeSearchToolbar(true, true);
    }
  }

  /**
   *Hides/shows UI while keeping state
   * @param isUiHidden True to hide the UI
   **/
  public void hideOrShowUIWithoutClosingPlacePage(boolean isUiHidden)
  {
    // Used instead of closeBottomSheet to preserve state and hide instantly
    UiUtils.showIf(!isUiHidden, findViewById(R.id.place_page_container_fragment));
    mMapButtonsViewModel.setButtonsHidden(isUiHidden);
  }

  public void showPositionChooserForAPI(@Nullable String appName)
  {
    showPositionChooser(ChoosePositionMode.Api, false, false);
    if (!TextUtils.isEmpty(appName))
    {
      setTitle(appName);
    }
  }

  public void showPositionChooserForEditor(boolean isBusiness, boolean applyPosition)
  {
    showPositionChooser(ChoosePositionMode.Editor, isBusiness, applyPosition);
  }

  private void showPositionChooser(ChoosePositionMode mode, boolean isBusiness, boolean applyPosition)
  {
    closeFloatingToolbarsAndPanels(false);

    UiUtils.show(mPointChooser);
    mMapButtonsViewModel.setButtonsHidden(true);
    ChoosePositionMode.set(mode, isBusiness, applyPosition);
    refreshLightStatusBar();
  }

  private void hidePositionChooser()
  {
    UiUtils.hide(mPointChooser);
    ChoosePositionMode mode = ChoosePositionMode.get();
    ChoosePositionMode.set(ChoosePositionMode.None, false, false);
    mMapButtonsViewModel.setButtonsHidden(false);
    Framework.nativeDeactivatePopup();
    refreshLightStatusBar();
    if (mode == ChoosePositionMode.Api)
      finish();
  }

  private void initNavigationButtons()
  {
    prepareNavigationButtons();
    initNavigationButtons(mMapButtonsViewModel.getLayoutMode().getValue());
  }

  private void prepareNavigationButtons()
  {
    buttonsHolder = LeftButtonsHolder.getInstance(this);
    buttonsHolder.registerButton(new LeftButton() {
      @Override
      public String getCode()
      {
        return BUTTON_HELP_CODE;
      }

      @Override
      public String getPrefsName()
      {
        return getString(R.string.about_help);
      }

      @Override
      public void drawIcon(FloatingActionButton imageView)
      {
        imageView.setImageResource(R.drawable.ic_logo_monochrome);
      }

      @Override
      public void onClick(FloatingActionButton left)
      {
        Intent intent = new Intent(MwmActivity.this, HelpActivity.class);
        MwmActivity.this.startActivity(intent);
      }
    });
    buttonsHolder.registerButton(new LeftButton() {
      @Override
      public String getCode()
      {
        return BUTTON_ADD_PLACE_CODE;
      }

      @Override
      public String getPrefsName()
      {
        return getString(R.string.placepage_add_place_button);
      }

      @Override
      public void drawIcon(FloatingActionButton imageView)
      {
        imageView.setImageResource(R.drawable.ic_plus);
      }

      @Override
      public void onClick(FloatingActionButton left)
      {
        onAddPlace();
      }
    });
    buttonsHolder.registerButton(new LeftButton() {
      @Override
      public String getCode()
      {
        return BUTTON_SETTINGS_CODE;
      }

      @Override
      public String getPrefsName()
      {
        return getString(R.string.settings);
      }

      @Override
      public void drawIcon(FloatingActionButton imageView)
      {
        imageView.setImageResource(R.drawable.ic_settings);
      }

      @Override
      public void onClick(FloatingActionButton left)
      {
        onOpenSettings();
      }
    });

  }

  private void initNavigationButtons(MapButtonsController.LayoutMode layoutMode)
  {
    // Recreate the navigation buttons with the correct layout when it changes
    if (mPreviousMapLayoutMode != layoutMode)
    {
      MapButtonsController mapButtonsController = new MapButtonsController();

      FragmentTransaction transaction =
          getSupportFragmentManager().beginTransaction().replace(R.id.map_buttons, mapButtonsController);
      transaction.commit();
      mPreviousMapLayoutMode = layoutMode;
    }
  }

  @Override
  public void onSearchCanceled()
  {
    closeSearchToolbar(true, true);
  }

  @Override
  public void onCategorySearch(@NonNull String query)
  {
    closeFloatingPanels();
    // Interactive on-map search, the same way SearchWheel does it.
    SearchEngine.INSTANCE.searchInteractive(this, query, true, System.nanoTime(), false);
    SearchEngine.INSTANCE.setQuery(query);
    mMapButtonsViewModel.setActiveSearchQuery(query);
  }

  private void showHamburgerMenu()
  {
    closeFloatingPanels();
    final View anchor = findViewById(R.id.hamburger_button);
    if (anchor == null)
    {
      showBottomSheet(MAIN_MENU_ID);
      return;
    }
    final PopupMenu popup = new PopupMenu(this, anchor, Gravity.END);
    popup.inflate(R.menu.map_hamburger);
    final int updates = getDownloadMapsCounter();
    if (updates > 0)
    {
      final MenuItem download = popup.getMenu().findItem(R.id.hm_download_maps);
      download.setTitle(getString(R.string.download_maps) + " (" + updates + ")");
    }
    popup.setOnMenuItemClickListener(item -> {
      final int id = item.getItemId();
      if (id == R.id.hm_download_maps)
        onDownloadMapsOptionSelected();
      else if (id == R.id.hm_share_location)
        onShareLocationOptionSelected();
      else if (id == R.id.hm_settings)
        onSettingsOptionSelected();
      else if (id == R.id.hm_help)
        showHelp();
      else
        return false;
      return true;
    });
    popup.show();
  }

  /**
   * Shows "Home · 18 min" on the Home chip. Runs once per app open and only when everything needed is already known;
   * otherwise it is retried from the location / bookmarks callbacks, and the chip just stays "Home".
   */
  private void requestHomeEta()
  {
    if (!mHomeEtaPending)
      return;
    final RoutingController routing = RoutingController.get();
    if (routing.isPlanning() || routing.isNavigating())
    {
      mHomeEtaPending = false;
      return;
    }
    if (BookmarkManager.INSTANCE.isAsyncBookmarksLoadingInProgress())
      return;
    final Location myLocation = MwmApplication.from(this).getLocationHelper().getSavedLocation();
    if (myLocation == null)
      return;
    mHomeEtaPending = false;

    final BookmarkInfo home = BookmarkManager.INSTANCE.findBookmarkByName(HOME_BOOKMARK_NAME);
    if (home == null)
      return;

    Framework.nativeEstimateCarEta(myLocation.getLatitude(), myLocation.getLongitude(), home.getLat(), home.getLon(),
                                   seconds -> runOnUiThread(() -> {
                                     final RoutingController r = RoutingController.get();
                                     if (seconds < 0 || r.isPlanning() || r.isNavigating())
                                       return;
                                     mMapButtonsViewModel.setHomeEta(formatHomeEta((int) seconds));
                                   }));
  }

  @NonNull
  private String formatHomeEta(int seconds)
  {
    final long totalMinutes = Math.max(1, Math.round(seconds / 60.0));
    final long hours = totalMinutes / 60;
    final long minutes = totalMinutes % 60;
    final String min = getString(R.string.minute);
    if (hours == 0)
      return minutes + " " + min;
    return hours + " " + getString(R.string.hour) + (minutes == 0 ? "" : " " + minutes + " " + min);
  }

  private void onHomeClicked()
  {
    final BookmarkInfo home = BookmarkManager.INSTANCE.findBookmarkByName(HOME_BOOKMARK_NAME);
    if (home == null)
    {
      dismissAlertDialog();
      mAlertDialog = new MaterialAlertDialogBuilder(this)
                         .setMessage(R.string.home_not_set_message)
                         .setPositiveButton(R.string.ok, null)
                         .setOnDismissListener(dialog -> mAlertDialog = null)
                         .show();
      return;
    }
    startLocationToPoint(MapObject.createMapObject(FeatureId.EMPTY, MapObject.API_POINT, home.getName(), "",
                                                   home.getLat(), home.getLon()));
  }

  @Override
  public void onMapButtonClick(MapButtonsController.MapButtons button)
  {
    switch (button)
    {
    case zoomIn -> Map.zoomIn();
    case zoomOut -> Map.zoomOut();
    case routePreviewPrevious -> mRoutePreviewController.previewPreviousTurn();
    case routePreviewNext -> mRoutePreviewController.previewNextTurn();
    case routePreviewCancel -> mRoutePreviewController.exitPreview();
    case myPosition ->
    {
      Logger.i(LOCATION_TAG, "The location button pressed");
      // Calls onMyPositionModeChanged(mode + 1).
      LocationState.nativeSwitchToNextMode();
    }
    case toggleMapLayer -> toggleMapLayerBottomSheet();
    case bookmarks -> showBookmarks();
    case search ->
    {
      final String activeQuery = mMapButtonsViewModel.getActiveSearchQuery().getValue();
      if (TextUtils.isEmpty(activeQuery))
        showSearch("");
      else
      {
        closeFloatingToolbarsAndPanels(true);
        showSearch(activeQuery);
      }
    }
    case home -> onHomeClicked();
    case addPlace -> onAddPlaceOptionSelected();
    case menu -> showHamburgerMenu();
    case help -> showHelp();
    }
  }

  private boolean closeBottomSheet(String id)
  {
    MenuBottomSheetFragment bottomSheet = (MenuBottomSheetFragment) getSupportFragmentManager().findFragmentByTag(id);
    if (bottomSheet == null || !bottomSheet.isAdded())
      return false;
    bottomSheet.dismiss();
    return true;
  }

  private void showBottomSheet(String id)
  {
    MenuBottomSheetFragment.newInstance(id).show(getSupportFragmentManager(), id);
  }

  private void toggleMapLayerBottomSheet()
  {
    if (!closeBottomSheet(LAYERS_MENU_ID))
      showBottomSheet(LAYERS_MENU_ID);
  }

  /**
   * @return False if the place page was already closed, true otherwise
   */
  public boolean closePlacePage()
  {
    if (mPlacePageViewModel.getMapObject().getValue() == null)
      return false;

    mPlacePageViewModel.setMapObject(null);
    return true;
  }

  /**
   * @return False if the navigation menu was already collapsed or hidden, true otherwise
   */
  public boolean collapseNavMenu()
  {
    if (mNavigationController.isNavMenuCollapsed() || mNavigationController.isNavMenuHidden())
      return false;
    mNavigationController.collapseNavMenu();
    return true;
  }

  /**
   * @return False if the side panel was already closed, true otherwise
   */
  public boolean closeSidePanel()
  {
    if (interceptBackPress())
      return true;

    return removeCurrentFragment(true);
  }

  private void closeAllFloatingPanelsTablet()
  {
    if (!mIsTabletLayout)
      return;

    closePlacePage();
    removeCurrentFragment(true);
  }

  /**
   * @return False if the position chooser was already closed, true otherwise
   */
  private boolean closePositionChooser()
  {
    if (UiUtils.isVisible(mPointChooser))
    {
      hidePositionChooser();
      return true;
    }
    return false;
  }

  /**
   * @param clearText True to clear the search query
   * @param stopSearch True to stop the search engine
   * @return False if the search toolbar was already closed and the search query was empty, true otherwise
   */
  private boolean closeSearchToolbar(boolean clearText, boolean stopSearch)
  {
    if (isSearchActive())
    {
      if (stopSearch)
      {
        SearchEngine.INSTANCE.cancel();
        mMapButtonsViewModel.setSearchOption(null);
      }
      mMapButtonsViewModel.setActiveSearchQuery(null);
      return true;
    }
    return false;
  }

  private boolean isSearchActive()
  {
    return !TextUtils.isEmpty(mMapButtonsViewModel.getActiveSearchQuery().getValue())
        || !TextUtils.isEmpty(SearchEngine.INSTANCE.getQuery());
  }

  private void closeFloatingToolbarsAndPanels(boolean clearSearchText)
  {
    closeFloatingPanels();
    closeFloatingToolbars(clearSearchText, true);
  }

  public void closeFloatingPanels()
  {
    closeBottomSheet(LAYERS_MENU_ID);
    closeBottomSheet(MAIN_MENU_ID);
    closePlacePage();
  }

  private void closeFloatingToolbars(boolean clearSearchText, boolean stopSearch)
  {
    closePositionChooser();
    closeSearchToolbar(clearSearchText, stopSearch);
  }

  public void startLocationToPoint(final @Nullable MapObject endPoint)
  {
    closeFloatingPanels();
    if (isFullscreen())
      setFullscreen(false);

    // Calls onMyPositionModeChanged(PENDING_POSITION).
    LocationState.nativeStartPendingPositionMode();

    MapObject startPoint = MwmApplication.from(this).getLocationHelper().getMyPosition();
    RoutingController.get().prepare(startPoint, endPoint);

    // TODO: check for tablet.
    closePlacePage();
  }

  private void initMainMenu()
  {
    final View menuFrame = findViewById(R.id.menu_frame);
    mMainMenu = new MainMenu(menuFrame, (visible) -> {
      this.updateBottomWidgetsOffset();
      if (visible)
        mPlacePageViewModel.setPlacePageDistanceToTop(menuFrame.getTop());
    });

    if (mIsTabletLayout)
    {
      mPanelAnimator = new PanelAnimator(this);
    }
  }

  private void initOnmapDownloader()
  {
    mOnmapDownloader = new OnmapDownloader(this);
    if (mIsTabletLayout)
      mPanelAnimator.registerListener(mOnmapDownloader);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState)
  {
    if (!mIsTabletLayout && RoutingController.get().isPlanning())
      mRoutingPlanInplaceController.onSaveState(outState);

    if (mIsTabletLayout)
    {
      RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
      if (fragment != null)
        fragment.saveRoutingPanelState(outState);
    }

    RoutingController.get().onSaveState();

    if (!isChangingConfigurations())
      RoutingController.get().saveRoute();
    else
      // We no longer need in a saved route if it's a configuration changing: theme switching,
      // orientation changing, etc. Otherwise, the saved route might be restored at undesirable moment.
      RoutingController.get().deleteSavedRoute();

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState)
  {
    super.onRestoreInstanceState(savedInstanceState);
    if (mIsTabletLayout)
    {
      RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
      if (fragment != null)
      {
        fragment.restoreRoutingPanelState(savedInstanceState);
      }
      else if (RoutingController.get().isPlanning())
      {
        mRestoreRoutingPlanFragmentNeeded = true;
        mSavedForTabletState = savedInstanceState;
      }
    }

    if (!mIsTabletLayout && RoutingController.get().isPlanning())
      mRoutingPlanInplaceController.restoreState(savedInstanceState);

  }

  private void rebuildLastRoute()
  {
    RoutingController.get().attach(this);
    rebuildLastRouteInternal();
  }

  private void rebuildLastRouteInternal()
  {
    if (mRoutingPlanInplaceController == null)
      return;

    mRoutingPlanInplaceController.hideRoutingOptionsView();
    RoutingController.get().rebuildLastRoute();
  }

  private void onIsolinesStateChanged(@NonNull IsolinesState type)
  {
    if (type == IsolinesState.NODATA)
    {
      Toast.makeText(this, R.string.isolines_location_error_dialog, Toast.LENGTH_SHORT).show();
    }

    if (type == IsolinesState.EXPIREDDATA)
    {
      mAlertDialog = new MaterialAlertDialogBuilder(this)
                         .setTitle(R.string.downloader_update_maps)
                         .setMessage(R.string.isolines_activation_error_dialog)
                         .setPositiveButton(
                             R.string.ok, (dialog, which) -> startActivity(new Intent(this, DownloaderActivity.class)))
                         .setNegativeButton(R.string.cancel, null)
                         .setOnDismissListener(dialog -> mAlertDialog = null)
                         .show();
    }
  }

  @Override
  protected void onNewIntent(Intent intent)
  {
    setIntent(intent);
    super.onNewIntent(intent);
    if (mMapController.isRenderingActive())
      processIntent();
  }

  @CallSuper
  @Override
  protected void onResume()
  {
    super.onResume();
    ThemeSwitcher.INSTANCE.restart(mMapController.isRenderingActive());
    refreshSearchToolbar();
    setFullscreen(isFullscreen());
    makeNavigationBarTransparentInLightMode();
    if (ChoosePositionMode.get() != ChoosePositionMode.None)
    {
      UiUtils.show(mPointChooser);
      mMapButtonsViewModel.setButtonsHidden(true);
    }
    if (mOnmapDownloader != null)
      mOnmapDownloader.onResume();

    mNavigationController.refresh(getApplicationContext());
    refreshLightStatusBar();

    MwmApplication.from(this).getSensorHelper().addListener(this);
  }

  @Override
  protected void onResumeFragments()
  {
    super.onResumeFragments();
    RoutingController.get().restore();
  }

  @Override
  protected void onPause()
  {
    if (mOnmapDownloader != null)
      mOnmapDownloader.onPause();
    MwmApplication.from(this).getSensorHelper().removeListener(this);
    dismissLocationErrorDialog();
    dismissAlertDialog();
    super.onPause();
  }

  @Override
  protected void onStart()
  {
    super.onStart();
    Framework.nativePlacePageActivationListener(this);
    BookmarkManager.INSTANCE.addLoadingListener(this);
    RoutingController.get().attach(this);
    MwmApplication.from(getApplicationContext()).getIsolinesManager().attach(this::onIsolinesStateChanged);
    LocationState.nativeSetListener(this);
    MwmApplication.from(this).getLocationHelper().addListener(this);
    Utils.keepScreenOn(Config.isKeepScreenOnEnabled() || RoutingController.get().isNavigating(), getWindow());
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    Framework.nativeRemovePlacePageActivationListener(this);
    BookmarkManager.INSTANCE.removeLoadingListener(this);
    MwmApplication.from(this).getLocationHelper().removeListener(this);
    if (mDisplayManager.isDeviceDisplayUsed() && !RoutingController.get().isNavigating())
    {
      LocationState.nativeRemoveListener();
      RoutingController.get().detach();
    }
    MwmApplication.from(getApplicationContext()).getIsolinesManager().detach();
    Utils.keepScreenOn(false, getWindow());

    final String backUrl = Framework.nativeGetParsedBackUrl();
    if (!TextUtils.isEmpty(backUrl))
      Utils.openUri(this, Uri.parse(backUrl), null);

    if (backupRunner != null && !backupRunner.isAlreadyChecked() && backupRunner.isTimeToBackup())
    {
      backupRunner.doBackup();
    }
  }

  @CallSuper
  @Override
  protected void onSafeDestroy()
  {
    super.onSafeDestroy();
    mLocationPermissionRequest.unregister();
    mLocationPermissionRequest = null;
    mLocationResolutionRequest.unregister();
    mLocationResolutionRequest = null;
    mPostNotificationPermissionRequest.unregister();
    mPostNotificationPermissionRequest = null;
    if (!isChangingConfigurations())
      mDisplayManager.removeListener(DisplayType.Device, this);
  }

  @Override
  public void onBackPressed()
  {
    final RoutingController routingController = RoutingController.get();
    if (!closeBottomSheet(MAIN_MENU_ID) && !closeBottomSheet(LAYERS_MENU_ID) && !collapseNavMenu() && !closePlacePage()
        && !closeSearchToolbar(true, true) && !closeSidePanel() && !closePositionChooser()
        && !exitRoutePreviewIfPreviewing()
        && !routingController.resetToPlanningStateIfNavigating() && !routingController.cancel())
    {
      try
      {
        super.onBackPressed();
      }
      catch (IllegalStateException e)
      {
        // Sometimes this can be called after onSaveState() for unknown reason.
      }
    }
  }

  private boolean exitRoutePreviewIfPreviewing()
  {
    if (!RoutingController.get().isPreviewingTurn())
      return false;

    exitRoutePreview();
    return true;
  }

  private boolean interceptBackPress()
  {
    final FragmentManager manager = getSupportFragmentManager();
    for (String tag : DOCKED_FRAGMENTS)
    {
      final Fragment fragment = manager.findFragmentByTag(tag);
      if (fragment != null && fragment.isResumed() && fragment instanceof OnBackPressListener)
        return ((OnBackPressListener) fragment).onBackPressed();
    }

    return false;
  }

  private void removeFragmentImmediate(Fragment fragment)
  {
    FragmentManager fm = getSupportFragmentManager();
    if (fm.isDestroyed())
      return;

    fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
    fm.executePendingTransactions();
  }

  private boolean removeCurrentFragment(boolean animate)
  {
    for (String tag : DOCKED_FRAGMENTS)
      if (removeFragment(tag, animate))
        return true;

    return false;
  }

  private boolean removeFragment(String className, boolean animate)
  {
    if (animate && mPanelAnimator == null)
      animate = false;

    final Fragment fragment = getSupportFragmentManager().findFragmentByTag(className);
    if (fragment == null)
      return false;

    if (animate)
      mPanelAnimator.hide(() -> removeFragmentImmediate(fragment));
    else
      removeFragmentImmediate(fragment);

    return true;
  }

  @Override
  public void onPlacePageActivated(@NonNull PlacePageData data)
  {
    // This will open the place page
    mPlacePageViewModel.setMapObject((MapObject) data);
  }

  @Override
  public void onPlacePageDeactivated()
  {
    closePlacePage();
  }

  @Override
  public void onSwitchFullScreenMode()
  {
    if ((mPanelAnimator != null && mPanelAnimator.isVisible()) || !TextUtils.isEmpty(mMapButtonsViewModel.getActiveSearchQuery().getValue()))
      return;

    if (isFullscreen())
    {
      // Always allow exiting fullscreen.
      setFullscreen(false);
    }
    else
    {
      // Only enter fullscreen if the setting is enabled.
      if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_long_tap_fullscreen), false))
      {
        Toast.makeText(this, R.string.long_tap_disabled_toast, Toast.LENGTH_LONG).show();
        return;
      }
      setFullscreen(true);
      closePlacePage();
    }
  }

  private void setFullscreen(boolean isFullscreen)
  {
    if (RoutingController.get().isNavigating() || RoutingController.get().isBuilding()
        || RoutingController.get().isPlanning())
      return;

    mMapButtonsViewModel.setButtonsHidden(isFullscreen);
    UiUtils.setFullscreen(this, isFullscreen);
  }

  private boolean isFullscreen()
  {
    // Buttons are hidden in position chooser mode but we are not in fullscreen
    return Boolean.TRUE.equals(mMapButtonsViewModel.getButtonsHidden().getValue())
 && ChoosePositionMode.get() == ChoosePositionMode.None;
  }

  @Override
  public boolean dispatchGenericMotionEvent(MotionEvent event)
  {
    if (mDisplayManager.isDeviceDisplayUsed())
      mMapController.getView().dispatchWindowMotionEvent(event);

    if (event.getActionMasked() == MotionEvent.ACTION_SCROLL)
    {
      int exponent = event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0 ? -1 : 1;
      Map.onScale(Math.pow(1.7f, exponent), event.getX(), event.getY(), true);
      return true;
    }
    return super.dispatchGenericMotionEvent(event);
  }

  public void customOnNavigateUp()
  {
    if (removeCurrentFragment(true))
    {
      refreshSearchToolbar();
    }
  }

  void updateCompassOffset(int offsetY)
  {
    updateCompassOffset(offsetY, -1);
  }

  void updateCompassOffset(int offsetY, int offsetX)
  {
    mMapController.updateCompassOffset(offsetX, offsetY);

    final double north = MwmApplication.from(this).getSensorHelper().getSavedNorth();
    if (!Double.isNaN(north))
      Map.onCompassUpdated(north, true);
  }

  public void onMapBottomButtonsHeightChange(float height)
  {
    updateBottomWidgetsOffset();
  }

  public void updateBottomWidgetsOffset()
  {
    updateBottomWidgetsOffset(-1);
  }

  public void updateBottomWidgetsOffset(int offsetX)
  {
    int offsetY = mNavBarHeight;
    final Float bottomButtonHeight = mMapButtonsViewModel.getBottomButtonsHeight().getValue();
    if (bottomButtonHeight != null)
      offsetY = Math.max(offsetY, bottomButtonHeight.intValue() + mNavBarHeight);
    if (mMainMenu != null)
      offsetY = Math.max(offsetY, mMainMenu.getMenuHeight());

    final View navBottomSheetLineFrame = findViewById(R.id.line_frame);
    final View navBottomSheetNavBar = findViewById(R.id.nav_bottom_sheet_nav_bar);
    if (navBottomSheetLineFrame != null)
      offsetY = Math.max(offsetY, navBottomSheetLineFrame.getHeight() + navBottomSheetNavBar.getHeight());

    if (mDisplayManager.isDeviceDisplayUsed())
    {
      mMapController.updateBottomWidgetsOffset(offsetX, offsetY);
      mMapController.updateMyPositionRoutingOffset(offsetY);
    }
  }

  @Override
  public void updateMenu()
  {
    final RoutingController controller = RoutingController.get();

    if (controller.isNavigating())
    {
      mNavigationController.show(true);
      closeSearchToolbar(false, false);
      mMainMenu.setState(MainMenu.State.NAVIGATION, isFullscreen());
      return;
    }

    if (controller.isBuilt())
    {
      showMainMenu(true);
      return;
    }

    if (controller.isPlanning() || controller.isBuilding() || controller.isErrorEncountered())
    {
      if (showAddStartOrFinishFrame(controller, true))
        return;

      if (controller.isPlanning())
      {
        mMainMenu.setState(MainMenu.State.ROUTE_PREPARE, isFullscreen());
        return;
      }
    }

    mMainMenu.setState(MainMenu.State.MENU, isFullscreen());
  }

  private boolean showAddStartOrFinishFrame(@NonNull RoutingController controller, boolean showFrame)
  {
    // S - start, F - finish, L - my position
    // -S-F-L -> Start
    // -S-F+L -> Finish
    // -S+F-L -> Start
    // -S+F+L -> Start + Use
    // +S-F-L -> Finish
    // +S-F+L -> Finish
    // +S+F-L -> Hide
    // +S+F+L -> Hide

    MapObject myPosition = MwmApplication.from(this).getLocationHelper().getMyPosition();

    if (myPosition != null && controller.getEndPoint() == null)
    {
      showAddFinishFrame();
      if (showFrame)
        showMainMenu(true);
      return true;
    }
    if (controller.getStartPoint() == null)
    {
      showAddStartFrame();
      if (showFrame)
        showMainMenu(true);
      return true;
    }
    if (controller.getEndPoint() == null)
    {
      showAddFinishFrame();
      if (showFrame)
        showMainMenu(true);
      return true;
    }

    return false;
  }

  private void showAddStartFrame()
  {
    if (!mIsTabletLayout)
    {
      mRoutingPlanInplaceController.showAddStartFrame();
      return;
    }

    RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
    if (fragment != null)
      fragment.showAddStartFrame();
  }

  private void showAddFinishFrame()
  {
    if (!mIsTabletLayout)
    {
      mRoutingPlanInplaceController.showAddFinishFrame();
      return;
    }

    RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
    if (fragment != null)
      fragment.showAddFinishFrame();
  }

  private void showMainMenu(boolean show)
  {
    mMainMenu.show(show);
  }

  @Override
  public void onRoutingPlanStartAnimate(boolean show)
  {
    // TODO This code section may be called when insets are not yet initialized
    // This is only a workaround to prevent crashes but a proper fix should be implemented
    if (mCurrentWindowInsets == null)
    {
      return;
    }
    int offsetY = mCurrentWindowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
    int offsetX = mCurrentWindowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).right;
    if (show && mRoutingPlanInplaceController != null)
    {
      final int height = mRoutingPlanInplaceController.calcHeight();
      if (height != 0)
        offsetY = height;
    }
    final int orientation = getResources().getConfiguration().orientation;
    if (orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
      if (show)
      {
        final boolean isSmallScreen = UiUtils.getDisplayTotalHeight(this) < dimen(this, R.dimen.dp_400);
        if (!isSmallScreen)
          offsetX += dimen(this, R.dimen.map_button_size);
      }
    }
    updateCompassOffset(offsetY, offsetX);
  }

  @Override
  public void showRoutePlan(boolean show, @Nullable Runnable completionListener)
  {
    if (show)
    {
      if (mIsTabletLayout)
      {
        replaceFragment(RoutingPlanFragment.class, null, completionListener);
        if (mRestoreRoutingPlanFragmentNeeded && mSavedForTabletState != null)
        {
          RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
          if (fragment != null)
            fragment.restoreRoutingPanelState(mSavedForTabletState);
        }
        showAddStartOrFinishFrame(RoutingController.get(), false);
      }
      else
      {
        mRoutingPlanInplaceController.show(true);
        if (completionListener != null)
          completionListener.run();
      }
    }
    else
    {
      if (mIsTabletLayout && mCurrentWindowInsets != null)
        updateCompassOffset(mCurrentWindowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top);
      else if (!mIsTabletLayout)
        mRoutingPlanInplaceController.show(false);

      closeAllFloatingPanelsTablet();

      if (completionListener != null)
        completionListener.run();
    }
  }

  @Override
  public void showNavigation(boolean show)
  {
    // TODO:
    // mPlacePage.refreshViews();
    mNavigationController.show(show);
    if (mOnmapDownloader != null)
      mOnmapDownloader.updateState(false);
  }

  @Override
  public void updateBuildProgress(int progress, Router router)
  {
    if (mIsTabletLayout)
    {
      RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
      if (fragment != null)
        fragment.updateBuildProgress(progress, router);
    }
    else
    {
      mRoutingPlanInplaceController.updateBuildProgress(progress, router);
    }
  }

  @Override
  public void onTurnPreviewChanged(boolean isPreviewing, int segmentIndex)
  {
    mRoutePreviewController.onTurnPreviewChanged(isPreviewing, segmentIndex);

    if (mIsTabletLayout)
    {
      RoutingPlanFragment fragment = (RoutingPlanFragment) getFragment(RoutingPlanFragment.class);
      if (fragment != null)
        fragment.onTurnPreviewChanged(isPreviewing, getPreviewedTurnInstruction());
    }
    else if (mRoutingPlanInplaceController != null)
    {
      mRoutingPlanInplaceController.onTurnPreviewChanged(isPreviewing);
      if (mRoutePreviewPanelController != null)
        mRoutePreviewPanelController.onTurnPreviewChanged(isPreviewing, getPreviewedTurnInstruction());
    }
  }

  @Override
  public void refreshNavigationController()
  {
    if (mNavigationController != null)
      mNavigationController.refresh(getApplicationContext());
  }

  @Override
  public void onStartRouteBuilding()
  {
    if (mRoutingPlanInplaceController == null)
      return;

    mRoutingPlanInplaceController.hideRoutingOptionsView();
  }

  @Override
  public void onNavigationCancelled()
  {
    closeFloatingToolbarsAndPanels(true);
    ThemeSwitcher.INSTANCE.restart(mMapController.isRenderingActive());
    if (mRoutingPlanInplaceController == null)
      return;

    mRoutingPlanInplaceController.hideRoutingOptionsView();
    NavigationService.stopService(this);
    mMapButtonsViewModel.setSearchOption(null);
    mMapButtonsViewModel.setLayoutMode(MapButtonsController.LayoutMode.regular);
    refreshLightStatusBar();
    Utils.keepScreenOn(Config.isKeepScreenOnEnabled(), getWindow());
  }

  @Override
  public void onNavigationStarted()
  {
    mMapButtonsViewModel.setHomeEta(null);
    closeFloatingToolbarsAndPanels(true);
    ThemeSwitcher.INSTANCE.restart(mMapController.isRenderingActive());
    mMapButtonsViewModel.setLayoutMode(MapButtonsController.LayoutMode.navigation);
    refreshLightStatusBar();

    // Don't start the background navigation service without fine location.
    if (!LocationUtils.checkFineLocationPermission(this))
    {
      Logger.w(LOCATION_TAG, "Permission ACCESS_FINE_LOCATION is not granted, skipping NavigationService");
      return;
    }

    requestPostNotificationsPermission();
    NavigationService.startForegroundService(this);
    Utils.keepScreenOn(true, getWindow());
  }

  @Override
  public void onPlanningCancelled()
  {
    closeFloatingToolbarsAndPanels(true);
    mMapButtonsViewModel.setLayoutMode(MapButtonsController.LayoutMode.regular);
    refreshLightStatusBar();
  }

  @Override
  public void onPlanningStarted()
  {
    mMapButtonsViewModel.setHomeEta(null);
    closeFloatingToolbarsAndPanels(true);
    mMapButtonsViewModel.setLayoutMode(MapButtonsController.LayoutMode.planning);
    refreshLightStatusBar();
  }

  @Override
  public void onResetToPlanningState()
  {
    closeFloatingToolbarsAndPanels(true);
    ThemeSwitcher.INSTANCE.restart(mMapController.isRenderingActive());
    NavigationService.stopService(this);
    mMapButtonsViewModel.setSearchOption(null);
    mMapButtonsViewModel.setLayoutMode(MapButtonsController.LayoutMode.planning);
    refreshLightStatusBar();
  }

  @Override
  public void onAddedStop()
  {
    closePlacePage();
  }

  @Override
  public void onRemovedStop()
  {
    closePlacePage();
  }

  @Override
  public void onBuiltRoute()
  {
    if (!RoutingController.get().isPlanning())
      return;

    closeSearchToolbar(true, true);
  }

  @Override
  public void onDrivingOptionsWarning()
  {
    if (mRoutingPlanInplaceController == null)
      return;

    mRoutingPlanInplaceController.showRoutingOptionsView();
  }

  @Override
  public void onCommonBuildError(int lastResultCode, @NonNull String[] lastMissingMaps)
  {
    RoutingErrorDialogFragment fragment = RoutingErrorDialogFragment.create(
        getSupportFragmentManager().getFragmentFactory(), getApplicationContext(), lastResultCode, lastMissingMaps);
    fragment.show(getSupportFragmentManager(), RoutingErrorDialogFragment.class.getSimpleName());
  }

  @Override
  public void onDrivingOptionsBuildError()
  {
    dismissAlertDialog();
    mAlertDialog =
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.unable_to_calc_alert_title)
            .setMessage(R.string.unable_to_calc_alert_subtitle)
            .setPositiveButton(R.string.settings,
                               (dialog, which) -> RoutingOptionsActivity.start(this, startRoutingOptionsForResult))
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener(dialog -> mAlertDialog = null)
            .show();
  }

  private boolean showRoutingDisclaimer()
  {
    if (Config.isRoutingDisclaimerAccepted())
      return true;

    final StringBuilder builder = new StringBuilder();
    for (int resId :
         new int[] {R.string.dialog_routing_disclaimer_priority, R.string.dialog_routing_disclaimer_precision,
                    R.string.dialog_routing_disclaimer_recommendations, R.string.dialog_routing_disclaimer_borders,
                    R.string.dialog_routing_disclaimer_beware})
      builder.append(getString(resId)).append("\n\n");

    dismissAlertDialog();
    mAlertDialog = new MaterialAlertDialogBuilder(this)
                       .setTitle(R.string.dialog_routing_disclaimer_title)
                       .setMessage(builder.toString())
                       .setCancelable(false)
                       .setNegativeButton(R.string.decline, null)
                       .setPositiveButton(R.string.accept,
                                          (dlg, which) -> {
                                            Config.acceptRoutingDisclaimer();
                                            onRoutingStart();
                                          })
                       .setOnDismissListener(dialog -> mAlertDialog = null)
                       .show();

    return false;
  }

  private void deliverTtsMessage()
  {
    if (Config.isTtsMessageDelivered())
      return;

    String languageDisplayName = TtsPlayer.INSTANCE.getLanguageDisplayName();

    if (languageDisplayName != null)
    {
      String navigationStartMessage = getResources().getString(R.string.navigation_start_tts_message);
      navigationStartMessage += languageDisplayName;
      Toast.makeText(this, navigationStartMessage, Toast.LENGTH_LONG).show();
    }
    else
      Toast.makeText(this, getResources().getString(R.string.navigation_start_tts_disabled_message), Toast.LENGTH_LONG)
          .show();

    Config.setTtsMessageDelivered();
  }

  private boolean showStartPointNotice()
  {
    final RoutingController controller = RoutingController.get();

    if (showAddStartOrFinishFrame(controller, true))
      return false;

    // Starting and ending points must be non-null, see {@link #showAddStartOrFinishFrame() }.
    final MapObject startPoint = Objects.requireNonNull(controller.getStartPoint());
    if (startPoint.isMyPosition())
      return true;

    final MapObject endPoint = Objects.requireNonNull(controller.getEndPoint());
    final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
        .setTitle(R.string.p2p_only_from_current)
        .setMessage(R.string.p2p_reroute_from_current)
        .setCancelable(false)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.ok, endPoint.isMyPosition() ?
            (dialog, which) -> controller.swapPoints() :
            (dialog, which) -> {
              // The current location may change while this dialog is still shown on the screen.
              final MapObject myPosition = MwmApplication.from(this).getLocationHelper().getMyPosition();
              controller.setStartPoint(myPosition);
            }
        )
        .setOnDismissListener(dialog -> mAlertDialog = null);
    dismissAlertDialog();
    mAlertDialog = builder.show();
    return false;
  }

  @Override
  public void onMyPositionModeChanged(int newMode)
  {
    Logger.d(LOCATION_TAG, "newMode = " + LocationState.nameOf(newMode));
    mMapButtonsViewModel.setMyPositionMode(newMode);
    RoutingController controller = RoutingController.get();
    if (controller.isPlanning() || controller.isBuilding() || controller.isErrorEncountered())
      showAddStartOrFinishFrame(controller, true);

    final LocationHelper locationHelper = MwmApplication.from(this).getLocationHelper();

    // Check if location was disabled by the user.
    if (LocationState.getMode() == LocationState.NOT_FOLLOW_NO_POSITION)
    {
      Logger.i(LOCATION_TAG, "Location updates are stopped by the user manually.");
      if (locationHelper.isActive())
        locationHelper.stop();
      return;
    }

    // Check for any location permissions.
    if (!LocationUtils.checkLocationPermission(this))
    {
      Logger.w(LOCATION_TAG, "Permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION are not granted");
      // Calls onMyPositionModeChanged(NOT_FOLLOW_NO_POSITION).
      LocationState.nativeOnLocationError(LocationState.ERROR_DENIED);

      Logger.i(LOCATION_TAG, "Requesting ACCESS_FINE_LOCATION + ACCESS_FINE_LOCATION permissions");
      dismissLocationErrorDialog();
      mLocationPermissionRequest.launch(new String[] {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION});
      return;
    }

    locationHelper.restartWithNewMode();

    if ((newMode == FOLLOW || newMode == FOLLOW_AND_ROTATE) && !LocationUtils.checkFineLocationPermission(this))
    {
      // Try to optimistically request FINE permission for FOLLOW and FOLLOW_AND_ROTATE modes.
      Logger.i(LOCATION_TAG, "Requesting ACCESS_FINE_LOCATION permission for " + LocationState.nameOf(newMode));
      dismissLocationErrorDialog();
      mLocationPermissionRequest.launch(new String[] {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION});
    }
  }

  /**
   * Dismiss the active modal dialog from the screen, if any.
   */
  private void dismissAlertDialog()
  {
    if (mAlertDialog != null && mAlertDialog.isShowing())
      mAlertDialog.dismiss();
    mAlertDialog = null;
  }

  /**
   * Dismiss location error dialog from the screen, if any.
   */
  private void dismissLocationErrorDialog()
  {
    if (mLocationErrorDialog != null && mLocationErrorDialog.isShowing())
      mLocationErrorDialog.dismiss();
    mLocationErrorDialog = null;
  }

  /**
   * Called when location is updated.
   * @param location new location
   */
  @Override
  @UiThread
  public void onLocationUpdated(@NonNull Location location)
  {
    dismissLocationErrorDialog();
    requestHomeEta();

    final RoutingController routing = RoutingController.get();
    if (!routing.isNavigating())
      return;

    RoutingInfo info = Framework.nativeGetRouteFollowingInfo();
    routing.updateCachedRoutingInfo(info);
    mNavigationController.update(info);
  }

  @Override
  @UiThread
  public void onLocationUpdateTimeout()
  {}

  /**
   * Called when compass data is updated.
   * @param north offset from the north
   */
  @Override
  @UiThread
  public void onCompassUpdated(double north)
  {
    Map.onCompassUpdated(north, false);
    mNavigationController.updateNorth();
  }

  @Override
  @UiThread
  public void onCompassCalibrationRecommended()
  {
    Toast.makeText(this, getString(R.string.compass_calibration_recommended), Toast.LENGTH_LONG).show();
  }

  @Override
  @UiThread
  public void onCompassCalibrationRequired()
  {
    Toast.makeText(this, getString(R.string.compass_calibration_required), Toast.LENGTH_LONG).show();
  }

  /**
   * Request POST_NOTIFICATIONS permission.
   */
  public void requestPostNotificationsPermission()
  {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        || ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PERMISSION_GRANTED)
    {
      Logger.i(TAG, "Permissions POST_NOTIFICATIONS is granted");
      return;
    }

    Logger.i(TAG, "Requesting POST_NOTIFICATIONS permission");
    mPostNotificationPermissionRequest.launch(POST_NOTIFICATIONS);
  }

  /**
   * Called on the result of the system location dialog.
   * @param permissions permissions granted or refused.
   */
  @UiThread
  private void onLocationPermissionsResult(java.util.Map<String, Boolean> permissions)
  {
    // Print permissions that have been granted or refused.
    for (java.util.Map.Entry<String, Boolean> entry : permissions.entrySet())
    {
      final String permission = entry.getKey().substring(entry.getKey().lastIndexOf('.') + 1);
      if (entry.getValue())
        Logger.i(LOCATION_TAG, "Permission " + permission + " has been granted");
      else
        Logger.w(LOCATION_TAG, "Permission " + permission + " has been refused");
    }

    if (LocationUtils.checkLocationPermission(this))
    {
      final boolean hasFineLocationPermission = LocationUtils.checkFineLocationPermission(this);

      LocationState.nativeStartPendingPositionMode();

      if (hasFineLocationPermission)
      {
        Logger.i(LOCATION_TAG, "ACCESS_FINE_LOCATION permission granted");
      }
      else
      {
        Logger.w(LOCATION_TAG, "Only ACCESS_COARSE_LOCATION permission granted");
        if (mLocationErrorDialog != null && mLocationErrorDialog.isShowing())
        {
          Logger.w(LOCATION_TAG, "Don't show 'Precise Location denied' dialog because another dialog is in progress");
          return;
        }
        if (!mPreciseLocationDialogShown)
        {
          mPreciseLocationDialogShown = true;
          final MaterialAlertDialogBuilder builder =
              new MaterialAlertDialogBuilder(this)
                  .setTitle("⚠ " + getString(R.string.limited_accuracy))
                  .setMessage(R.string.precise_location_is_disabled_long_text)
                  .setNegativeButton(R.string.close, (dialog, which) -> dialog.dismiss())
                  .setCancelable(true)
                  .setOnDismissListener(dialog -> mLocationErrorDialog = null);
          final Intent intent = Utils.makeSystemLocationSettingIntent(this);
          if (intent != null)
          {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            builder.setPositiveButton(R.string.location_settings, (dialog, which) -> startActivity(intent));
          }
          mLocationErrorDialog = builder.show();
        }
        else
        {
          Toast.makeText(this, R.string.precise_location_is_disabled_long_text, Toast.LENGTH_LONG).show();
        }
      }
      return;
    }

    Logger.w(LOCATION_TAG, "Permissions ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION have been refused");
    // Calls onMyPositionModeChanged(NOT_FOLLOW_NO_POSITION).
    LocationState.nativeOnLocationError(LocationState.ERROR_DENIED);

    if (mLocationErrorDialog != null && mLocationErrorDialog.isShowing())
    {
      Logger.w(LOCATION_TAG, "Don't show 'location denied' error dialog because another dialog is in progress");
      return;
    }

    mLocationErrorDialog = new MaterialAlertDialogBuilder(this)
                               .setTitle(R.string.enable_location_services)
                               .setMessage(R.string.location_is_disabled_long_text)
                               .setOnDismissListener(dialog -> mLocationErrorDialog = null)
                               .setNegativeButton(R.string.close, null)
                               .show();
  }

  /**
   * Called on the result of the POST_NOTIFICATIONS request.
   * @param granted true if permission has been granted.
   */
  @UiThread
  private void onPostNotificationPermissionResult(boolean granted)
  {
    if (granted)
      Logger.i(TAG, "Permission POST_NOTIFICATIONS has been granted");
    else
      Logger.w(TAG, "Permission POST_NOTIFICATIONS has been refused");
  }

  /**
   * Called by GoogleFusedLocationProvider to request to GPS and/or Wi-Fi.
   * @param pendingIntent an intent to launch.
   */
  @Override
  @UiThread
  public void onLocationResolutionRequired(@NonNull PendingIntent pendingIntent)
  {
    Logger.d(LOCATION_TAG);

    // Cancel our dialog in favor of system dialog.
    dismissLocationErrorDialog();

    // Launch system permission resolution dialog.
    Logger.i(LOCATION_TAG, "Starting location resolution dialog");
    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
    mLocationResolutionRequest.launch(intentSenderRequest);
  }

  /**
   * Triggered by onLocationResolutionRequired().
   * @param result invocation result.
   */
  @UiThread
  private void onLocationResolutionResult(@NonNull ActivityResult result)
  {
    final int resultCode = result.getResultCode();
    Logger.d(LOCATION_TAG, "resultCode = " + resultCode);

    if (resultCode != Activity.RESULT_OK)
    {
      Logger.w(LOCATION_TAG, "Location resolution has been refused");
      // Calls onMyPositionModeChanged(NOT_FOLLOW_NO_POSITION).
      LocationState.nativeOnLocationError(LocationState.ERROR_GPS_OFF);
      return;
    }

    Logger.i(LOCATION_TAG, "Location resolution has been granted, restarting location");

    // Calls onMyPositionModeChanged(PENDING_POSITION).
    LocationState.nativeStartPendingPositionMode();
  }

  /**
   * Called by AndroidNativeLocationProvider when no suitable location methods are available.
   */
  @Override
  @UiThread
  public void onLocationDisabled()
  {
    Logger.d(LOCATION_TAG, "settings = " + LocationUtils.areLocationServicesTurnedOn(this));

    // Calls onMyPositionModeChanged(NOT_FOLLOW_NO_POSITION).
    LocationState.nativeOnLocationError(LocationState.ERROR_GPS_OFF);

    if (mLocationErrorDialog != null && mLocationErrorDialog.isShowing())
    {
      Logger.d(LOCATION_TAG, "Don't show 'location disabled' error dialog because another dialog is in progress");
      return;
    }

    final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                                                   .setTitle(R.string.enable_location_services)
                                                   .setMessage(R.string.location_is_disabled_long_text)
                                                   .setOnDismissListener(dialog -> mLocationErrorDialog = null)
                                                   .setNegativeButton(R.string.close, null);
    final Intent intent = Utils.makeSystemLocationSettingIntent(this);
    if (intent != null)
    {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
      intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
      builder.setPositiveButton(R.string.location_settings, (dialog, which) -> startActivity(intent));
    }
    mLocationErrorDialog = builder.show();
  }

  @Override
  public void onUseMyPositionAsStart()
  {
    RoutingController.get().setStartPoint(MwmApplication.from(this).getLocationHelper().getMyPosition());
  }

  @Override
  public void onSearchRoutePoint(@NonNull RouteMarkType pointType)
  {
    RoutingController.get().waitForPoiPick(pointType);
    closeSearchToolbar(true, true);
    showSearch("");
  }

  @Override
  public void onRoutingStart()
  {
    if (!showStartPointNotice())
    {
      UiUtils.setFullscreen(this, false);
      return;
    }

    if (!showRoutingDisclaimer())
      return;

    deliverTtsMessage();

    closeFloatingPanels();
    setFullscreen(false);

    RoutingController.get().start();
  }

  @Override
  public void onManageRouteOpen()
  {
    // Create and show 'Manage Route' Bottom Sheet panel.
    mManageRouteBottomSheet = new ManageRouteBottomSheet();
    mManageRouteBottomSheet.show(getSupportFragmentManager(), "ManageRouteBottomSheet");
  }

  @Override
  public void onDirectionsPreviewOpen() {
    mDirectionsPreviewBottomSheet = new DirectionsPreviewBottomSheet();
    mDirectionsPreviewBottomSheet.show(getSupportFragmentManager(), "DirectionsPreviewBottomSheet");
  }

  @Override
  public void onBookmarksLoadingFinished()
  {
    requestHomeEta();
  }

  @Override
  public void onBookmarksFileUnsupported(@NonNull Uri uri)
  {
    dismissAlertDialog();
    mAlertDialog =
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.load_kmz_title)
            .setMessage(getString(R.string.unknown_file_type, uri))
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.report_a_bug,
                               (dialog, which)
                                   -> Utils.sendBugReport(mShareLauncher, this, getString(R.string.load_kmz_title),
                                                          getString(R.string.unknown_file_type, uri)))
            .setOnDismissListener(dialog -> mAlertDialog = null)
            .show();
  }

  @Override
  public void onBookmarksFileDownloadFailed(@NonNull Uri uri, @NonNull String error)
  {
    dismissAlertDialog();
    mAlertDialog =
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.load_kmz_title)
            .setMessage(getString(R.string.failed_to_open_file, uri, error))
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.report_a_bug,
                               (dialog, which)
                                   -> Utils.sendBugReport(mShareLauncher, this, getString(R.string.load_kmz_title),
                                                          getString(R.string.failed_to_open_file, uri, error)))
            .setOnDismissListener(dialog -> mAlertDialog = null)
            .show();
  }

  @Override
  public void onBookmarksFileImportSuccessful()
  {
    Utils.showSnackbar(this, findViewById(R.id.coordinator), R.string.load_kmz_successful);
  }

  @Override
  public void onBookmarksFileImportFailed()
  {
    dismissAlertDialog();
    mAlertDialog = new MaterialAlertDialogBuilder(this)
                       .setTitle(R.string.load_kmz_title)
                       .setMessage(R.string.load_kmz_failed)
                       .setPositiveButton(R.string.ok, null)
                       .setOnDismissListener(dialog -> mAlertDialog = null)
                       .show();
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event)
  {
    switch (keyCode)
    {
    case KeyEvent.KEYCODE_DPAD_DOWN: Map.zoomOut(); return true;
    case KeyEvent.KEYCODE_DPAD_UP: Map.zoomIn(); return true;
    case KeyEvent.KEYCODE_ESCAPE:
      final Intent currIntent = getIntent();
      final String backUrl = Framework.nativeGetParsedBackUrl();
      if (TextUtils.isEmpty(backUrl) || (currIntent != null && Factory.isStartedForApiResult(currIntent)))
      {
        finish();
        return true;
      }
      return super.onKeyUp(keyCode, event);
    default: return super.onKeyUp(keyCode, event);
    }
  }

  public void onAddPlaceOptionSelected()
  {
    closeFloatingPanels();
    onAddPlace();
  }

  public void onDownloadMapsOptionSelected()
  {
    RoutingController.get().cancel();
    closeFloatingPanels();
    showDownloader(false);
  }

  public void onSettingsOptionSelected()
  {
    closeFloatingPanels();
    onOpenSettings();
  }

  private void onOpenSettings()
  {
    Intent intent = new Intent(this, SettingsActivity.class);
    mSettingsLauncher.launch(intent);
  }

  public void onShareLocationOptionSelected()
  {
    closeFloatingPanels();
    shareMyLocation();
  }

  @Override
  @Nullable
  public ArrayList<MenuBottomSheetItem> getMenuBottomSheetItems(String id)
  {
    if (id.equals(MAIN_MENU_ID))
    {
      final String activeLeftButton = buttonsHolder.getActiveButtonCode();
      ArrayList<MenuBottomSheetItem> items = new ArrayList<>();

      if (!BUTTON_ADD_PLACE_CODE.equals(activeLeftButton))
        items.add(new MenuBottomSheetItem(R.string.placepage_add_place_button, R.drawable.ic_plus,
                                          this::onAddPlaceOptionSelected));

      items.add(new MenuBottomSheetItem(R.string.download_maps, R.drawable.ic_download, getDownloadMapsCounter(),
                                        this::onDownloadMapsOptionSelected));

      if (!BUTTON_SETTINGS_CODE.equals(activeLeftButton))
        items.add(new MenuBottomSheetItem(R.string.settings, R.drawable.ic_settings, this::onSettingsOptionSelected));

      items.add(new MenuBottomSheetItem(R.string.share_my_location, R.drawable.ic_share,
                                        this::onShareLocationOptionSelected));

      if (!BUTTON_HELP_CODE.equals(activeLeftButton))
        items.add(new MenuBottomSheetItem(R.string.about_help, R.drawable.ic_logo_monochrome, this::showHelp));

      return items;
    }
    return null;
  }

  @Override
  @Nullable
  public Fragment getMenuBottomSheetFragment(String id)
  {
    if (id.equals(LAYERS_MENU_ID))
      return new ToggleMapLayerFragment();
    return null;
  }

  @Override
  public void onPlacePageRequestToggleRouteSettings(@NonNull RoadType roadType)
  {
    closePlacePage();
    Router routerType = RoutingController.get().getLastRouterType();
    RoutingOptions.addOption(roadType, routerType);
    rebuildLastRouteInternal();
  }

  @Override
  public void onTrimMemory(int level)
  {
    super.onTrimMemory(level);
    Logger.d(TAG, "trim memory, level = " + level);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
    {
      // With API Level >= 35, only TRIM_MEMORY_BACKGROUND and TRIM_MEMORY_UI_HIDDEN memory
      // levels are reported.
      if (level >= TRIM_MEMORY_BACKGROUND)
        Framework.nativeMemoryWarning();
    }
    else
    {
      // Suppress warning of deprecated TRIM_MEMORY_RUNNING_LOW trim memory level
      // with API level < 35.
      @SuppressWarnings("deprecation")
      int WARNING_LEVEL = TRIM_MEMORY_RUNNING_LOW;
      if (level >= WARNING_LEVEL)
        Framework.nativeMemoryWarning();
    }
  }

  private void makeNavigationBarTransparentInLightMode()
  {
    int nightMask = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    if (nightMask == Configuration.UI_MODE_NIGHT_NO) // if light mode
    {
      Window window = getWindow();
      window.setNavigationBarColor(Color.TRANSPARENT);
      window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

      int flags = window.getDecorView().getSystemUiVisibility();
      flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

      window.getDecorView().setSystemUiVisibility(flags);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        window.setNavigationBarContrastEnforced(false);
    }
  }

  private void reportUnsupported()
  {
    new MaterialAlertDialogBuilder(this)
        .setMessage(R.string.unsupported_phone)
        .setCancelable(false)
        .setPositiveButton(R.string.close, (dlg, which) -> this.moveTaskToBack(true))
        .show();
  }
}
