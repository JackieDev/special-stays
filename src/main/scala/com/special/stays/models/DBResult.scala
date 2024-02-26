package com.special.stays.models

sealed trait DBResult

case object SuccessfulDBInsert extends DBResult
case object FailedDBInsert extends DBResult
