package com.rarecase.model

/**
 * Denotes where the Pids came from.
 */
enum class PidType {

    /**
     * Pid of song that was saved offline by user
     */
    Offline,

    /**
     * PidType for songs that were last shared to Spring
     */
    Shared,

    /**
     * PidType for songs for which download after they were shared with Spring
     */
    Downloading
}