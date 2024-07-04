package com.special.stays.producers

import cats.effect._
import cats.syntax.all._
import com.special.stays.models.SpecialDealBooking
import fs2.kafka.vulcan._
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings, RecordSerializer, Serializer}


object BookingProducer {

  val topic = "special-stay-bookings"

  def sendMessage(key: String,
                  message: SpecialDealBooking,
                  producer: KafkaProducer[IO, String, SpecialDealBooking]
                 ): IO[Unit] =
    producer.produce(ProducerRecords.one(ProducerRecord(topic, key, message))).flatten.void

  def apply[F[_]: ConcurrentEffect: ContextShift](): fs2.Stream[F, KafkaProducer[F, String, SpecialDealBooking]] = {

    val avroSettings = AvroSettings[F](
      SchemaRegistryClientSettings[F]("http://0.0.0.0:8081")
        .withAuth(Auth.Basic("username", "password")))

    implicit val serializer: RecordSerializer[F, SpecialDealBooking] =
      AvroSerializer[SpecialDealBooking].using(avroSettings)

    val producerSettings =
      ProducerSettings[F, String, SpecialDealBooking](
        keySerializer = Serializer[F, String],
        valueSerializer = serializer
      ).withBootstrapServers("localhost:9092")

    KafkaProducer
      .stream(producerSettings)
  }

}
