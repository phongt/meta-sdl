SUMMARY = "SmartDeviceLink In-Vehicle Software"
DESCRIPTION = "SmartDeviceLink (SDL) is a standard set of protocols and messages \
    that connect applications on a smartphone to a vehicle head unit."
HOMEPAGE = "https://www.smartdevicelink.com"
LICENSE = "BSD-3-Clause"

LIC_FILES_CHKSUM = "file://LICENSE;md5=ecc58617265863ee517caa91580a143d \
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

SRC_URI = "git://github.com/smartdevicelink/sdl_core.git;branch=master"

SRC_URI += " \
    file://0001-add-the-default-cmake-cxx-flag-for-oe.patch \
    file://0002-Use-the-default-install-prefix-of-cmake.patch \
    file://0003-disable-building-sdl-tools.patch \
    file://0004-Prevent-Wmaybe-uninitialized-from-inducing-Werror-fo.patch \
    file://0005-Install-the-configure-file-to-sysconf-dir.patch \
    file://0006-Change-to-use-standard-libdir.patch \
    file://0007-Add-standard-usr-lib-path-to-rpath.patch \
    file://0008-Prepend-the-correct-sysroot-prefix-to-the-libbson-li.patch \
    file://0009-avoid-use-of-generic-start.sh-in-usr-bin.patch \
    file://0010-add-env-for-log4cxx-properties-path.patch \
    file://smartdevicelink.service \
"

PV = "6.0.1"
SRCREV = "3dcc409766b49f9914963a5c7b2ef0c0dc5fddc8"

S = "${WORKDIR}/git"

inherit cmake pythonnative systemd

# The Bluetooth support in the source code is written in terms of Bluez and Pulseaudio. So both must
# be enabled in the distribution for the option to be activated.
PACKAGECONFIG_BLUETOOTH ?= "${@bb.utils.contains('DISTRO_FEATURES', 'bluetooth pulseaudio', 'bluez5', '', d)}"

# The extended media support uses Pulseaudio
PACKAGECONFIG_PULSEAUDIO ?= "${@bb.utils.contains('DISTRO_FEATURES', 'pulseaudio', 'pulseaudio', '', d)}"

PACKAGECONFIG ??= "${PACKAGECONFIG_BLUETOOTH} ${PACKAGECONFIG_PULSEAUDIO}"

PACKAGECONFIG[bluez5] = "-DBUILD_BT_SUPPORT=ON,-DBUILD_BT_SUPPORT=OFF,bluez5 pulseaudio,pulseaudio-module-bluetooth-discover pulseaudio-module-bluetooth-policy pulseaudio-module-switch-on-connect pulseaudio-module-bluez5-discover pulseaudio-module-bluez5-device bluez-tools"

PACKAGECONFIG[pulseaudio] = "-DEXTENDED_MEDIA_MODE=ON,-DEXTENDED_MEDIA_MODE=OFF,pulseaudio,pulseaudio-module-alsa-sink"

DEPENDS += "avahi glib-2.0 sqlite3 log4cxx dbus openssl libusb1 bson-c-lib"
DEPENDS += "gstreamer1.0 gstreamer1.0-plugins-good"
DEPENDS += "gstreamer1.0-rtsp-server"
DEPENDS += "boost"

export THIRD_PARTY_INSTALL_PREFIX="${STAGING_DIR_TARGET}"
export GSTREAMER_DIR="${STAGING_LIBDIR}/gstreamer-1.0"
EXTRA_OECMAKE += "-DNO_REBUILD_3RD_PARTY=ON"
EXTRA_OECMAKE += "-DUSE_CCACHE=OFF"
EXTRA_OECMAKE += "-DCMAKE_BUILD_TYPE=RelWithDebInfo"
EXTRA_OECMAKE += "-DUSE_GOLD_LD=OFF"
#FixMe: current with thud openssl 1.1.1. some interfaces were changed disable security...
EXTRA_OECMAKE += "-DENABLE_SECURITY=OFF"

# sdl-core does not compile with ninja
OECMAKE_GENERATOR = "Unix Makefiles"

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

        if ${@bb.utils.contains('PACKAGECONFIG', 'bluez5', 'false', 'true', d)}; then
            sed -i -e '/Requires=bluetooth.service/d' \
                -e '/After=bluetooth.service/d' \
                ${D}${systemd_unitdir}/system/smartdevicelink.service
        fi
    fi
}

SYSTEMD_SERVICE_${PN} = "smartdevicelink.service"

RDEPENDS_${PN} += " bash"

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
