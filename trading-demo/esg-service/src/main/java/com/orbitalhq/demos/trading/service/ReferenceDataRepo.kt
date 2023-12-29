package com.orbitalhq.demos.trading.service

import com.lunarbank.demo.Isin
import com.lunarbank.demo.TraderId
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class ReferenceDataRepo(private val jdbcTemplate: JdbcTemplate) {
   fun allIsins(): List<Isin> {
      return jdbcTemplate.query("select id from instruments") { rs, i->
         rs.getString("id")
      }.toList()
   }

   fun allTraders(): List<TraderId> {
      return jdbcTemplate.query("select id from traders") { rs, i->
         rs.getInt("id")
      }.toList()

   }
}
