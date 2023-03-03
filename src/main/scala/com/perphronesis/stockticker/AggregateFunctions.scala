package com.perphronesis.stockticker

/**
 * This class is intended to provide implicit aggregate functions to Numeric lists.  Right now only avg is implemented
 * but this could be extended to include other aggregate functions.
 *
 * @param data
 * @tparam A
 */
class AggregateFunctions[A](data: Iterable[A]) {

  def avg(implicit num: Fractional[A]): A = {
    val (sum, count) = data.foldLeft((num.zero, num.zero)) {
      case ((sum, count), x) => (num.plus(sum, x), num.plus(count, num.one))
    }
    num.div(sum, count)
  }

}
