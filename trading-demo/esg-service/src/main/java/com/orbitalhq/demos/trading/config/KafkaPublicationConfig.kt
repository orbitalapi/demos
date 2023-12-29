package com.orbitalhq.demos.trading.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.publication")
class KafkaPublicationConfig(var orderPublishPeriod: Long = 15, var lastTradePublishPeriod: Long = 20)
