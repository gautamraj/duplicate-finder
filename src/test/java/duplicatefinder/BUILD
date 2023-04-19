load("//bzl:test.bzl", "junit4_test_suite")

junit4_test_suite(
    name = "AllTests",
    size = "small",
    srcs = glob([
        "*Test.java",
        "*ITCase.java",
    ]),
    deps = [
        "//src/main/java/duplicatefinder",
        "@maven//:com_google_jimfs_jimfs",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_assertj_assertj_core",
    ],
)
