project(desktop)

QT6_ADD_RESOURCES(RES_SOURCES res/resources.qrc)

set(SRC
  about.cpp
  about.hpp
  bookmark_dialog.cpp
  bookmark_dialog.hpp
  build_style/build_common.cpp
  build_style/build_common.h
  build_style/build_drules.cpp
  build_style/build_drules.h
  build_style/build_phone_pack.cpp
  build_style/build_phone_pack.h
  build_style/build_skins.cpp
  build_style/build_skins.h
  build_style/build_statistics.cpp
  build_style/build_statistics.h
  build_style/build_style.cpp
  build_style/build_style.h
  build_style/run_tests.cpp
  build_style/run_tests.h
  create_feature_dialog.cpp
  create_feature_dialog.hpp
  draw_widget.cpp
  draw_widget.hpp
  editor_dialog.cpp
  editor_dialog.hpp
  html_processor.cpp
  html_processor.hpp
  info_dialog.cpp
  info_dialog.hpp
  main.cpp
  mainwindow.cpp
  mainwindow.hpp
  mwms_borders_selection.cpp
  mwms_borders_selection.hpp
  osm_auth_dialog.cpp
  osm_auth_dialog.hpp
  place_page_dialog_common.cpp
  place_page_dialog_common.hpp
  place_page_dialog_developer.cpp
  place_page_dialog_developer.hpp
  place_page_dialog_user.cpp
  place_page_dialog_user.hpp
  preferences_dialog.cpp
  preferences_dialog.hpp
  popup_menu_holder.cpp
  popup_menu_holder.hpp
  routing_settings_dialog.cpp
  routing_settings_dialog.hpp
  routing_turns_visualizer.cpp
  routing_turns_visualizer.hpp
  ruler.cpp
  ruler.hpp
  screenshoter.cpp
  screenshoter.hpp
  search_panel.cpp
  search_panel.hpp
  selection.hpp
  star_rating_widget.cpp
  star_rating_widget.hpp
  update_dialog.cpp
  update_dialog.hpp
)

omim_add_executable(${PROJECT_NAME} ${RES_SOURCES} ${SRC})

set_target_properties(${PROJECT_NAME} PROPERTIES AUTOMOC ON)

target_link_libraries(${PROJECT_NAME}
  PUBLIC
    base
    drape_frontend
    geometry
    indexer
    kml
    location_service
    map
    platform
    qt_common
    routing
    search
    storage
  PRIVATE
    coding
    editor
    generator  # For borders::LoadBorders
    i18n
)

target_link_libraries(${PROJECT_NAME}
  PRIVATE
    gflags::gflags
)

if (BUILD_DESIGNER)
  target_link_libraries(${PROJECT_NAME} PRIVATE generator)
  set(BUNDLE_NAME "CoMaps-Designer")
  set(BUNDLE_DISPLAY_NAME "CoMaps Designer")
else()
  set(BUNDLE_NAME "CoMaps")
  set(BUNDLE_DISPLAY_NAME "CoMaps Desktop")
endif()

set_target_properties(${PROJECT_NAME} PROPERTIES OUTPUT_NAME ${BUNDLE_NAME})

set(BUNDLE_EXECUTABLE ${BUNDLE_NAME})

set(BUNDLE_FOLDER ${CMAKE_BINARY_DIR}/${BUNDLE_NAME}.app)
set(RESOURCES_FOLDER ${BUNDLE_FOLDER}/Contents/Resources)
set(DATA_DIR ${OMIM_ROOT}/data)

execute_process(
    COMMAND mkdir -p ${RESOURCES_FOLDER}
)
function(copy_resources)
  foreach(file ${ARGN})
    execute_process(
      COMMAND cp -r ${DATA_DIR}/${file} ${RESOURCES_FOLDER}
    )
  endforeach()
endfunction()

set(DATA_FILES
  countries-strings
  fonts
  symbols
  welcome.html
  categories.txt
  categories_brands.txt
  classificator.txt
  colors.txt
  copyright.html
  countries.txt
  countries_meta.txt
  drules_proto_walking_light.bin
  drules_proto_walking_outdoor_light.bin
  drules_proto_walking_dark.bin
  drules_proto_walking_outdoor_dark.bin
  drules_proto_cycling_light.bin
  drules_proto_cycling_outdoor_light.bin
  drules_proto_cycling_dark.bin
  drules_proto_cycling_outdoor_dark.bin
  drules_proto_driving_light.bin
  drules_proto_driving_outdoor_light.bin
  drules_proto_driving_dark.bin
  drules_proto_driving_outdoor_dark.bin
  drules_proto_public-transport_light.bin
  drules_proto_public-transport_outdoor_light.bin
  drules_proto_public-transport_dark.bin
  drules_proto_public-transport_outdoor_dark.bin
  drules_proto_vehicle_light.bin
  drules_proto_vehicle_dark.bin
  editor.config
  packed_polygons.bin
  patterns.txt
  subtypes.csv
  transit_colors.txt
  types.txt
  mapcss-mapping.csv
  World.mwm
  WorldCoasts.mwm
)

