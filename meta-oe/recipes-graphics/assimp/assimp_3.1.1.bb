DESCRIPTION = "Open Asset Import Library is a portable Open Source library to import \
               various well-known 3D model formats in a uniform manner."
HOMEPAGE = "http://www.assimp.org/"
SECTION = "devel"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bc4231a2268da8fc55525ad119638a87"

DEPENDS = "zlib"

SRC_URI = "http://sourceforge.net/projects/${BPN}/files/${BPN}-3.1/${BPN}-${PV}_no_test_models.zip"
SRC_URI[md5sum] = "ccd4788204509da58a3a53c7aeda7a8b"
SRC_URI[sha256sum] = "da9827876f10a8b447270368753392cfd502e70a2e9d1361554e5dfcb1fede9e"

inherit cmake

EXTRA_OECMAKE += "\
    '-DASSIMP_LIB_INSTALL_DIR=${libdir}' \
    '-DASSIMP_INCLUDE_INSTALL_DIR=${includedir}' \
    '-DASSIMP_BIN_INSTALL_DIR=${bindir}' \
"

PACKAGECONFIG ?= "tools"
PACKAGECONFIG[boost] = "-DASSIMP_ENABLE_BOOST_WORKAROUND=OFF,-DASSIMP_ENABLE_BOOST_WORKAROUND=ON,boost"
PACKAGECONFIG[samples] = "-DASSIMP_BUILD_SAMPLES=ON,-DASSIMP_BUILD_SAMPLES=OFF,freeglut"
PACKAGECONFIG[static] = "-DASSIMP_BUILD_STATIC_LIB=ON,-DASSIMP_BUILD_STATIC_LIB=OFF,"
PACKAGECONFIG[tools] = "-DASSIMP_BUILD_ASSIMP_TOOLS=ON,-DASSIMP_BUILD_ASSIMP_TOOLS=OFF,"

PACKAGE_BEFORE_PN += "${PN}-samples"
FILES_${PN}-samples = "${bindir}/assimp_*"
FILES_${PN}-dev += "${libdir}/cmake"
