#!/bin/sh
# print-stdout <application_id>

yarn logs -applicationId $1 --log_files stdout