copy_resources(${DATA_FILES})

include(GNUInstallDirs)
install(TARGETS ${PROJECT_NAME} DESTINATION ${CMAKE_INSTALL_BINDIR})

function(install_resources)
  foreach(file ${ARGN})
    if(EXISTS ${DATA_DIR}/${file})
      if(IS_DIRECTORY ${DATA_DIR}/${file})
        install(DIRECTORY ${DATA_DIR}/${file} DESTINATION ${CMAKE_INSTALL_DATADIR}/comaps/data)
      else()
        install(FILES ${DATA_DIR}/${file} DESTINATION ${CMAKE_INSTALL_DATADIR}/comaps/data)
      endif()
    endif()
  endforeach()
endfunction()

install_resources(${DATA_FILES})

if (PLATFORM_LINUX)
  install(FILES ${OMIM_ROOT}/packaging/app.comaps.comaps.metainfo.xml DESTINATION ${CMAKE_INSTALL_DATADIR}/metainfo/)
  install(FILES ${OMIM_ROOT}/qt/res/linux/app.comaps.comaps.desktop DESTINATION ${CMAKE_INSTALL_DATADIR}/applications/)
  if (BUILD_DESIGNER)
    install(FILES ${OMIM_ROOT}/qt/res/designer.png DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/512x512/apps/ RENAME comaps.png)
    install(FILES ${OMIM_ROOT}/qt/res/designer-scalable.svg DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/scalable/apps/ RENAME comaps.svg)
  else()
    install(FILES ${OMIM_ROOT}/qt/res/logo.png DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/512x512/apps/ RENAME comaps.png)
    install(FILES ${OMIM_ROOT}/qt/res/logo-scalable.svg DESTINATION ${CMAKE_INSTALL_DATADIR}/icons/hicolor/scalable/apps/ RENAME comaps.svg)
  endif()
endif()

if (NOT PLATFORM_LINUX)
  # On Linux, ICU data is loaded from the shared library.
  copy_resources(icudt75l.dat)
endif()

if (PLATFORM_WIN)
  if (BUILD_DESIGNER)
    target_sources(${PROJECT_NAME} PRIVATE res/windows/designer.rc)
  else()
    target_sources(${PROJECT_NAME} PRIVATE res/windows/windows.rc)
  endif()
endif()

if (BUILD_DESIGNER)
  execute_process(
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/mdpi/light/ ${OMIM_ROOT}/data/symbols/mdpi/design/
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/hdpi/light/ ${OMIM_ROOT}/data/symbols/hdpi/design/
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/xhdpi/light/ ${OMIM_ROOT}/data/symbols/xhdpi/design/
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/xxhdpi/light/ ${OMIM_ROOT}/data/symbols/xxhdpi/design/
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/6plus/light/ ${OMIM_ROOT}/data/symbols/6plus/design/
    COMMAND cp -rf ${OMIM_ROOT}/data/symbols/xxxhdpi/light/ ${OMIM_ROOT}/data/symbols/xxxhdpi/design/
    COMMAND cp -f ${OMIM_ROOT}/data/drules_proto_walking_light.bin ${OMIM_ROOT}/data/drules_proto_walking_design.bin
    COMMAND cp -f ${OMIM_ROOT}/data/colors.txt ${OMIM_ROOT}/data/colors_design.txt
    COMMAND cp -f ${OMIM_ROOT}/data/patterns.txt ${OMIM_ROOT}/data/patterns_design.txt
  )

  copy_resources(
    colors_design.txt
    drules_proto_walking_design.bin
    mapcss-dynamic.txt
    mapcss-mapping.csv
    patterns_design.txt
    symbols/mdpi/design
    symbols/hdpi/design
    symbols/xhdpi/design
    symbols/xxhdpi/design
    symbols/xxxhdpi/design
    symbols/6plus/design
  )

  execute_process(
    COMMAND cp ${OMIM_ROOT}/tools/python/recalculate_geom_index.py ${RESOURCES_FOLDER}
    COMMAND cp ${OMIM_ROOT}/tools/python/generate_styles_override.py ${RESOURCES_FOLDER}
    COMMAND cp -rf ${OMIM_ROOT}/tools/kothic ${RESOURCES_FOLDER}/kothic/
    COMMAND cp -f ${OMIM_ROOT}/tools/python/stylesheet/drules_info.py ${RESOURCES_FOLDER}/kothic/src/
    COMMAND cp -rf ${OMIM_ROOT}/tools/python/stylesheet/ ${RESOURCES_FOLDER}/kothic/src/
    COMMAND cp -f ${OMIM_ROOT}/3party/protobuf/protobuf-3.3.0-py2.7.egg ${RESOURCES_FOLDER}/kothic/
  )

endif()

add_subdirectory(qt_common)
