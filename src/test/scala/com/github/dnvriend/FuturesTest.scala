package com.github.dnvriend

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, FlatSpec}
import Matchers._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class FuturesTest extends FlatSpec with Matchers with ScalaFutures {

  /**
   *  Futures provide a nice way to reason about performing many operations in parallel– in an efficient and non-blocking way.
   *  The idea is simple, a Future is a sort of a placeholder object that you can create for a result that does not yet exist.
   *  Generally, the result of the Future is computed concurrently and can be later collected.
   *
   *  Composing concurrent tasks in this way tends to result in faster, asynchronous, non-blocking parallel code.
   *
   *  By default, futures and promises are non-blocking, making use of callbacks instead of typical blocking operations.
   *
   *  To simplify the use of callbacks both syntactically and conceptually, Scala provides combinators such as flatMap,
   *  foreach, and filter used to compose futures in a non-blocking way. Blocking is still possible - for cases where it
   *  is absolutely necessary, futures can be blocked on (although this is discouraged).
   */

  /**
   * To use a future, we need to import an ExecutionContext, that provides the thread that will execute the Future.
   * Scala provides one out of the box, just add the following two imports to your code:
   */

  import scala.concurrent._
  import ExecutionContext.Implicits.global

  /**
   * A Future is an object holding a value which may become available at some point. This value is usually the result of
   * some other computation:
   *
   * If the computation has not yet completed, we say that the Future is not completed.
   * If the computation has completed with a value or with an exception, we say that the Future is completed.
   *
   * Completion can take one of two forms:
   *
   * 1. When a Future is completed with a value, we say that the future was successfully completed with that value.
   * 2. When a Future is completed with an exception thrown by the computation, we say that the Future was failed with that exception.
   */

  /**
   * A Future has an important property that it may only be assigned once. Once a Future object is given a value or an
   * exception, it becomes in effect immutable– it can never be overwritten.
   *
   * The simplest way to create a future object is to use the Future companion object and use the apply method and pass
   * a body to the Future, which starts an asynchronous computation:
   *
   * def apply[T](body: =>T)(implicit executor: ExecutionContext): Future[T]
   */

  "FutureConcept" should "complete with value 2, sometime in the future" in {
    val future: Future[Int] = Future { 1 }.map(_*2)
    future.isReadyWithin(10 millis) shouldBe true
    future.futureValue shouldBe 2
  }

  /**
   * We can block the current thread and wait for the future to complete to
   * get the result:
   */

  "Future waiting" should "block the thread until the result is available" in {
    val future: Future[Int] = Future { 1 }.map(_*2)
    val result: Int = Await.result(future, 10 millis)
    result shouldBe 2
  }

  /**
   * A future is the asynchronous version of Try[T], so the result type of a future is Success[T] or Failure[T], where
   * the failure contains the throwable. To get to the result of the future, we can block our thread and wait
   * for the result like so:
   */

  "Future callback" should "block the thread, until the thread is ready and test using callback" in {
    val future: Future[Int] = Future { 1 }.map(_*2)
    future.onComplete {
      case Success(n) => n shouldBe 2
      case Failure(t) => fail(t)
    }
    Await.ready(future, 10 millis)
    // block the test thread to let the future evaluate
    Thread.sleep((10 millis).toMillis)
  }

  /**
   * The computation of a future can fail:
   */

  "Failed future" should "complete with throwable" in {
    val future: Future[Int] = Future { 2/ 0 }
    future.onComplete {
      case Failure(t: ArithmeticException) =>
      case Failure(t) => fail(t)
      case Success(n) => fail("Should not complete successfully")
    }
    Await.ready(future, 10 millis)
    Thread.sleep((10 millis).toMillis)
  }

  "Failed future" should "recover with a default value" in {
    val future: Future[Int] = Future { 2/ 0 } recover { case t: Throwable => 0 }
    future.onComplete {
      case Success(n) => n shouldBe 0
      case Failure(t) => fail(t)
    }
    Await.ready(future, 10 millis)
    Thread.sleep((10 millis).toMillis)
  }

  "Future composition" should "map the result of the future with a new calculation" in {
    val future: Future[Int] = Future { 2 }.map(_*2) recover { case t: Throwable => 0 }
    future.futureValue shouldBe 4
  }

  "Future composition can fail" should "should still recover with default value" in {
    val future: Future[Int] = Future { 2 }.map(_ / 0) recover { case t: Throwable => 0 }
    future.futureValue shouldBe 0
  }

  "Multiple futures" should "be able to be composed" in {
    val f1: Future[Int] = Future { 2 }
    val f2: Future[Int] = Future { 4 }

    // to return a Future[Int], we must destroy (flatten) the container 'f1', so
    // we have the value 2, make the calculation and return a Future
    val f3: Future[Int] = f1 flatMap { n => f2.map { _ * n } }
    f3.futureValue shouldBe 8
  }

  "Future for comprehension" should "result in the value 8" in {
    val f1: Future[Int] = Future { 2 }
    val f2: Future[Int] = Future { 4 }

    val f3: Future[Int] = for {
      n1 <- f1 // take the value n1 out of container f1
      n2 <- f2 // take the value n2 out of container f2
    } yield {
      n1 * n2
    } // do the processing and return container f3

    f3.futureValue shouldBe 8
  }

  "Future with side effects" should "leave the result alone" in {
    val f1 = Future { 1 }
      .andThen { case Success(n: Int) => n * 2 }
      .andThen { case Success(n: Int) => n * 2 }
      .andThen { case Success(n: Int) => n * 2 }

    f1.futureValue shouldBe 1
  }
}
