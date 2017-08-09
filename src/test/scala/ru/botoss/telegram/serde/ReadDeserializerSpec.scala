package ru.botoss.telegram.serde

import ru.botoss.telegram.UnitSpec

class ReadDeserializerSpec extends UnitSpec {
  private implicit val read = mock[Read[Object]]
  private val deserializer = new ReadDeserializer[Object]
  private val data = "test-data"
  private val deserializedData = new Object
  private val encoding = "UTF-8"

  "ReadDeserializer" should "deserialize valid data" in {
    read.read _ expects data returning deserializedData
    deserializer.deserialize(null, data.getBytes(encoding)) shouldBe deserializedData
  }

  it should "configure silently" in {
    deserializer.configure(null, isKey = false)
  }

  it should "close silently" in {
    deserializer.close()
  }
}
