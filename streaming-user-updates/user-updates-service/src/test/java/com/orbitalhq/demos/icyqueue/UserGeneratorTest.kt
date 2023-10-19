package com.orbitalhq.demos.icyqueue

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class UserGeneratorTest : DescribeSpec({
describe("loading users") {
   it("should load users") {
      UserGenerator().users.shouldHaveSize(250)
   }
}

})
