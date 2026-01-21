package com.devhunter9x.firstapp.exception

// User exceptions
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)

// Room exceptions
class RoomAlreadyExistsException(message: String) : RuntimeException(message)
class RoomNotFoundException(message: String) : RuntimeException(message)

// Auth exceptions
class InvalidCredentialsException(message: String) : RuntimeException(message)