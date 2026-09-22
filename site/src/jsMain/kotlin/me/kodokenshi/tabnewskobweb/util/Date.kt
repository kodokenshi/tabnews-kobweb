package me.kodokenshi.tabnewskobweb.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

val LocalDateTime.Formats.PTBR
  get() =
    LocalDateTime.Format {
      day()
      char('/')
      monthNumber()
      char('/')
      year()
      char(',')
      char(' ')
      hour()
      char(':')
      minute()
      char(':')
      second()
    }
