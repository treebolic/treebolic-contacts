#!/bin/bash

source "../../../make-artwork-lib.sh"

subdir="treebolic/provider/contacts/images"

icon="*.svg"
root="root*.svg"

make_resource "${icon}" 64 "${subdir}"
make_resource "${root}" 256 "${subdir}"

