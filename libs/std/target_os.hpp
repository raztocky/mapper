#pragma once

#if defined(ANDROID) || defined(__ANDROID__)
#define OMIM_OS_ANDROID
#define OMIM_OS_NAME "android"
#define OMIM_OS_MOBILE

#elif defined(_WIN32)
#define OMIM_OS_WINDOWS
#define OMIM_OS_NAME "win"
#define OMIM_OS_DESKTOP

#ifdef __MINGW32__
#define OMIM_OS_WINDOWS_MINGW
#else
#define OMIM_OS_WINDOWS_NATIVE
#endif

#else
#define OMIM_OS_LINUX
#define OMIM_OS_NAME "linux"
#define OMIM_OS_DESKTOP
#endif
