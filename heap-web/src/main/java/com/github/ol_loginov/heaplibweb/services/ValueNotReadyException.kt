package com.github.ol_loginov.heaplibweb.services

class ValueNotReadyException(message: String) : Exception(message) {
    constructor() : this("value not ready")
}