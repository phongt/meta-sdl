SUMMARY = "Library for converting to and from BSON"
HOMEPAGE = "https://github.com/smartdevicelink/bson_c_lib"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ba5f5f5d25c7ac21f27e90890d6a70f9 \
                    file://src/emhashmap/LICENSE;md5=38aa84309f9da2ed93c60836a8456033"

SRC_URI = "git://github.com/smartdevicelink/bson_c_lib.git"

PV = "1.1.0+git${SRCPV}"
SRCREV = "82f9e9dcb1f49811ec678a6d19d4f90da831ac0f"

S = "${WORKDIR}/git"

inherit autotools
