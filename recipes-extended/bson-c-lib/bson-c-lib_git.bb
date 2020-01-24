SUMMARY = "Library for converting to and from BSON"
HOMEPAGE = "https://github.com/smartdevicelink/bson_c_lib"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ba5f5f5d25c7ac21f27e90890d6a70f9 \
                    file://src/emhashmap/LICENSE;md5=38aa84309f9da2ed93c60836a8456033"

SRC_URI = "git://github.com/smartdevicelink/bson_c_lib.git;branch=master"

SRC_URI += " \
    file://0001-standard-pkgconfig-location.patch \
"


PV = "1.2.0+git${SRCPV}"
SRCREV = "5e79ef239b88246504ca8efa017479bf417c5164"

S = "${WORKDIR}/git"

inherit pkgconfig autotools