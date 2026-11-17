package app.organicmaps.car.screens;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.CarToast;
import androidx.car.app.model.Action;
import androidx.car.app.model.CarIcon;
import androidx.car.app.model.Header;
import androidx.car.app.model.Pane;
import androidx.car.app.model.PaneTemplate;
import androidx.car.app.model.Row;
import androidx.car.app.model.Template;
import androidx.car.app.navigation.model.MapWithContentTemplate;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.LifecycleOwner;
import app.organicmaps.R;
import app.organicmaps.car.renderer.Renderer;
import app.organicmaps.car.screens.base.BaseMapScreen;
import app.organicmaps.car.util.Colors;
import app.organicmaps.car.util.UiHelpers;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.sdk.routing.RoutingController;

/**
 * Shown when a place is selected (search result, bookmark or map tap) while navigating.
 * Lets the driver add the place as an intermediate stop and then resumes navigation on the
 * rebuilt route.
 */
public class AddStopScreen extends BaseMapScreen implements RoutingController.Container
{
  @NonNull
  private final MapObject mMapObject;
  @NonNull
  private final RoutingController mRoutingController;

  // True between tapping "Add stop" and the rebuilt route starting (or failing).
  private boolean mAddingStop = false;

  private AddStopScreen(@NonNull CarContext carContext, @NonNull Renderer surfaceRenderer,
                        @NonNull MapObject mapObject)
  {
    super(carContext, surfaceRenderer);
    mMapObject = mapObject;
    mRoutingController = RoutingController.get();
  }

  @NonNull
  public static AddStopScreen create(@NonNull CarContext carContext, @NonNull Renderer surfaceRenderer,
                                     @NonNull MapObject mapObject)
  {
    return new AddStopScreen(carContext, surfaceRenderer, mapObject);
  }

  @NonNull
  @Override
  protected Template onGetTemplateImpl()
  {
    final MapWithContentTemplate.Builder builder = new MapWithContentTemplate.Builder();
    builder.setMapController(UiHelpers.createMapController(getCarContext(), getSurfaceRenderer()));
    builder.setContentTemplate(createPaneTemplate());
    return builder.build();
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner)
  {
    super.onDestroy(owner);
    Framework.nativeDeactivatePopup();
  }

  @NonNull
  private PaneTemplate createPaneTemplate()
  {
    final Header header = new Header.Builder()
                              .setStartHeaderAction(Action.BACK)
                              .setTitle(getCarContext().getString(R.string.placepage_add_stop))
                              .build();
    return new PaneTemplate.Builder(createPane()).setHeader(header).build();
  }

  @NonNull
  private Pane createPane()
  {
    final Pane.Builder builder = new Pane.Builder();
    if (mAddingStop)
    {
      builder.setLoading(true);
      return builder.build();
    }

    builder.addRow(createPlaceRow());

    final Row openingHours = UiHelpers.getPlaceOpeningHoursRow(mMapObject, getCarContext());
    if (openingHours != null)
      builder.addRow(openingHours);

    final Action addStop =
        new Action.Builder()
            .setTitle(getCarContext().getString(R.string.placepage_add_stop))
            .setIcon(new CarIcon.Builder(IconCompat.createWithResource(getCarContext(), R.drawable.ic_plus)).build())
            .setBackgroundColor(Colors.START_NAVIGATION)
            .setFlags(Action.FLAG_PRIMARY)
            .setOnClickListener(this::addStop)
            .build();
    builder.addAction(addStop);
    return builder.build();
  }

  @NonNull
  private Row createPlaceRow()
  {
    final Row.Builder builder = new Row.Builder();
    builder.setTitle(mMapObject.getTitle());
    if (!mMapObject.getSubtitle().isEmpty())
      builder.addText(mMapObject.getSubtitle());
    String address = mMapObject.getAddress();
    if (address.isEmpty())
      address = Framework.nativeGetAddress(mMapObject.getLat(), mMapObject.getLon());
    if (!address.isEmpty())
      builder.addText(address);
    return builder.build();
  }

  private void addStop()
  {
    if (mAddingStop || !mRoutingController.isNavigating())
      return;

    mAddingStop = true;
    invalidate();
    // Route this screen's callbacks: the controller drops back to planning, rebuilds the route
    // with the stop, and onBuiltRoute() restarts navigation.
    mRoutingController.attach(this);
    mRoutingController.addStop(mMapObject);
  }

  @Override
  public void onBuiltRoute()
  {
    if (!mAddingStop)
      return;
    mAddingStop = false;
    mRoutingController.start();
  }

  @Override
  public void showNavigation(boolean show)
  {
    if (!show)
      return;
    // Back to the existing NavigationScreen, which re-attaches itself to the controller on resume.
    getScreenManager().popTo(NavigationScreen.MARKER);
  }

  @Override
  public void onCommonBuildError(int lastResultCode, @NonNull String[] lastMissingMaps)
  {
    onBuildFailed();
  }

  @Override
  public void onDrivingOptionsBuildError()
  {
    onBuildFailed();
  }

  private void onBuildFailed()
  {
    if (!mAddingStop)
      return;
    mAddingStop = false;
    CarToast.makeText(getCarContext(), R.string.unable_to_calc_alert_title, CarToast.LENGTH_LONG).show();
    // The controller already left navigation mode to rebuild the route, so there is nothing to resume.
    mRoutingController.cancel();
    getScreenManager().popToRoot();
  }
}
