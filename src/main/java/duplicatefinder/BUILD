java_library(
    name = "duplicatefinder",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_beust_jcommander",
        "@maven//:com_google_guava_guava",
        "@maven//:org_apache_logging_log4j_log4j_api",
        "@maven//:org_apache_logging_log4j_log4j_core",
        "@maven//:org_apache_logging_log4j_log4j_slf4j_impl",
    ],
)

java_binary(
    name = "Main",
    resources = ["//src/main/resources:log4j2_xml"],
    runtime_deps = [":duplicatefinder"],
)
