#!/bin/bash

#
# Copyright (c) 2026. Bernard Bou
#

source "lib-artwork.sh"

subdir="treebolic/provider/contacts/images"

icon="*.svg"
root="root*.svg"

make_resource "${icon}" 64 "${subdir}"
make_resource "${root}" 256 "${subdir}"

check_dir "${dirres}"

