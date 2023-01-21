package com.mas2022datascience.tracabgen5filter.processor;

import com.mas2022datascience.avro.v1.Frame;
import com.mas2022datascience.avro.v1.TracabGen5TF01;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsRunnerDSL {
  @Value(value = "${topic.tracab-01.name}")
  private String topicIn;

  @Value(value = "${topic.tracab-02.name}")
  private String topicOut;

  @Value(value = "${spring.kafka.properties.schema.registry.url}") private String schemaRegistry;

  @Bean
  public KStream<String, TracabGen5TF01> kStream(StreamsBuilder kStreamBuilder) {

    // When you want to override serdes explicitly/selectively
    final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
        schemaRegistry);
    final Serde<TracabGen5TF01> tracabGen5TF01Serde = new SpecificAvroSerde<>();
    tracabGen5TF01Serde.configure(serdeConfig, false); // `false` for record values

    // the builder is used to construct the topology
    KStream<String, TracabGen5TF01> stream = kStreamBuilder.stream(topicIn,
    Consumed.with(Serdes.String(), tracabGen5TF01Serde));

    stream
        .to(topicOut);

    return stream;

  }

  /**
   * Convertes the utc string of type "yyyy-MM-dd'T'HH:mm:ss.SSS" to epoc time in milliseconds.
   * @param utcString of type String of format 'yyyy-MM-dd'T'HH:mm:ss.SSS'
   * @return epoc time in milliseconds
   */
  private static long utcString2epocMs(String utcString) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        .withZone(ZoneOffset.UTC);

    return Instant.from(fmt.parse(utcString)).toEpochMilli();
  }

  /**
   * checks if the isBallInPlay of the actualFrame is set to 0 or 1.
   * If filter set to all or anything else then it returns always true
   * @param actualFrame of type frame
   * @param isBallInPlay is of type string
   * @return of type boolean
   */
  private boolean checkIsBallInPlay(Frame actualFrame, String isBallInPlay) {
    switch (isBallInPlay) {
      case "0":
      case "1":
        if (!isBallInPlay.equals(String.valueOf(actualFrame.getIsBallInPlay()))) {
          return false;
        }
      default:
        return true;
    }
  }

}


