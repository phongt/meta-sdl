DESCRIPTION = "Apache logging framework for C++ library 3rd party"
SECTION = "libs"
DEPENDS = "apr apr-util expat gdbm"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"
HOMEPAGE = "http://logging.apache.org/log4cxx/"

SRC_URI = "git://github.com/smartdevicelink/sdl_core.git;branch=release/4.1.0"
SRC_URI_append = " \
    file://0001-LOGCXX-413-doesn-t-compile-on-openembedded-thanks-to.patch \
    file://0002-LOGCXX-414-possibly-wrong-use-of-autotools-docdir-th.patch"

SRCREV = "2b4db8814d09c0ca668934d2e52e8102724c6ccb"

S = "${WORKDIR}/git/src/3rd_party/apache-${PN}-${PV}"

inherit autotools-brokensep pkgconfig

BBCLASSEXTEND += "native"

SECURITY_CFLAGS = "${SECURITY_NO_PIE_CFLAGS}"
