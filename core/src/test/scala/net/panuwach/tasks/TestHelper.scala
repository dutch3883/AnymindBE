package net.panuwach.tasks

import org.mockito.scalatest.{AsyncMockitoSugar, MockitoSugar, ResetMocksAfterEachAsyncTest}
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}

sealed trait GeneralTestHelper extends Matchers with EitherValues
trait TestHelper               extends AnyWordSpec with GeneralTestHelper with MockitoSugar
trait TestHelperAsync
    extends AsyncWordSpec
    with GeneralTestHelper
    with AsyncMockitoSugar
    with ResetMocksAfterEachAsyncTest
