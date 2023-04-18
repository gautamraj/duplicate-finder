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
    ],
)
