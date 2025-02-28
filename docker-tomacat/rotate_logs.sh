#!/bin/bash

LOG_DIR="/usr/local/tomcat/logs"
CURRENT_HOUR=$(date +%H)

# Function to rotate logs
rotate_log() {
    local log_file=$1
    local log_base=$2

    # Move the current log file to the rotated file with the current hour
    mv "${LOG_DIR}/${log_file}" "${LOG_DIR}/${log_base}_${CURRENT_HOUR}.log"
    touch "${LOG_DIR}/${log_file}"

    # Delete logs older than the past hour
    for rotated_log in "${LOG_DIR}/${log_base}"_*.log; do
        if [[ -f "$rotated_log" ]]; then
            rotated_hour=$(basename "$rotated_log" | cut -d'_' -f2 | cut -d'.' -f1)
            if [[ "$rotated_hour" != "$CURRENT_HOUR" && "$rotated_hour" != "$((CURRENT_HOUR - 1))" ]]; then
                rm -f "$rotated_log"
            fi
        fi
    done
}

# Rotate catalina logs
rotate_log "catalina.$(date +%Y-%m-%d).log" "catalina.$(date +%Y-%m-%d)"

# Rotate localhost_access logs
rotate_log "localhost_access_log.$(date +%Y-%m-%d).txt" "localhost_access_log.$(date +%Y-%m-%d)"
