# mapper fork plan

Personal CoMaps fork. Targets: **Android** and **Linux desktop (Qt)** only. Windows code is left alone for now.
Upstream merge compatibility is not a goal — chop freely.

## Status

| # | Task | Status |
|---|------|--------|
| 1 | Strip iOS, macOS, Metal, Codeberg CI | Done on `strip-apple-codeberg` (commit f75a21f), **never compiled** |
| 2 | Build Android debug APK + Linux Qt desktop, fix breakage from step 1 | Todo |
| 3 | Remove track recording (Android + C++) and Donate | Todo |
| 4 | Top search pill + hamburger menu | Todo |
| 5 | Category chips | Todo |
| 6 | Bottom tabs | Todo |
| 7 | Home chip → route to "Home" bookmark | Todo |
| 8 | Home chip ETA on app open | Todo |
| 9 | Test on device (light/dark, search, chips, Home, navigation, place page overlap) | Todo |

Build after every step.

## 1. Apple/Codeberg strip (done, unverified)

- Deleted `iphone/`, `xcode/`, `.forgejo/`, `.github/workflows/ios-check.yaml`, `3party/CMake-MetalShaderSupport` submodule.
- Removed Objective-C (`*.mm`), `libs/drape/metal`, `libs/shaders/Metal`, iOS background downloader, Apple location service.
- Apple `#if` branches stripped with `unifdef` (OMIM_OS_IPHONE, OMIM_OS_MAC, OMIM_METAL_AVAILABLE, `__APPLE__`, ...).
- `dp::ApiVersion::Metal` removed; `Vulkan` keeps explicit value 2 (preferences store the API by name anyway).
- `tools/python/generate_desktop_ui_strings.py` now reads `android/sdk/src/main/res/values/types_strings.xml`
  (was iOS `LocalizableTypes.strings`); `configure.sh`, `generate_taginfo.py`, `categories/consistency.py` likewise.
- Mac DMG/bundle packaging removed from `qt/CMakeLists.txt`.

Likely trouble spots if the build breaks: unused-parameter warnings where Metal branches were removed
(`[[maybe_unused]]` added in `arrow3d.cpp`, `utils/projection.cpp`), and CMake blocks edited in
`libs/platform`, `libs/drape`, `libs/shaders`, `qt`, `dev_sandbox`.

## 3. Removals

**Track recording** — remove the whole feature:
- Android app: `location/TrackRecordingService` (+ manifest entry, notification channel in `MwmApplication`),
  `MwmActivity` (menu item, start/stop/save dialogs, intent handling, layout offsets),
  `maplayer/MapButtonsController` + `MapButtonsViewModel` (status button, menu badge color),
  `routing/NavigationController`, `widget/menu/NavMenu` + nav bottom layout button,
  `util/bottomsheet/MenuAdapter`, `map_status_track_recording.xml` and its includes in all layout variants
  (`layout`, `layout-land`, `layout-h400dp`, `layout-h400dp-land`), related strings/colors/drawables.
- SDK: `sdk/location/TrackRecorder.java`, its JNI `.cpp`, `LocationHelper` interval hook.
- C++: recording code in `libs/map` (framework, bookmark_manager, power_management, mwm_url) and tests.
- Existing saved tracks still show under bookmarks; no new recordings.

**Donate** — menu item in `MwmActivity`, Help/About entry, helper code, strings.

## 4–6. Google Maps-style UI (Android)

Muscle-memory clone of Google Maps layout. No voice search, no blue directions FAB.

Top:
- **Search pill** across the top. Tap → existing search (`showSearch("")`).
  While results are on the map, the pill shows the query + X (absorbs `FloatingSearchToolbarController`).
- **Hamburger** at the pill's right end (where Google's avatar is). Menu: Download maps (update badge shows on
  the hamburger), Share my location, Settings, Help. No Donate, no track recording, no Add place.
- **Chips** under the pill, horizontally scrollable, in this order:

  | Chip | Query |
  |------|-------|
  | Home | see below |
  | Gas | `category_fuel` |
  | Coffee | "Cafe" (not "coffee" — that also matches vending machines/coffee shops) |
  | Playgrounds | "Playground" |
  | Groceries | `category_food` |
  | Restaurants | `category_eat` (bars/fast food included — fine) |

  Tap runs an interactive on-map search the same way `maplayer/SearchWheel` does, and puts the query in the pill.
- **Layers** button moves to the right side under the chips.
- Keep CoMaps zoom buttons and my-location button.

Bottom: replace `map_buttons_bottom` (Help / Search / Bookmarks / Menu) with three tabs:
- **Explore** — the map
- **You** — bookmarks & tracks
- **Contribute** — Add place to OSM (moved out of the menu)

Handle status bar insets and dark mode.

Relevant files: `android/app/src/main/res/layout/map_buttons_layout_regular.xml`, `map_buttons_bottom.xml`,
`maplayer/MapButtonsController.java`, `maplayer/SearchWheel.java`, `MwmActivity.java`.

## 7–8. Home chip

- Home = bookmark named **"Home"**. Tap chip → route planning from my position to it.
  No Home bookmark → hint on how to create one.
- **ETA on the chip ("Home · 18 min") on app open only, not while navigating** (matches Google).
  Implementation: new native function that creates a *separate* `AsyncRouter` with a car `IndexRouter`
  (mirror `RoutingManager::SetRouterImpl` in `libs/map/routing_manager.cpp`), routes my position → Home,
  returns seconds via JNI. Never touch `RoutingController` / `RoutingSession`.
  Chip shows just "Home" if there's no GPS fix, no Home bookmark, or Home's map isn't downloaded.
