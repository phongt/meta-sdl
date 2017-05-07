DESCRIPTION = "Bluez Tools"
HOMEPAGE = "http://code.google.com/p/bluez-tools/"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=12f884d2ae1ff87c09e5b7ccc2c4ca7e"

RDEPENDS_${PN} = "bluez5 obexd"
DEPENDS = "glib-2.0 dbus-glib"

PR = "r3+gitr${SRCPV}"

SRCREV = "97efd293491ad7ec96a655665339908f2478b3d1"

S = "${WORKDIR}/git"

SRC_URI = "git://github.com/khvzak/bluez-tools.git;protocol=git"

inherit autotools pkgconfig

EXTRA_AUTORECONF_append = " -I ${STAGING_DATADIR}/aclocal"
