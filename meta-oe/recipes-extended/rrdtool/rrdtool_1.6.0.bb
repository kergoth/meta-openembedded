SUMMARY = "High performance data logging and graphing system for time series data"
HOMEPAGE = "http://oss.oetiker.ch/rrdtool/"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYRIGHT;md5=3349111ed0533471494beec99715bc9d"

DEPENDS = "libpng zlib cairo pango glib-2.0 libxml2 groff-native"

SRCREV = "61f116744262c4c18922dcf806e496715f199669"
PV = "1.6.0"

SRC_URI = "\
    git://github.com/oetiker/rrdtool-1.x.git;branch=1.6;protocol=http; \
"

S = "${WORKDIR}/git"

inherit cpan autotools-brokensep gettext pythonnative python-dir systemd

BBCLASSEXTEND = "native"

SYSTEMD_SERVICE_${PN} = "rrdcached.socket rrdcached.service"

EXTRA_AUTORECONF = "-I m4"

PACKAGECONFIG ??= "python perl ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', '', d)}"

PACKAGECONFIG[python] = "--enable-python=yes \
am_cv_python_pythondir=${STAGING_LIBDIR}/python${PYTHON_BASEVERSION}/site-packages \
am_cv_python_pyexecdir=${STAGING_LIBDIR}/python${PYTHON_BASEVERSION}/site-packages,\
--disable-python,python,"

PACKAGECONFIG[perl] = \
"--enable-perl=yes --with-perl-options='INSTALLDIRS="vendor"' \
ac_cv_path_PERL_CC='${CC}',  \
--disable-perl,perl,"

PACKAGECONFIG[dbi] = "--enable-libdbi,--disable-libdbi,libdbi"

PACKAGECONFIG[systemd] = "--with-systemdsystemunitdir=${systemd_unitdir}/system/,--without-systemdsystemunitdir,systemd,"

EXTRA_OECONF = " \
    --enable-shared \
    --disable-libwrap \
    --program-prefix='' \
    rd_cv_ieee_works=yes \
    --disable-ruby \
    --disable-lua \
    --disable-tcl \
    --disable-rpath \
"

export STAGING_LIBDIR
export STAGING_INCDIR

# emulate cpan_do_configure
EXTRA_OEMAKE = ' PERL5LIB="${PERL_ARCHLIB}" '
# Avoid do_configure error on some hosts

do_configure() {
    unset PERLHOSTLIB
    #fix the pkglib problem with newer automake
    #perl
    sed -i -e "s|-Wl,--rpath -Wl,\$rp||g" \
        ${S}/bindings/perl-shared/Makefile.PL

    #python
    sed -i -e '/PYTHON_INCLUDES="-I${/c \
    PYTHON_INCLUDES="-I=/usr/include/python${PYTHON_BASEVERSION}"' \
        ${S}/m4/acinclude.m4
    #remove the useless RPATH from the rrdtool.so
    sed -i -e 's|LD_RUN_PATH=$(libdir)||g' ${S}/bindings/Makefile.am

    autotools_do_configure

    #modify python sitepkg
    #remove the dependency of perl-shared:Makefile
    #or perl-shared/Makefile will be regenerated
    #if any code touch bindings/Makefile after below perl bindings code
    sed -i -e "s:python/setup.py install:python/setup.py install \
        --install-lib=${PYTHON_SITEPACKAGES_DIR}:" \
        -e "s:perl-shared/Makefile.PL Makefile:perl-shared/Makefile.PL:" \
        ${B}/bindings/Makefile

    #change the interpreter in file
    sed -i -e "s|^PERL = ${STAGING_BINDIR_NATIVE}/.*|PERL = /usr/bin/perl|g" \
        ${B}/examples/Makefile
    sed -i -e "s|${STAGING_BINDIR_NATIVE}/perl-native/perl|/usr/bin/perl|g" \
        ${B}/examples/*.pl

    sed -i -e '/SUBDIRS/s/bindings//' "${B}/Makefile"
}

do_compile () {
    oe_runmake

    cd ${B}/bindings
    oe_runmake python

    # MAKE is set to true to ensure that the Makefile is generated from
    # Makefile.PL, but not actually built, so we can apply the cpan.bbclass
    # Makefile fixups first
    oe_runmake MAKE=true perl-shared perl-piped
    if [ "${BUILD_SYS}" != "${HOST_SYS}" ]; then
        find ${S}/bindings -name Makefile.PL | while read f; do
            f="${B}/bindings/${f#${S}/bindings/}"
            f="${f%.PL}"
            if [ ! -e "$f" ]; then
                bbfatal "$f does not exist"
            fi
            sed -i -e "s:\(PERL_ARCHLIB = \).*:\1${PERL_ARCHLIB}:" \
                   -e 's/perl.real/perl/' \
                   -e "s|^\(CCFLAGS =.*\)|\1 ${CFLAGS}|" \
                $f
        done
    fi
    oe_runmake -C ${B}/bindings/perl-shared
    oe_runmake -C ${B}/bindings/perl-piped
}

PACKAGES =+ "${PN}-perl ${PN}-python"

FILES_${PN}-doc += "${datadir}/rrdtool/examples"

DESCRIPTION_${PN}-perl = \
"The ${PN}-perl package includes RRDtool bindings for perl."
FILES_${PN}-perl = "${libdir}/perl/vendor_perl/*/*.pm \
    ${libdir}/perl/vendor_perl/*/auto/RRDs/RRDs.*"
RDEPENDS_${PN}-perl = "perl perl-module-lib perl-module-getopt-long perl-module-time-hires \
    perl-module-io-file perl-module-ipc-open2 perl-module-io-socket"

DESCRIPTION_${PN}-python = \
"The ${PN}-python package includes RRDtool bindings for python."
FILES_${PN}-python = "${libdir}/python${PYTHON_BASEVERSION}/site-packages/*"
RDEPENDS_${PN}-python = "python"

FILES_${PN}-dbg += "${libdir}/perl/vendor_perl/*/auto/RRDs/.debug \
    ${libdir}/python${PYTHON_BASEVERSION}/site-packages/.debug"
