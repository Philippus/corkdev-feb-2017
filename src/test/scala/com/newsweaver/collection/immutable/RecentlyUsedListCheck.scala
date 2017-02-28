package com.newsweaver.collection.immutable

import com.newsweaver.collection.immutable.generators.Ints._
import com.newsweaver.collection.immutable.generators.Strings._
import org.scalacheck.Gen.choose
import org.scalacheck.Prop.throws
import org.scalatest.FunSpec
import org.scalatest.prop.PropertyChecks

final class RecentlyUsedListCheck
    extends FunSpec
        with PropertyChecks {

  describe("A new Recently Used List") {
    it("must have a positive capacity") {
      forAll(nonpositiveInt) { capacity =>
        throws(classOf[IllegalArgumentException]) { RecentlyUsedList(capacity) }
      }
    }

    it("is empty") {
      forAll(positiveInt) { capacity =>
        RecentlyUsedList(capacity).isEmpty
      }
    }
  }

  describe("An empty Recently Used List") {
    it("retains a single addition") {
      forAll(positiveInt, string) { (capacity, item) =>

        val rul = RecentlyUsedList(capacity) + item
        rul.size == 1 && rul(0) == item
      }
    }


    it("retains unique additions in stack order, up to its capacity") {
      forAll(positiveInt, setOfStrings) { (capacity, strings) =>
        val uniqueItems = strings.toList
        val rul = buildRecentlyUsedList(capacity, uniqueItems)

        rul.toList == uniqueItems.reverse.take(capacity)
      }
    }
  }

  describe("An nonempty Recently Used List") {
    it("is unchanged when head item is re-added") {
      forAll(positiveInt, nonEmptySetOfStrings) { (capacity, strings) =>
        val rul = buildRecentlyUsedList(capacity, strings)

        val head = rul(0)
        rul + head == rul
      }
    }
  }

  describe("Any Recently Used List") {

    it("of at least two items moves a non-head item to head when that item is re-added") {
      forAll(positiveInt, setOfAtLeastTwoStrings) { (n, strings) =>
        val capacity = n + 1
        val initialRul = buildRecentlyUsedList(capacity, strings)
        val size = initialRul.size

        forAll(choose(1, size - 1)) { i =>
          val nonHeadItem = initialRul(i)
          val updatedRul = initialRul + nonHeadItem

          updatedRul.toList == nonHeadItem :: (initialRul.toList diff List(nonHeadItem))
        }
      }
    }

    it("that gets cleared yields an empty list of the same capacity") {
      forAll(positiveInt, nonEmptySetOfStrings) { (capacity, strings) =>
        val rul = buildRecentlyUsedList(capacity, strings)

        val clearedRul = rul.clear

        clearedRul.isEmpty &&
            (clearedRul.capacity == rul.capacity)
      }
    }


    it("rejects the addition of a null item") {
      forAll(positiveInt, setOfStrings) { (capacity, strings) =>
        val rul = buildRecentlyUsedList(capacity, strings)

        throws(classOf[IllegalArgumentException]) { rul + null }
      }
    }


    it("allows indexing only within its bounds") {
      forAll(positiveInt, setOfStrings, int) { (capacity, strings, i) =>
        val rul = buildRecentlyUsedList(capacity, strings)
        val size = rul.size

        if (0 <= i && i < size) {
          rul.isDefinedAt(i) &&
              rul(i) == rul.toList(i)
        } else {
          !rul.isDefinedAt(i) &&
              throws(classOf[IndexOutOfBoundsException]) { rul(i) }
        }
      }
    }
  }

  private def buildRecentlyUsedList[A](capacity: Int, as: Iterable[A]) =
    as.foldLeft(RecentlyUsedList[A](capacity))(_ + _)

}
