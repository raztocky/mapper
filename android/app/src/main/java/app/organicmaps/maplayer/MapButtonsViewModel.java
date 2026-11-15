package app.organicmaps.maplayer;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapButtonsViewModel extends ViewModel
{
  private final MutableLiveData<Boolean> mButtonsHidden = new MutableLiveData<>(false);
  private final MutableLiveData<Float> mBottomButtonsHeight = new MutableLiveData<>(0f);
  private final MutableLiveData<MapButtonsController.LayoutMode> mLayoutMode =
      new MutableLiveData<>(MapButtonsController.LayoutMode.regular);
  private final MutableLiveData<Integer> mMyPositionMode = new MutableLiveData<>();
  private final MutableLiveData<SearchWheel.SearchOption> mSearchOption = new MutableLiveData<>();
  // Query currently shown on the map by an interactive search, or null/empty if there is none.
  private final MutableLiveData<String> mActiveSearchQuery = new MutableLiveData<>(null);
  // "Home" chip label suffix (e.g. "18 min"), or null to show the bare "Home" chip.
  private final MutableLiveData<String> mHomeEta = new MutableLiveData<>(null);
  private final MutableLiveData<RoutePreviewControlsState> mRoutePreviewControlsState =
      new MutableLiveData<>(RoutePreviewControlsState.none());

  public MutableLiveData<Boolean> getButtonsHidden()
  {
    return mButtonsHidden;
  }

  public void setButtonsHidden(boolean buttonsHidden)
  {
    mButtonsHidden.setValue(buttonsHidden);
  }

  public MutableLiveData<Float> getBottomButtonsHeight()
  {
    return mBottomButtonsHeight;
  }

  public void setBottomButtonsHeight(float height)
  {
    mBottomButtonsHeight.setValue(height);
  }

  public MutableLiveData<MapButtonsController.LayoutMode> getLayoutMode()
  {
    return mLayoutMode;
  }

  public void setLayoutMode(MapButtonsController.LayoutMode layoutMode)
  {
    mLayoutMode.setValue(layoutMode);
  }

  public MutableLiveData<Integer> getMyPositionMode()
  {
    return mMyPositionMode;
  }

  public void setMyPositionMode(int mode)
  {
    mMyPositionMode.setValue(mode);
  }

  public MutableLiveData<SearchWheel.SearchOption> getSearchOption()
  {
    return mSearchOption;
  }

  public void setSearchOption(@Nullable SearchWheel.SearchOption searchOption)
  {
    mSearchOption.setValue(searchOption);
  }

  public MutableLiveData<String> getActiveSearchQuery()
  {
    return mActiveSearchQuery;
  }

  public void setActiveSearchQuery(@Nullable String query)
  {
    mActiveSearchQuery.setValue(query);
  }

  public MutableLiveData<String> getHomeEta()
  {
    return mHomeEta;
  }

  public void setHomeEta(@Nullable String eta)
  {
    mHomeEta.setValue(eta);
  }

  public MutableLiveData<RoutePreviewControlsState> getRoutePreviewControlsState()
  {
    return mRoutePreviewControlsState;
  }

  public void setRoutePreviewControlsState(RoutePreviewControlsState state)
  {
    mRoutePreviewControlsState.setValue(state);
  }

  public record
      RoutePreviewControlsState(boolean isVisible, String positionText, boolean allowPrevious, boolean allowNext)
  {
    public static RoutePreviewControlsState none()
    {
      return new RoutePreviewControlsState(false, "", false, false);
    }
  }
}
