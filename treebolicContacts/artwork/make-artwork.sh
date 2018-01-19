#!/bin/bash

source "../../../make-artwork-lib.sh"

launch="ic_launcher.svg ic_launcher_round.svg"
web="ic_launcher.svg"

icon="ic_treebolic.svg ic_contact.svg ic_group.svg"
icon_large="ic_contact_large.svg ic_group_large.svg"
splash="ic_splash.svg"
photo="ic_contact_default.svg ic_group_default.svg"

make_mipmap "${launch}" 48
make_app "${web}" 512

make_res "${icon}" 48
make_res "${icon_large}" 256
make_res "${splash}" 256
make_res "${photo}" 256

