package ing.etl

import ing.etl.Constants.{NO, YES}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._

trait HelperFunctions {

  /**
    * Convert return 1 if both values are equal
    * @return
    */
  def transformToNumeric: String => String => Int =
    yesNo => columnValue => if (yesNo == columnValue) 1 else 0

  /**
    * User defined function for transforming value to 1 or 0
    * @param yesNo: String
    * @return
    */
  def transformYesNoToNumericUdf(yesNo: String): UserDefinedFunction =
    udf(transformToNumeric(yesNo))

  /**
    * Transform the value into 1 if value is yes
    */
  def responseToYes: UserDefinedFunction = transformYesNoToNumericUdf(YES)

  /**
    * Transform the value into 0 if value is no
    * @return
    */
  def responseToNo: UserDefinedFunction = transformYesNoToNumericUdf(NO)

  /**
    * Transform response yes/no to 1/0 and drop response column
    * @param df: DataFrame
    * @return DataFrame
    */
  def transformRsvpResponse(df: DataFrame): DataFrame = {

    df.withColumn(YES, responseToYes(col("response")))
      .withColumn(NO, responseToNo(col("response")))
      .drop("response")
  }
}

object HelperFunctions extends HelperFunctions