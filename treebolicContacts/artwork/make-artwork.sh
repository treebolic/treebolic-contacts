#!/bin/bash

#
# Copyright (c) 2026. Bernard Bou
#

source "lib-artwork.sh"

launch="ic_launcher.svg ic_launcher_round.svg"
app="ic_launcher.svg"
logo="logo_app.svg"

icon="ic_treebolic.svg ic_contact.svg ic_group.svg"
icon_large="ic_contact_large.svg ic_group_large.svg"
splash="ic_splash.svg"
photo="ic_contact_default.svg ic_group_default.svg"
toolbar="ic_filter.svg"

make_mipmap "${launch}" 48
make_app "${app}" 512
make_res "${logo}" 48

make_res "${icon}" 48
make_res "${icon_large}" 256
make_res "${splash}" 192
make_res "${photo}" 256
make_res "${toolbar}" 24

check_dir "${dirres}"
