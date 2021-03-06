DESCRIPTION = "set Linux HDLC packet radio modem driver port information"
HOMEPAGE = "https://www.kernel.org/pub/linux/utils/net/hdlc"
LICENSE = "GPLv2 & GPLv2+ "
LIC_FILES_CHKSUM = "file://Makefile;md5=19aada31930b2be84bf7138420d77263"


SRC_URI = "https://www.kernel.org/pub/linux/utils/net/hdlc/${BPN}-1.18.tar.gz \
"
SRC_URI[md5sum] = "9016878156a5eadb06c0bae71cc5c9ab"
SRC_URI[sha256sum] = "21b1e2e1cb0e288b0ec8fcfd9fed449914e0f8e6fc273706bd5b3d4f6ab6b04e"


S = "${WORKDIR}/${BPN}-1.18"

DEPENDS="virtual/kernel"

EXTRA_OEMAKE="CROSS_COMPILE=${TARGET_PREFIX} CC='${CC} ${LDFLAGS}' \
              KERNEL_DIR=${STAGING_KERNEL_DIR} "

do_compile_prepend () {
    oe_runmake clean
}


do_install() {
    install -d ${D}/${bindir}
    install sethdlc ${D}/${bindir}/
}

FILES_${PN} += "${bindir}/sethdlc"
