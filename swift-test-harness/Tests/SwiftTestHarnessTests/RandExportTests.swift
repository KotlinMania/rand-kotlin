import Testing
import Rand

@Suite struct RandExportTests {
    @Test func testSwiftModuleLoads() {
        #expect(Bool(true), "Rand swift module imported cleanly")
    }
}



