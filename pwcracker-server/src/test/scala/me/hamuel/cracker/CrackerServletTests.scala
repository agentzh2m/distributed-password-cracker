package me.hamuel.cracker

import org.scalatra.test.scalatest._

class CrackerServletTests extends ScalatraFunSuite {

  addServlet(classOf[CrackerServlet], "/*")

  test("GET / on CrackerServlet should return status 200"){
    get("/"){
      status should equal (200)
    }
  }

}
