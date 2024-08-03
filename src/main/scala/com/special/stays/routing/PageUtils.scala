package com.special.stays.routing

import scalatags.Text.TypedTag
import scalatags.Text.all._

object PageUtils {

  def mkHead(title: String): TypedTag[String] = head(
    tag("title")(title),
    link(
      href := "https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.css",
      rel  := "stylesheet"
    ),
    link(
      href := "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css",
      rel  := "stylesheet"
    )
  )

}
