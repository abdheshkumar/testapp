package ing.etl

import com.github.mrpowers.spark.fast.tests.DatasetComparer
import org.scalatest.{FlatSpec, Matchers}
import org.apache.spark.sql.functions._

class HelperFunctionsSpec
    extends FlatSpec
    with Matchers
    with SparkSessionTestWrapper
    with DatasetComparer {

  import spark.implicits._
  "transformYesNoToNumeric" should "return 1 when both values are equal" in {
    val result =
      HelperFunctions.transformToNumeric(Constants.YES)(Constants.YES)
    result shouldBe 1
  }

  it should "return 0 when both values are not equal" in {
    val result =
      HelperFunctions.transformToNumeric(Constants.YES)(Constants.YES)
    result shouldBe 1
  }

  "responseToYes" should "transform column of DataFrame to 1 if it's value is yes" in {
    val df = Seq("yes").toDF("my_column")
    val transformDf = df.withColumn(
      "my_column",
      HelperFunctions.responseToYes(col("my_column"))
    )
    val expected = Seq(1).toDF("my_column")
    assertSmallDatasetEquality(transformDf, expected)
  }

  it should "transform column of DataFrame to 0 if it's value is not yes" in {
    val df = Seq("test").toDF("my_column")
    val transformDf = df.withColumn(
      "my_column",
      HelperFunctions.responseToYes(col("my_column"))
    )
    val expected = Seq(0).toDF("my_column")
    assertSmallDatasetEquality(transformDf, expected)
  }

  "responseToNo" should "transform column of DataFrame to 1 if it's value is no" in {
    val df = Seq("no").toDF("my_column")
    val transformDf =
      df.withColumn("my_column", HelperFunctions.responseToNo(col("my_column")))
    val expected = Seq(1).toDF("my_column")
    assertSmallDatasetEquality(transformDf, expected)
  }

  it should "transform column of DataFrame to 0 if it's value is not no" in {
    val df = Seq("test").toDF("my_column")
    val transformDf =
      df.withColumn("my_column", HelperFunctions.responseToNo(col("my_column")))
    val expected = Seq(0).toDF("my_column")
    assertSmallDatasetEquality(transformDf, expected)
  }

  "transformRsvpResponse" should "transfer response yes/no to 1/0" in {
    val data = List("yes", "yes", "no").toDF("response")
    val result = HelperFunctions.transformRsvpResponse(data)
    val excepted = Seq((1, 0), (1, 0), (0, 1)).toDF("yes", "no")
    excepted.printSchema()
    assertSmallDatasetEquality(result, excepted)
  }
}
