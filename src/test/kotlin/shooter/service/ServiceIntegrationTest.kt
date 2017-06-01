package shooter.service

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class ServiceIntegrationTest : LightPlatformCodeInsightFixtureTestCase() {

    fun testReference() {
        val project = myFixture.project
        val service = ImageParsingService.getService(project)

    }
}