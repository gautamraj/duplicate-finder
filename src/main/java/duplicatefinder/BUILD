java_library(
    name = "duplicatefinder",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_guava_guava",
    ],
)

java_binary(
    name = "Main",
    runtime_deps = [":duplicatefinder"],
)
