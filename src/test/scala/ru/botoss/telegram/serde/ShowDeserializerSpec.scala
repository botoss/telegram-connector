package ru.botoss.telegram.serde

import ru.botoss.telegram.UnitSpec

import scalaz.Show

class ShowDeserializerSpec extends UnitSpec {
  private implicit val show = mock[Show[Object]]
  private val serializer = new ShowSerializer[Object]
  private val data = mock[Object]
  private val serializedData = "test-data"

  "ShowSerializer" should "deserialize valid data" in {
    show.shows _ expects data returning serializedData
    serializer.serialize(null, data) shouldBe serializedData.getBytes
  }

  it should "configure silently" in {
    serializer.configure(null, isKey = false)
  }

  it should "close silently" in {
    serializer.close()
  }
}
