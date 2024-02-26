package com.special.stays.database

import cats.Show
import com.special.stays.models.SpecialDeal
import doobie._
import doobie.implicits._
import doobie.implicits.javasql.TimestampMeta
import org.postgresql.util.PGobject

import java.sql.Timestamp
import java.time.{ZoneOffset, ZonedDateTime}

object SQLQueries {

  implicit val han: LogHandler = LogHandler.nop

  implicit val metaInstance: Meta[ZonedDateTime] = Meta[Timestamp]
    .imap(ts => ZonedDateTime.ofInstant(ts.toInstant, ZoneOffset.UTC))(zdt => Timestamp.from(zdt.toInstant))

  implicit val pgObjectShow: Show[PGobject] = _.toString

  def insertSpecial(specialDeal: SpecialDeal): Update0 =
    sql"""
         | insert into special_deals (special_id, description, total_nights, discount_percentage_off, available_from, available_to, added_on)
         | values (${specialDeal.id}, ${specialDeal.description}, ${specialDeal.totalNights}, ${specialDeal.discountPercentageOff},
         | ${specialDeal.availableFrom}, ${specialDeal.availableTo}, now())""".stripMargin
      .update

}
