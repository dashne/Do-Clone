  cmake_minimum_required(VERSION 3.22.1)
  project(va-native)

  find_package(shadowhook REQUIRED CONFIG)

  add_library(va-native SHARED IOUniformer_restore.cpp
                                HookTest.cpp)
  target_link_libraries(va-native log shadowhook::shadowhook)