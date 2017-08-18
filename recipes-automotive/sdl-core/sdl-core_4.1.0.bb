SUMMARY = "SmartDeviceLink In-Vehicle Software"
DESCRIPTION = "SmartDeviceLink (SDL) is a standard set of protocols and messages \
    that connect applications on a smartphone to a vehicle head unit."
HOMEPAGE = "https://www.smartdevicelink.com"
LICENSE = "BSD-3-Clause"

LIC_FILES_CHKSUM = "file://LICENSE;md5=37fc754617a4cd43d221b3042dc1be11 \
                    file://src/3rd_party/dbus-1.7.8/COPYING;md5=10dded3b58148f3f1fd804b26354af3e \
                    file://src/3rd_party/expat-2.1.0/COPYING;md5=1b71f681713d1256e1c23b0890920874 \
                    file://src/3rd_party/apr-1.5.0/LICENSE;md5=4dfd4cd216828c8cae5de5a12f3844c8 \
                    file://src/3rd_party/apr-util-1.5.3/LICENSE;md5=519e0a18e03f7c023070568c14b077bb \
                    file://src/3rd_party/apr-util-1.5.3/xml/expat/COPYING;md5=7eface865f327188f814c549d44684ad \
                    file://src/3rd_party/apache-log4cxx-0.10.0/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
                    file://src/3rd_party/apache-log4cxx-0.10.0/site/license.html;md5=09f755b54444a1e59e367ec2437c913c \
                    file://src/3rd_party/apache-log4cxx-0.10.0/src/site/doxy/license_notice_footer.txt;md5=00fdad7c9ef761c3dc041b274b267018 \
                    file://src/3rd_party-static/jsoncpp/LICENSE;md5=c56ee55c03a55f8105b969d8270632ce \
                    file://src/3rd_party-static/jsoncpp/devtools/licenseupdater.py;md5=a85f07940cf61377c9a02202a0ec4480 \
                    file://src/3rd_party-static/gmock-1.7.0/LICENSE;md5=cbbd27594afd089daa160d3a16dd515a \
                    file://src/3rd_party-static/gmock-1.7.0/scripts/generator/LICENSE;md5=2c0b90db7465231447cf2dd2e8163333 \
                    file://src/3rd_party-static/gmock-1.7.0/gtest/LICENSE;md5=cbbd27594afd089daa160d3a16dd515a"

SRC_URI = "git://github.com/smartdevicelink/sdl_core.git;branch=release/4.1_LTS"

SRC_URI_append = " \
    file://0001-add-the-default-cmake-cxx-flag-for-oe.patch \
    file://0003-Use-the-default-install-prefix-of-cmake.patch \
    file://0004-disable-building-sdl-tools.patch \
    file://0005-Change-to-use-standard-libdir.patch \
    file://0006-Fix-the-warning-uninitied-value-for-navi-tts.patch \
    file://0007-Fix-make-error-with-race-condition.patch \
    file://0008-add-env-for-log4cxx-properties-path.patch \
    file://0009-Install-the-configure-file-to-sysconf-dir.patch \
    file://0011-Do-not-install-the-sample-web-HMI.patch \
    file://smartdevicelink.service \
"

PV = "4.1.0+git${SRCPV}"
SRCREV = "5043a62c8ec6bc7248c1ba5fee3f58eefc708bc4"

S = "${WORKDIR}/git"

inherit cmake pythonnative systemd

DEPENDS_append = " avahi bluez5 glib-2.0 sqlite3 log4cxx dbus openssl libusb1"
DEPENDS_append = " gstreamer1.0 gstreamer1.0-plugins-good"
DEPENDS_append = " gstreamer1.0-rtsp-server pulseaudio"

export THIRD_PARTY_INSTALL_PREFIX="${STAGING_DIR_TARGET}"
export GSTREAMER_DIR="${STAGING_LIBDIR}/gstreamer-1.0"
EXTRA_OECMAKE_append = " -DNO_REBUILD_3RD_PARTY=ON"
EXTRA_OECMAKE_append = " -DEXTENDED_MEDIA_MODE=ON"
PARALLEL_MAKE = ""

cmake_do_generate_toolchain_file_append() {
    cat >> ${WORKDIR}/toolchain.cmake <<EOF
set( CMAKE_SYSTEM_PROCESSOR ${HOST_SYS} )
EOF
}

do_install_append() {
    sed -i -e 's:AppConfigFolder =:AppConfigFolder = /etc/smartdevicelink/:g' \
        ${D}/${sysconfdir}/smartdevicelink/smartDeviceLink.ini
    sed -i -e 's:SmartDeviceLinkCore.log:/var/log/smartdevicelink/SmartDeviceLinkCore.log:g' \
        -e 's:TransportManager.log:/var/log/smartdevicelink/TransportManager.log:g' \
        -e 's:ProtocolFordHandling.log:/var/log/smartdevicelink/ProtocolFordHandling.log:g' \
        ${D}/${sysconfdir}/smartdevicelink/log4cxx.properties

    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 644 ${WORKDIR}/smartdevicelink.service ${D}${systemd_unitdir}/system/smartdevicelink.service
    fi
}

SYSTEMD_SERVICE_${PN} = "smartdevicelink.service"

RDEPENDS_${PN} += " bluez-tools"

PACKAGES = " \
    ${PN} \
    ${PN}-dev \
    ${PN}-staticdev \
    ${PN}-dbg \
"

FILES_${PN}_append = " \
    ${libdir}/lib*.so \
"

INSANE_SKIP_${PN} = "dev-so"
